package com.ivanroot.minplayer.player;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.hwangjr.rxbus.Bus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.activity.PlayerActivity;
import com.ivanroot.minplayer.activity.StartupActivity;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.playlist.Playlist;
import com.ivanroot.minplayer.playlist.PlaylistManager;
import com.ivanroot.minplayer.utils.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;


public class PlayerService

        extends Service
        implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener,
        AudioManager.OnAudioFocusChangeListener {


    public static final String SERVICE_NAME = "PlayerService";
    private final int permissionDenied = PackageManager.PERMISSION_DENIED;
    private final int permissionGranted = PackageManager.PERMISSION_GRANTED;
    private static final int NOTIFICATION_ID = 101;
    private PlayerBinder localBinder = new PlayerBinder();
    private SharedPreferences settings;
    private Playlist playlist;
    private Queue<Audio> nextQueue;
    private BroadcastReceiver becomingNoisyReceiver;
    private Audio currAudio;
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private MediaSessionManager mediaSessionManager;
    private MediaSession mediaSession;
    private MediaController.TransportControls transportControls;
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private boolean wasPlaying;
    private String nextEvent = PlayerActionsEvents.EVENT_METADATA_UPDATED;
    private boolean isNextQueueUsing = false;
    private int mpError = 0;
    private Disposable playlistSubscription;
    private PlaylistManager playlistManager = PlaylistManager.getInstance();
    private Bus rxBus = RxBus.getInstance();


    @Override
    public void onCreate() {
        Log.i("PlayerService", "onCreate");
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnInfoListener(this);
        nextQueue = new PriorityQueue<>();
        rxBus.register(this);
        enableEqualizer();
        initMediaSession();
        becomingNoisyReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                pause();
            }
        };
        registerBecomingNoisy();
        callStateListener();
        loadSettings();
        prepareToPlay();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("PlayerService", "onStartCommand");
        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i("PlayerService", "onDestroy");
        super.onDestroy();
        unregisterReceiver(becomingNoisyReceiver);
        disableEqualizer();
        removeNotification();
        mediaSession.release();
        mediaPlayer.stop();
        if (playlistSubscription != null) {
            playlistSubscription.dispose();
        }
        writeSettings();
        rxBus.unregister(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("PlayerService", "onBind");
        rxBus.post(PlayerActionsEvents.EVENT_METADATA_UPDATED,getCurrentStateMap());
        return localBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        Log.i("PlayerService", "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.i("PlayerService", "onCompletion");
        //removeAudioFocus();
        if (getAudioPosition() != 0 && mpError == 0) {
            playNextTrack();
        }
    }

    private void prepareToPlay() {
        Log.i("PlayerService", "prepareToPlay");
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            if (currAudio != null) {
                if (playlist.size() > 0) {

                    try {
                        mediaPlayer.setDataSource(currAudio.getData());
                    } catch (IOException ex) {

                        Log.e(SERVICE_NAME, ex.getMessage());
                        removeNotification();
                        stopSelf();
                    }
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    try {
                        mediaPlayer.prepareAsync();
                    } catch (IllegalStateException ex) {
                        Toast.makeText(this, "prepareToPlay() " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(SERVICE_NAME, "prepareToPlay() " + ex.getMessage());
                    }
                }
            }
        }
    }

    private void enableEqualizer() {
        Log.i("PlayerService", "enableEqualizer");
        Intent equalizerIntent = new Intent();
        equalizerIntent.setAction(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
        equalizerIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, mediaPlayer.getAudioSessionId());
        equalizerIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        sendBroadcast(equalizerIntent);
    }

    private void disableEqualizer() {
        Log.i("PlayerService", "disableEqualizer");
        Intent equalizerIntent = new Intent();
        equalizerIntent.setAction(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        equalizerIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, mediaPlayer.getAudioSessionId());
        equalizerIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        sendBroadcast(equalizerIntent);
    }

    @Subscribe(tags = {@Tag(PlayerActionsEvents.ACTION_SET_PLAYLIST)})
    public void setPlaylist(String playlistName) {

        if (playlist != null)
            if (playlistName.equals(playlist.getName())) return;

        if (playlistSubscription != null)
            playlistSubscription.dispose();

        //PlaylistManager.writePlaylist(this,playlist);
        playlistSubscription = playlistManager.getPlaylistObservable(this,playlistName)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setPlaylist);

    }

    private void setPlaylist(Playlist playlist) {
        this.playlist = playlist;

        if (!playlist.checkAndSetAudio(currAudio)) {
            currAudio = playlist.getCurrentAudio();
            updateMetaData();
            prepareToPlay();
            rxBus.post(PlayerActionsEvents.EVENT_PLAYLIST_CHANGED, playlist.getName());
        }

    }

    @Subscribe(tags = {@Tag(PlayerActionsEvents.ACTION_GET_PLAYLIST)})
    public Playlist getPlaylist(Object object) {
        rxBus.post(PlayerActionsEvents.ACTION_GET_PLAYLIST, playlist);
        return playlist;
    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.i("PlayerService", "onError");
        mpError = what;
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        Log.i("PlayerService", "onInfo");
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.i("PlayerService", "onPrepared");
        requestAudioFocus();
        buildNotification(wasPlaying, true);
        mpError = 0;
        if (wasPlaying)
            mediaPlayer.start();

        updateMetaData();
        rxBus.post(nextEvent, getCurrentStateMap());
        buildAndSetPlaybackState(wasPlaying);
    }

    @NonNull
    public HashMap<String, Object> getCurrentStateMap() {

        HashMap<String, Object> state = new HashMap<>();
        state.put(PlayerActionsEvents.KEY_AUDIO, getCurrAudio());
        state.put(PlayerActionsEvents.KEY_IS_SHUFFLED, isShuffled());
        state.put(PlayerActionsEvents.KEY_IS_PLAYING, wasPlaying);
        state.put(PlayerActionsEvents.KEY_RP_MODE, getRepeatMode());
        state.put(PlayerActionsEvents.KEY_DURATION, getAudioDuration());
        state.put(PlayerActionsEvents.KEY_POSITION,getAudioPosition());
        return state;
    }

    private void buildAndSetPlaybackState(boolean isPlaying){

        int state = (isPlaying ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED);

        mediaSession.setPlaybackState(new PlaybackState.Builder()
                .setActions(
                        PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PAUSE |
                        PlaybackState.ACTION_SKIP_TO_NEXT | PlaybackState.ACTION_SKIP_TO_PREVIOUS)
                .setState(state,getAudioPosition(),1)
                .build());

    }

    @Subscribe(tags = {@Tag(PlayerActionsEvents.ACTION_GET_METADATA)})
    public void onGetMetadata(Object object){
        rxBus.post(PlayerActionsEvents.EVENT_ON_GET_METADATA,getCurrentStateMap());
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        Log.i("PlayerService", "onAudioFocusChange");
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (wasPlaying) {
                    mediaPlayer.start();
                    rxBus.post(PlayerActionsEvents.EVENT_AUDIO_IS_PLAYING, true);
                }
                buildNotification(wasPlaying, true);
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    rxBus.post(PlayerActionsEvents.EVENT_AUDIO_IS_PAUSED, false);
                }
                removeAudioFocus();
                buildNotification(false, false);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    rxBus.post(PlayerActionsEvents.EVENT_AUDIO_IS_PAUSED, false);
                }
                buildNotification(false, false);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private boolean requestAudioFocus() {
        Log.i("PlayerService", "requestAudioFocus");
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return true;
        }
        return false;
    }

    private boolean removeAudioFocus() {
        Log.i("PlayerService", "removeAudioFocus");
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }

    public boolean isPlaying() {
        Log.i("PlayerService", "isPlaying");
        if (mediaPlayer != null)
            return mediaPlayer.isPlaying();
        return false;
    }

    @Subscribe(tags = {@Tag(PlayerActionsEvents.ACTION_IS_PLAYING)})
    public boolean isPlaying(Object object) {
        boolean isPlaying = isPlaying();
        if (isPlaying)
            rxBus.post(PlayerActionsEvents.EVENT_AUDIO_IS_PLAYING, true);
        else
            rxBus.post(PlayerActionsEvents.EVENT_AUDIO_IS_PAUSED, false);
        return isPlaying;
    }

    public boolean isShuffled() {
        Log.i("PlayerService", "isShuffled");
        if (playlist != null)
            return playlist.isShuffled();
        else
            return false;
    }

    @Subscribe(tags = {@Tag(PlayerActionsEvents.ACTION_IS_SHUFFLED)})
    public boolean isShuffled(Object object) {
        boolean isShuffled = isShuffled();
        if (isShuffled)
            rxBus.post(PlayerActionsEvents.EVENT_SHUFFLED, true);
        else
            rxBus.post(PlayerActionsEvents.EVENT_UNSHUFFLED, false);
        return isShuffled;
    }

    public void addToNextQueue(Audio audio) {
        Log.i("PlayerService", "addToNextQueue");
        nextQueue.offer(audio);
        isNextQueueUsing = true;
    }

    public void clearNextQueue() {
        Log.i("PlayerService", "clearNextQueue");
        if (playlist != null) {
            currAudio = playlist.getCurrentAudio();
        }
        nextQueue.clear();
        isNextQueueUsing = false;
    }

    public Queue<Audio> getNextQueue() {
        Log.i("PlayerService", "getNextQueue");
        return nextQueue;
    }

    @Subscribe(tags = {@Tag(PlayerActionsEvents.ACTION_PLAY_OR_PAUSE)})
    public void playOrPause(Object object) {
        if (isPlaying())
            pause();
        else
            play();
    }

    @Subscribe(tags = {@Tag(PlayerActionsEvents.ACTION_PLAY_AUDIO)})
    public void play(Object object){
        if(object instanceof Audio)
            play((Audio)object);
        else play();
    }

    public void play() {
        Log.i("PlayerService", "play()");
        if (mediaPlayer != null) {
            if (!mediaPlayer.isPlaying()) {
                requestAudioFocus();
                buildNotification(true, true);
                mediaPlayer.start();
                rxBus.post(PlayerActionsEvents.EVENT_AUDIO_IS_PLAYING, true);
            }
            wasPlaying = mediaPlayer.isPlaying();
            buildAndSetPlaybackState(wasPlaying);
        }
    }

    @Subscribe(tags = {@Tag(PlayerActionsEvents.ACTION_SET_AND_PLAY_AUDIO)})
    public void play(Audio audio) {
        Log.i("PlayerService", "play(Audio Audio)");
        if (mediaPlayer != null && playlist != null) {
            if (!audio.equals(currAudio)) {
                if (playlist.checkAndSetAudio(audio)) {
                    currAudio = audio;
                } else {
                    nextQueue.offer(audio);
                    currAudio = nextQueue.poll();
                }
                nextEvent = PlayerActionsEvents.EVENT_METADATA_UPDATED;
                wasPlaying = true;
                prepareToPlay();
            }

        }
    }

    @Subscribe(tags = {@Tag(PlayerActionsEvents.ACTION_PAUSE_AUDIO)})
    public void pause(Object object){
        pause();
    }

    public void pause() {
        Log.i("PlayerService", "pause");
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                buildNotification(false, true);
                mediaPlayer.pause();
                rxBus.post(PlayerActionsEvents.EVENT_AUDIO_IS_PAUSED, false);
            }
            wasPlaying = mediaPlayer.isPlaying();
            buildAndSetPlaybackState(wasPlaying);
        }
    }

    @Subscribe(tags = {@Tag(PlayerActionsEvents.ACTION_NEXT_AUDIO)})
    public void playNextTrack(Object object) {
        playNextTrack();
    }

    public void playNextTrack() {
        Log.i("PlayerService", "playNextTrack");
        if (nextQueue.size() == 0) {
            isNextQueueUsing = false;

            if (playlist.setToNextAudio()) {

                nextEvent = PlayerActionsEvents.EVENT_NEXT_AUDIO_METADATA;
                currAudio = playlist.getCurrentAudio();
                prepareToPlay();

            } else {

                if (playlist.getRepeatMode() == Playlist.REPEAT_ONE) {
                    nextEvent = PlayerActionsEvents.EVENT_NONE;
                    prepareToPlay();
                }

            }
        } else {
            currAudio = nextQueue.poll();
            isNextQueueUsing = true;
            nextEvent = PlayerActionsEvents.EVENT_NEXT_AUDIO_METADATA;
            prepareToPlay();
        }

    }

    @Subscribe(tags = {@Tag(PlayerActionsEvents.ACTION_PREV_AUDIO)})
    public void playPrevTrack(Object object) {
        playPrevTrack();
    }

    public void playPrevTrack() {
        Log.i("PlayerService", "playPrevTrack");
        if (getAudioPosition() > 6000) {
            nextEvent = PlayerActionsEvents.EVENT_REPLAYING_CURR_AUDIO;
            prepareToPlay();
        } else {
            boolean result = playlist.setToPrevAudio();
            if (isNextQueueUsing) {

                if (result) playlist.setToNextAudio();
                nextEvent = PlayerActionsEvents.EVENT_PREV_AUDIO_METADATA;
            } else {

                if (result) {
                    nextEvent = PlayerActionsEvents.EVENT_PREV_AUDIO_METADATA;
                } else {
                    nextEvent = PlayerActionsEvents.EVENT_NONE;
                }
            }
            currAudio = playlist.getCurrentAudio();
            prepareToPlay();
        }
    }

    @Subscribe(tags = {@Tag(PlayerActionsEvents.ACTION_FAST_FORWARD)})
    public void fastForward(Integer forwardShift) {
        Log.i("PlayerService", "fastForward");
        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + forwardShift);
        rxBus.post(PlayerActionsEvents.EVENT_FAST_FORWARD,forwardShift);
        rxBus.post(PlayerActionsEvents.EVENT_ON_POSITION_CHANGED,getAudioPosition());
    }

    @Subscribe(tags = {@Tag(PlayerActionsEvents.ACTION_REWIND)})
    public void fastRewind(Integer backwardShift) {
        Log.i("PlayerService", "fastRewind");
        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - backwardShift);
        rxBus.post(PlayerActionsEvents.EVENT_REWIND,backwardShift);
        rxBus.post(PlayerActionsEvents.EVENT_ON_POSITION_CHANGED,getAudioPosition());
    }

    @Subscribe(tags = {@Tag(PlayerActionsEvents.ACTION_GET_CURR_AUDIO)})
    public void getCurrAudio(Object object) {
        rxBus.post(PlayerActionsEvents.EVENT_ON_GET_CURR_AUDIO, getCurrAudio());
    }

    public Audio getCurrAudio() {
        Log.i("PlayerService", "getCurrAudio");
        return currAudio;
    }

    @Subscribe(tags = {@Tag(PlayerActionsEvents.ACTION_GET_AUDIO_SESSION_ID)})
    public void getAudioSessionId(Object object) {
        rxBus.post(PlayerActionsEvents.EVENT_ON_GET_AUDIO_SESSION_ID, getAudioSessionId());
    }

    public int getAudioSessionId() {
        if (mediaPlayer != null) {
            return mediaPlayer.getAudioSessionId();
        }
        return 0;
    }

    @Subscribe(tags = {@Tag(PlayerActionsEvents.ACTION_GET_AUDIO_DURATION)})
    public void getAudioDuration(Object object) {
        rxBus.post(PlayerActionsEvents.EVENT_ON_GET_AUDIO_DURATION, getAudioDuration());
    }

    public int getAudioDuration() {
        Log.i("PlayerService", "getAudioDuration");
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    @Subscribe(tags = {@Tag(PlayerActionsEvents.ACTION_GET_AUDIO_POSITION)})
    public void getAudioPosition(Object object) {
        rxBus.post(PlayerActionsEvents.EVENT_ON_GET_AUDIO_POSITION, getAudioPosition());
    }

    public int getAudioPosition() {
        //Log.i("PlayerService","getAudioPosition");
        if (mediaPlayer != null) {

            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Subscribe(tags = {@Tag(PlayerActionsEvents.ACTION_SEEK_TO)})
    public void seekTo(Integer pos) {
        Log.i("PlayerService", "seekTo");
        if (mediaPlayer != null) {

            mediaPlayer.seekTo(pos);
            rxBus.post(PlayerActionsEvents.EVENT_ON_POSITION_CHANGED, pos);
        }
    }

    @Subscribe(tags = {@Tag(PlayerActionsEvents.ACTION_CHANGE_RP_MODE)})
    public void changeRepeatMode(Object object) {
        if (object instanceof Integer) {
            setRepeatMode((Integer) object);
        } else {
            setRepeatMode((getRepeatMode() + 1) % 3);
        }
    }


    public void setRepeatMode(int repeatMode) {
        Log.i("PlayerService", "setRepeatMode");
        if (playlist != null) {
            playlist.setRepeatMode(repeatMode);
            rxBus.post(PlayerActionsEvents.EVENT_RP_MODE_CHANGED,getRepeatMode());
        }
    }

    @Subscribe(tags = {@Tag(PlayerActionsEvents.ACTION_GET_RP_MODE)})
    public void getRepeatMode(Object object){
        rxBus.post(PlayerActionsEvents.EVENT_ON_GET_RP_MODE,getRepeatMode());
    }

    public int getRepeatMode() {
        Log.i("PlayerService", "getRepeatMode");
        if (playlist != null)
            return playlist.getRepeatMode();
        else
            return Playlist.NOT_REPEAT;
    }

    @Subscribe(tags = {@Tag(PlayerActionsEvents.ACTION_CHANGE_SHUFFLE_MODE)})
    public void shuffleOrUnShuffle(Object object) {
        boolean isShuffled;
        if (object instanceof Boolean)
            isShuffled = (Boolean) object;
        else
            isShuffled = isShuffled();

        if (isShuffled) {
            unShufflePlaylist();
        } else {
            shufflePlaylist();
        }
    }

    public void shufflePlaylist() {
        Log.i("PlayerService", "shufflePlaylist");
        if (playlist != null) {
            playlist.shuffle();
            rxBus.post(PlayerActionsEvents.EVENT_SHUFFLED, true);
        }
    }

    public void unShufflePlaylist() {
        Log.i("PlayerService", "unShufflePlaylist");
        if (playlist != null) {
            playlist.unShuffle();
            rxBus.post(PlayerActionsEvents.EVENT_UNSHUFFLED, false);
        }
    }

    private void loadSettings() {
        Log.i("PlayerService", "loadSettings");
        settings = getSharedPreferences(SERVICE_NAME, Context.MODE_PRIVATE);
        //wasPlaying = settings.getBoolean("wasPlaying",false);
        if (checkPermissions()) {
            String playlistName = settings.getString("playlist", PlaylistManager.ALL_TRACKS_PLAYLIST);
            setPlaylist(playlistName);
        } else {
            startActivity(new Intent(this, StartupActivity.class));
            stopSelf();
        }

    }

    private void writeSettings() {
        Log.i("PlayerService", "writeSettings");
        SharedPreferences.Editor editor = settings.edit();
        //editor.putBoolean("wasPlaying", wasPlaying);
        if (checkPermissions()) {
            editor.putString("playlist", playlist.getName());
            editor.commit();
            playlistManager.writePlaylist(this, playlist);
        }
    }

    private void registerBecomingNoisy() {
        Log.i("PlayerService", "registerBecomingNoisy");
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, filter);
    }

    private void callStateListener() {
        Log.i("PlayerService", "callStateListener");
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            if (mediaPlayer.isPlaying()) {
                                mediaPlayer.pause();
                                rxBus.post(PlayerActionsEvents.EVENT_AUDIO_IS_PAUSED,false);
                            }
                            buildNotification(false, false);
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                if (wasPlaying) {
                                    mediaPlayer.start();
                                    rxBus.post(PlayerActionsEvents.EVENT_AUDIO_IS_PLAYING,true);
                                    buildNotification(true, true);
                                }
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }


    private void initMediaSession() {
        Log.i("PlayerService", "initMediaSession");
        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        mediaSession = new MediaSession(getApplicationContext(), SERVICE_NAME);
        transportControls = mediaSession.getController().getTransportControls();
        mediaSession.setCallback(new MediaSession.Callback() {

            @Override
            public void onPlay() {
                super.onPlay();
                play();
            }

            @Override
            public void onPause() {
                super.onPause();
                pause();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                playNextTrack();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                playPrevTrack();
            }

            @Override
            public void onFastForward() {
                super.onFastForward();
                fastForward(250);
            }

            @Override
            public void onRewind() {
                super.onRewind();
                fastRewind(250);
            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                seekTo((int) position);
                super.onSeekTo(position);
            }
        });
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS | MediaSession.FLAG_HANDLES_MEDIA_BUTTONS);
        mediaSession.setActive(true);
    }


    private void handleIncomingActions(Intent playbackAction) {
        Log.i("PlayerService", "handleIncomingActions");
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(PlayerActionsEvents.ACTION_PLAY_AUDIO) && !mediaPlayer.isPlaying()) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(PlayerActionsEvents.ACTION_PAUSE_AUDIO) && mediaPlayer.isPlaying()) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(PlayerActionsEvents.ACTION_NEXT_AUDIO)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(PlayerActionsEvents.ACTION_PREV_AUDIO)) {
            transportControls.skipToPrevious();
        }
    }

    private void buildNotification(boolean isPlaying, boolean ongoing) {
        Log.i("PlayerService", "buildNotification");
        if (currAudio != null) {

            int notificationAction = R.drawable.ic_pause_noti;//needs to be initialized
            PendingIntent playPauseAction = null;

            //Build a new notification according to the current state of the MediaPlayer
            if (isPlaying) {
                notificationAction = R.drawable.ic_pause_noti;
                //create the pause action
                playPauseAction = playbackAction(1);
            } else if (!isPlaying) {
                notificationAction = R.drawable.ic_play_noti;
                //create the play action
                playPauseAction = playbackAction(0);
            }

            Bitmap largeIcon = Utils.getAudioAlbumArt(currAudio.getAlbumArtPath(),
                    BitmapFactory.decodeResource(getResources(), R.drawable.default_album_art));

            Intent intent = new Intent(this, PlayerActivity.class);

            Notification.Builder notificationBuilder = new Notification.Builder(this)
                    .setContentIntent(PendingIntent.getActivity(this, 0, intent, 0))
                    .setShowWhen(false)
                    .setOngoing(ongoing)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setStyle(new Notification.MediaStyle()
                            .setMediaSession(mediaSession.getSessionToken())
                            .setShowActionsInCompactView(0, 1, 2))
                    .setLargeIcon(largeIcon)
                    //.setColor(getResources().getColor(R.color.colorPrimary))
                    .setSmallIcon(R.drawable.ic_music_rounded)
                    .setContentText(currAudio.getArtist())
                    .setContentTitle(currAudio.getTitle())
                    .addAction(R.drawable.ic_prev_noti, "previous", playbackAction(3))
                    .addAction(notificationAction, "play/pause", playPauseAction)
                    .addAction(R.drawable.ic_next_noti, "next", playbackAction(2));

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
        }
    }

    private PendingIntent playbackAction(int actionNumber) {
        Log.i("PlayerService", "playbackAction");
        Intent playbackAction = new Intent(this, PlayerService.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(PlayerActionsEvents.ACTION_PLAY_AUDIO);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(PlayerActionsEvents.ACTION_PAUSE_AUDIO);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction(PlayerActionsEvents.ACTION_NEXT_AUDIO);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction(PlayerActionsEvents.ACTION_PREV_AUDIO);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    private void removeNotification() {
        Log.i("PlayerService", "removeNotification");
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void updateMetaData() {

        Log.i("PlayerService", "updateMetadata");
        if (currAudio != null) {
            Bitmap albumArt = Utils.getAudioAlbumArt(currAudio.getAlbumArtPath(),
                    BitmapFactory.decodeResource(getResources(),R.drawable.default_album_art));

            mediaSession.setMetadata(new MediaMetadata.Builder()
                    .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, albumArt)
                    .putString(MediaMetadata.METADATA_KEY_ARTIST, currAudio.getArtist())
                    .putString(MediaMetadata.METADATA_KEY_ALBUM, currAudio.getAlbum())
                    .putString(MediaMetadata.METADATA_KEY_TITLE, currAudio.getTitle())
                    .build());
        }
    }

    public class PlayerBinder extends Binder {

        public PlayerService getService() {

            return PlayerService.this;

        }

        public MediaController.TransportControls getController() {

            return transportControls;
        }

    }

    private boolean checkPermissions() {

        int readStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPhoneState = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (readStorage == permissionGranted && writeStorage == permissionGranted && readPhoneState == permissionGranted)
            return true;
        return false;
    }
}

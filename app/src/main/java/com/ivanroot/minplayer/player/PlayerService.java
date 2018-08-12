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
import android.media.audiofx.AudioEffect;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Surface;

import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.hwangjr.rxbus.Bus;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.activity.MainActivity;
import com.ivanroot.minplayer.activity.StartupActivity;
import com.ivanroot.minplayer.activity.TokenActivity;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.disk.RestClientUtil;
import com.ivanroot.minplayer.player.constants.PlayerActions;
import com.ivanroot.minplayer.player.constants.PlayerEvents;
import com.ivanroot.minplayer.player.constants.PlayerKeys;
import com.ivanroot.minplayer.playlist.Playlist;
import com.ivanroot.minplayer.playlist.PlaylistManager;
import com.ivanroot.minplayer.utils.Pair;
import com.ivanroot.minplayer.utils.RxNetworkChangeReceiver;
import com.ivanroot.minplayer.utils.Utils;
import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.RestClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;


public class PlayerService extends Service implements
        AudioManager.OnAudioFocusChangeListener {

    public static final String SERVICE_NAME = "PlayerService";
    private final int permissionDenied = PackageManager.PERMISSION_DENIED;
    private final int permissionGranted = PackageManager.PERMISSION_GRANTED;
    private static final int NOTIFICATION_ID = 101;
    private int audioSessionId = 0;
    private RestClient restClient;
    private PlayerBinder localBinder = new PlayerBinder();
    private SharedPreferences settings;
    private Playlist playlist;
    private Queue<Audio> nextQueue;
    private BroadcastReceiver becomingNoisyReceiver;
    private Audio currAudio;
    private AudioManager audioManager;
    private TrackSelection.Factory trackSelectionFactory;
    private TrackSelector trackSelector;
    private BandwidthMeter bandwidthMeter;
    private DefaultDataSourceFactory defaultDataSourceFactory;
    private OkHttpDataSourceFactory okHttpDataSourceFactory;
    private SimpleExoPlayer exoPlayer;
    private PlayerAnalyticsListener playerAnalyticsListener;
    private MediaSessionManager mediaSessionManager;
    private MediaSession mediaSession;
    private MediaController.TransportControls transportControls;
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private boolean wasPlaying;
    //private String nextEvent = PlayerEvents.EVENT_METADATA_UPDATED;
    private boolean isNextQueueUsing = false;
    private Disposable playlistDisposable;
    private Disposable restDisposable;
    private CompositeDisposable compositeDisposable;
    private PlaylistManager playlistManager = PlaylistManager.getInstance();
    private Bus rxBus = RxBus.get();
    private ArrayList<Long> clicks = new ArrayList<>();

    @Override
    public void onCreate() {
        Log.i("PlayerService", "onCreate");
        super.onCreate();
        nextQueue = new PriorityQueue<>();
        rxBus.register(this);
        setupRestClient();
        initExoPlayer();
        initMediaSession();
        registerBecomingNoisy();
        setupCallStateListener();
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
        exoPlayer.stop();
        exoPlayer.release();

        if (playlistDisposable != null) {
            playlistDisposable.dispose();
        }

        if (restDisposable != null) {
            restDisposable.dispose();
        }

        writeSettings();
        rxBus.unregister(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("PlayerService", "onBind");
        rxBus.post(PlayerEvents.EVENT_METADATA_UPDATED, getCurrentStateMap());
        return localBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("PlayerService", "onUnbind");
        return super.onUnbind(intent);
    }

    void initExoPlayer() {
        bandwidthMeter = new DefaultBandwidthMeter();
        trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        trackSelector = new DefaultTrackSelector(trackSelectionFactory);
        playerAnalyticsListener = new PlayerAnalyticsListener();
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        exoPlayer.addAnalyticsListener(playerAnalyticsListener);

        String userAgent = Util.getUserAgent(this, "MinPlayer");
        defaultDataSourceFactory = new DefaultDataSourceFactory(this, userAgent);
        okHttpDataSourceFactory = new OkHttpDataSourceFactory(new OkHttpClient(), "Android.ExoPlayer", null);

    }

    void setupRestClient() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        Observable<String> tokenObservable = RxSharedPreferences.create(preferences)
                .getString(TokenActivity.PREF_ACCESS_TOKEN)
                .asObservable();

        Observable<NetworkInfo> networkInfoObservable = RxNetworkChangeReceiver.create(this)
                .register()
                .asObservable();

        restDisposable = RestClientUtil.asObservable(tokenObservable, networkInfoObservable)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(state -> {
                    restClient = (RestClient)state.get(RestClientUtil.KEY_REST_CLIENT);
                    if (playlist != null && playlist.getName().equals(PlaylistManager.DISK_ALL_TRACKS_PLAYLIST)) {
                        playlistDisposable.dispose();
                        playlistDisposable = playlistManager.getPlaylistObservable(this, restClient, PlaylistManager.DISK_ALL_TRACKS_PLAYLIST)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(this::setPlaylist);
                    }
                });
    }

    private void prepareToPlay() {
        Log.i("PlayerService", "prepareToPlay");

        if (currAudio != null) {
            if (playlist.size() > 0) {

                if (currAudio.getLocalData() != null) {
                    MediaSource mediaSource = new ExtractorMediaSource.Factory(defaultDataSourceFactory)
                            .createMediaSource(Uri.parse(currAudio.getLocalData()));
                    exoPlayer.prepare(mediaSource);
                    exoPlayer.setPlayWhenReady(wasPlaying);
                } else if (currAudio.getCloudData() != null && restClient != null) {
                    Disposable dis = Single.<Uri>create(emitter -> emitter.onSuccess(Uri.parse(restClient.getDownloadLink(currAudio.getCloudData()).getHref())))
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(uri -> {
                                MediaSource mediaSource = new ExtractorMediaSource.Factory(okHttpDataSourceFactory)
                                        .createMediaSource(uri);
                                exoPlayer.prepare(mediaSource);
                                exoPlayer.setPlayWhenReady(wasPlaying);
                            });
                }

            }
        }

    }

    private void enableEqualizer() {
        Log.i("Enable Equaliser", String.valueOf(exoPlayer.getAudioSessionId()));
        Intent equalizerIntent = new Intent();
        equalizerIntent.setAction(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
        equalizerIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId);
        equalizerIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        sendBroadcast(equalizerIntent);
    }

    private void disableEqualizer() {
        Log.i("PlayerService", "disableEqualizer");
        Intent equalizerIntent = new Intent();
        equalizerIntent.setAction(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        equalizerIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId);
        equalizerIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        sendBroadcast(equalizerIntent);
    }

    @Subscribe(tags = {@Tag(PlayerActions.ACTION_SET_PLAYLIST)})
    public void setPlaylist(String playlistName) {

        if (playlist != null)
            if (playlistName.equals(playlist.getName())) return;

        if (playlistDisposable != null)
            playlistDisposable.dispose();

        //PlaylistManager.writePlaylist(this,playlist);
        playlistDisposable = playlistManager.getPlaylistObservable(this, restClient, playlistName)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setPlaylist);

    }

    private void setPlaylist(Playlist playlist) {
        this.playlist = playlist;

        if (!playlist.checkAndSetAudio(currAudio)) {
            currAudio = playlist.getCurrentAudio();
            rxBus.post(PlayerEvents.EVENT_PLAYLIST_CHANGED, playlist.getName());
            updateMetaData();
            prepareToPlay();
        }

    }

    @Subscribe(tags = {@Tag(PlayerActions.ACTION_GET_PLAYLIST)})
    public Playlist getPlaylist(Object object) {
        rxBus.post(PlayerActions.ACTION_GET_PLAYLIST, playlist);
        return playlist;
    }


    @NonNull
    public HashMap<String, Object> getCurrentStateMap() {
        HashMap<String, Object> state = new HashMap<>();
        state.put(PlayerKeys.KEY_AUDIO, getCurrAudio());
        state.put(PlayerKeys.KEY_IS_SHUFFLED, isShuffled());
        state.put(PlayerKeys.KEY_IS_PLAYING, wasPlaying);
        state.put(PlayerKeys.KEY_RP_MODE, getRepeatMode());
        state.put(PlayerKeys.KEY_DURATION, getAudioDuration());
        state.put(PlayerKeys.KEY_POSITION, getAudioPosition());
        return state;
    }

    private void buildAndSetPlaybackState(boolean isPlaying) {
        int state = (isPlaying ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED);

        if (!mediaSession.isActive())
            mediaSession.setActive(true);

        mediaSession.setPlaybackState(new PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY |
                        PlaybackState.ACTION_PAUSE |
                        PlaybackState.ACTION_SKIP_TO_NEXT |
                        PlaybackState.ACTION_SKIP_TO_PREVIOUS)
                .setState(state, getAudioPosition(), 1)
                .build());

    }

    @Subscribe(tags = {@Tag(PlayerActions.ACTION_GET_METADATA)})
    public void onGetMetadata(Object object) {
        rxBus.post(PlayerEvents.EVENT_ON_GET_METADATA, getCurrentStateMap());
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        Log.i("onAudioFocusChange", String.valueOf(focusChange));
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (wasPlaying) {
                    exoPlayer.setPlayWhenReady(true);
                    rxBus.post(PlayerEvents.EVENT_AUDIO_IS_PLAYING, true);
                }
                buildNotification(wasPlaying, true);
                exoPlayer.setVolume(1f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                if (isPlaying()) {
                    exoPlayer.setPlayWhenReady(false);
                    rxBus.post(PlayerEvents.EVENT_AUDIO_IS_PAUSED, false);
                }
                removeAudioFocus();
                buildNotification(false, false);
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (!ongoingCall) {
                    if (isPlaying()) {
                        exoPlayer.setPlayWhenReady(false);
                        rxBus.post(PlayerEvents.EVENT_AUDIO_IS_PAUSED, false);
                    }
                    buildNotification(false, false);
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (isPlaying()) exoPlayer.setVolume(0.1f);
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
        return exoPlayer.getPlayWhenReady();
    }

    @Subscribe(tags = {@Tag(PlayerActions.ACTION_IS_PLAYING)})
    public boolean isPlaying(Object object) {
        boolean isPlaying = isPlaying();
        if (isPlaying)
            rxBus.post(PlayerEvents.EVENT_AUDIO_IS_PLAYING, true);
        else
            rxBus.post(PlayerEvents.EVENT_AUDIO_IS_PAUSED, false);
        return isPlaying;
    }

    public boolean isShuffled() {
        Log.i("PlayerService", "isShuffled");
        if (playlist != null)
            return playlist.isShuffled();
        else
            return false;
    }

    @Subscribe(tags = {@Tag(PlayerActions.ACTION_IS_SHUFFLED)})
    public boolean isShuffled(Object object) {
        boolean isShuffled = isShuffled();
        if (isShuffled)
            rxBus.post(PlayerEvents.EVENT_SHUFFLED, true);
        else
            rxBus.post(PlayerEvents.EVENT_UNSHUFFLED, false);
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

    @Subscribe(tags = {@Tag(PlayerActions.ACTION_PLAY_OR_PAUSE)})
    public void playOrPause(Object object) {
        if (isPlaying())
            pause();
        else
            play();
    }

    @Subscribe(tags = {@Tag(PlayerActions.ACTION_PLAY_AUDIO)})
    public void play(Object object) {
        if (object instanceof Audio)
            play((Audio) object);
        else play();
    }

    public void play() {
        Log.i("PlayerService", "play()");
        if (!isPlaying()) {
            requestAudioFocus();
            buildNotification(true, true);
            exoPlayer.setPlayWhenReady(true);
            rxBus.post(PlayerEvents.EVENT_AUDIO_IS_PLAYING, true);
        }
        wasPlaying = isPlaying();
        buildAndSetPlaybackState(wasPlaying);

    }

    @Subscribe(tags = {@Tag(PlayerActions.ACTION_SET_AND_PLAY_AUDIO)})
    public void play(Audio audio) {
        Log.i("PlayerService", "play(Audio)");
        if (playlist != null && audio != null && !audio.equals(currAudio)) {
            if (playlist.checkAndSetAudio(audio)) {
                currAudio = audio;
            } else {
                nextQueue.offer(audio);
                currAudio = nextQueue.poll();
            }
            rxBus.post(PlayerEvents.EVENT_METADATA_UPDATED, getCurrentStateMap());
            wasPlaying = true;
            prepareToPlay();
        }
    }

    @Subscribe(tags = {@Tag(PlayerActions.ACTION_PAUSE_AUDIO)})
    public void pause(Object object) {
        pause();
    }

    public void pause() {
        Log.i("PlayerService", "pause");
        if (isPlaying()) {
            buildNotification(false, true);
            rxBus.post(PlayerEvents.EVENT_AUDIO_IS_PAUSED, false);
            exoPlayer.setPlayWhenReady(false);
        }
        wasPlaying = isPlaying();
        buildAndSetPlaybackState(wasPlaying);

    }

    public void stop() {
        removeNotification();
        if (isPlaying()) {
            exoPlayer.setPlayWhenReady(false);
            rxBus.post(PlayerEvents.EVENT_AUDIO_IS_PAUSED, false);
        }
        wasPlaying = isPlaying();
        mediaSession.setActive(false);
        mediaSession.setPlaybackState(new PlaybackState.Builder().setState(PlaybackState.STATE_STOPPED, getAudioPosition(), 1).build());
    }

    @Subscribe(tags = {@Tag(PlayerActions.ACTION_NEXT_AUDIO)})
    public void playNextTrack(Object object) {
        playNextTrack();
    }

    public void playNextTrack() {
        Log.i("PlayerService", "playNextTrack");
        if (nextQueue.size() == 0) {
            isNextQueueUsing = false;

            if (playlist.setToNextAudio()) {
                //nextEvent = PlayerEvents.EVENT_NEXT_AUDIO_METADATA;
                currAudio = playlist.getCurrentAudio();
                rxBus.post(PlayerEvents.EVENT_NEXT_AUDIO_METADATA, getCurrentStateMap());
                prepareToPlay();

            } else {

                if (playlist.getRepeatMode() == Playlist.REPEAT_ONE) {
                    //nextEvent = PlayerEvents.EVENT_NONE;
                    rxBus.post(PlayerEvents.EVENT_REPLAYING_CURR_AUDIO, getCurrentStateMap());
                    seekTo(0);
                }

            }
        } else {
            currAudio = nextQueue.poll();
            isNextQueueUsing = true;
            //nextEvent = PlayerEvents.EVENT_NEXT_AUDIO_METADATA;
            rxBus.post(PlayerEvents.EVENT_NEXT_AUDIO_METADATA, getCurrentStateMap());
            prepareToPlay();
        }
        buildNotification(false, true);
    }

    @Subscribe(tags = {@Tag(PlayerActions.ACTION_PREV_AUDIO)})
    public void playPrevTrack(Object object) {
        playPrevTrack();
    }

    public void playPrevTrack() {
        Log.i("PlayerService", "playPrevTrack");
        if (getAudioPosition() > 6000) {
            //nextEvent = PlayerEvents.EVENT_REPLAYING_CURR_AUDIO;
            rxBus.post(PlayerEvents.EVENT_REPLAYING_CURR_AUDIO, getCurrentStateMap());
            seekTo(0);
        } else {
            boolean result = playlist.setToPrevAudio();
            if (isNextQueueUsing) {
                if (result) playlist.setToNextAudio();
                //nextEvent = PlayerEvents.EVENT_PREV_AUDIO_METADATA;
                currAudio = playlist.getCurrentAudio();
                rxBus.post(PlayerEvents.EVENT_PREV_AUDIO_METADATA, getCurrentStateMap());
            } else {
                if (result) {
                    //nextEvent = PlayerEvents.EVENT_PREV_AUDIO_METADATA;
                    currAudio = playlist.getCurrentAudio();
                    rxBus.post(PlayerEvents.EVENT_PREV_AUDIO_METADATA, getCurrentStateMap());
                } else {
                    //nextEvent = PlayerEvents.EVENT_NONE;
                }
            }
            buildNotification(false, true);
            prepareToPlay();
        }
    }

    @Subscribe(tags = {@Tag(PlayerActions.ACTION_FAST_FORWARD)})
    public void fastForward(Integer forwardShift) {
        Log.i("PlayerService", "fastForward");
        exoPlayer.seekTo(exoPlayer.getCurrentPosition() + forwardShift);
        rxBus.post(PlayerEvents.EVENT_FAST_FORWARD, forwardShift);
        rxBus.post(PlayerEvents.EVENT_ON_POSITION_CHANGED, getAudioPosition());
    }

    @Subscribe(tags = {@Tag(PlayerActions.ACTION_REWIND)})
    public void fastRewind(Integer backwardShift) {
        Log.i("PlayerService", "fastRewind");
        exoPlayer.seekTo(exoPlayer.getCurrentPosition() - backwardShift);
        rxBus.post(PlayerEvents.EVENT_REWIND, backwardShift);
        rxBus.post(PlayerEvents.EVENT_ON_POSITION_CHANGED, getAudioPosition());
    }

    @Subscribe(tags = {@Tag(PlayerActions.ACTION_GET_CURR_AUDIO)})
    public void getCurrAudio(Object object) {
        rxBus.post(PlayerEvents.EVENT_ON_GET_CURR_AUDIO, getCurrAudio());
    }

    public Audio getCurrAudio() {
        Log.i("PlayerService", "getCurrAudio");
        return currAudio;
    }

    @Subscribe(tags = {@Tag(PlayerActions.ACTION_GET_AUDIO_SESSION_ID)})
    public void getAudioSessionId(Object object) {
        rxBus.post(PlayerEvents.EVENT_ON_GET_AUDIO_SESSION_ID, getAudioSessionId());
    }

    public int getAudioSessionId() {
        return exoPlayer.getAudioSessionId();
    }

    @Subscribe(tags = {@Tag(PlayerActions.ACTION_GET_AUDIO_DURATION)})
    public void getAudioDuration(Object object) {
        rxBus.post(PlayerEvents.EVENT_ON_GET_AUDIO_DURATION, getAudioDuration());
    }

    public int getAudioDuration() {
        Log.i("PlayerService", "getAudioDuration");
        return (int) exoPlayer.getDuration();
    }

    @Subscribe(tags = {@Tag(PlayerActions.ACTION_GET_AUDIO_POSITION)})
    public void getAudioPosition(Object object) {
        rxBus.post(PlayerEvents.EVENT_ON_GET_AUDIO_POSITION, getAudioPosition());
    }

    public int getAudioPosition() {
        return (int) exoPlayer.getCurrentPosition();
    }

    @Subscribe(tags = {@Tag(PlayerActions.ACTION_SEEK_TO)})
    public void seekTo(Integer pos) {
        Log.i("PlayerService", "seekTo");
        exoPlayer.seekTo(pos);
        rxBus.post(PlayerEvents.EVENT_ON_POSITION_CHANGED, pos);

    }

    @Subscribe(tags = {@Tag(PlayerActions.ACTION_CHANGE_RP_MODE)})
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
            rxBus.post(PlayerEvents.EVENT_RP_MODE_CHANGED, getRepeatMode());
        }
    }

    @Subscribe(tags = {@Tag(PlayerActions.ACTION_GET_RP_MODE)})
    public void getRepeatMode(Object object) {
        rxBus.post(PlayerEvents.EVENT_ON_GET_RP_MODE, getRepeatMode());
    }

    public int getRepeatMode() {
        Log.i("PlayerService", "getRepeatMode");
        if (playlist != null)
            return playlist.getRepeatMode();
        else
            return Playlist.NOT_REPEAT;
    }

    @Subscribe(tags = {@Tag(PlayerActions.ACTION_CHANGE_SHUFFLE_MODE)})
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
            rxBus.post(PlayerEvents.EVENT_SHUFFLED, true);
        }
    }

    public void unShufflePlaylist() {
        Log.i("PlayerService", "unShufflePlaylist");
        if (playlist != null) {
            playlist.unShuffle();
            rxBus.post(PlayerEvents.EVENT_UNSHUFFLED, false);
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
        becomingNoisyReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                pause();
            }
        };
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, filter);
    }

    private void setupCallStateListener() {
        Log.i("PlayerService", "setupCallStateListener");
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                Log.i("onCallStateChanged", String.valueOf(state));
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (isPlaying()) {
                            exoPlayer.setPlayWhenReady(false);
                            rxBus.post(PlayerEvents.EVENT_AUDIO_IS_PAUSED, false);
                        }
                        removeNotification();
                        ongoingCall = true;

                        break;

                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (ongoingCall) {
                            ongoingCall = false;
                            if (wasPlaying) {
                                exoPlayer.setPlayWhenReady(true);
                                rxBus.post(PlayerEvents.EVENT_AUDIO_IS_PLAYING, true);
                            }
                            buildNotification(wasPlaying, true);
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
        Context appContext = getApplicationContext();
        Intent activityIntent = new Intent(appContext, MainActivity.class);
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null, appContext, MediaButtonReceiver.class);

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);

        mediaSession = new MediaSession(getApplicationContext(), SERVICE_NAME);
        mediaSession.setSessionActivity(PendingIntent.getActivity(appContext, 0, activityIntent, 0));
        mediaSession.setMediaButtonReceiver(PendingIntent.getBroadcast(appContext, 0, mediaButtonIntent, 0));
        mediaSession.setCallback(new MediaSessionCallback(this));
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS | MediaSession.FLAG_HANDLES_MEDIA_BUTTONS);
        mediaSession.setActive(true);

        transportControls = mediaSession.getController().getTransportControls();
    }


    private void handleIncomingActions(Intent playbackAction) {
        Log.i("PlayerService", "handleIncomingActions");
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(PlayerActions.ACTION_PLAY_AUDIO) && !isPlaying()) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(PlayerActions.ACTION_PAUSE_AUDIO) && isPlaying()) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(PlayerActions.ACTION_NEXT_AUDIO)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(PlayerActions.ACTION_PREV_AUDIO)) {
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

            Intent intent = new Intent(this, MainActivity.class);

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
                playbackAction.setAction(PlayerActions.ACTION_PLAY_AUDIO);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(PlayerActions.ACTION_PAUSE_AUDIO);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction(PlayerActions.ACTION_NEXT_AUDIO);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction(PlayerActions.ACTION_PREV_AUDIO);
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
                    BitmapFactory.decodeResource(getResources(), R.drawable.default_album_art));

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

    private class PlayerAnalyticsListener extends Player.DefaultEventListener implements AnalyticsListener {

        @Override
        public void onPlayerStateChanged(EventTime eventTime, boolean playWhenReady, int playbackState) {
            switch (playbackState) {
                case Player.STATE_ENDED:
                    if (exoPlayer.getCurrentPosition() != 0)
                        playNextTrack();
                    break;

                case Player.STATE_READY:
                    Log.i("PlayerState", "Ready");
                    if (exoPlayer.getCurrentPosition() == 0) {
                        requestAudioFocus();
                        buildNotification(playWhenReady, true);
                        updateMetaData();
                        rxBus.post(PlayerEvents.EVENT_PLAYER_READY, getCurrentStateMap());
                        buildAndSetPlaybackState(playWhenReady);
                    }
                    break;

                case Player.STATE_BUFFERING:
                    Log.i("PlayerState", "Buffering");
                    rxBus.post(PlayerEvents.EVENT_PLAYER_PREPAIRING);
                    break;
            }
        }

        @Override
        public void onTimelineChanged(EventTime eventTime, int reason) {

        }

        @Override
        public void onPositionDiscontinuity(EventTime eventTime, int reason) {

        }

        @Override
        public void onSeekStarted(EventTime eventTime) {

        }

        @Override
        public void onSeekProcessed(EventTime eventTime) {

        }

        @Override
        public void onPlaybackParametersChanged(EventTime eventTime, PlaybackParameters playbackParameters) {

        }

        @Override
        public void onRepeatModeChanged(EventTime eventTime, int repeatMode) {

        }

        @Override
        public void onShuffleModeChanged(EventTime eventTime, boolean shuffleModeEnabled) {

        }

        @Override
        public void onLoadingChanged(EventTime eventTime, boolean isLoading) {
            Log.i("PlayerState", "onLoadingChanged " + String.valueOf(isLoading));
        }

        @Override
        public void onPlayerError(EventTime eventTime, ExoPlaybackException error) {

        }

        @Override
        public void onTracksChanged(EventTime eventTime, TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

        @Override
        public void onLoadStarted(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData) {
            Log.i("PlayerState", "onLoadStarted");
        }

        @Override
        public void onLoadCompleted(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData) {
            Log.i("PlayerState", "onLoadCompleted");
        }

        @Override
        public void onLoadCanceled(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData) {

        }

        @Override
        public void onLoadError(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData, IOException error, boolean wasCanceled) {

        }

        @Override
        public void onDownstreamFormatChanged(EventTime eventTime, MediaSourceEventListener.MediaLoadData mediaLoadData) {

        }

        @Override
        public void onUpstreamDiscarded(EventTime eventTime, MediaSourceEventListener.MediaLoadData mediaLoadData) {

        }

        @Override
        public void onMediaPeriodCreated(EventTime eventTime) {
            Log.i("PlayerState", "onMediaPeriodCreated");
        }

        @Override
        public void onMediaPeriodReleased(EventTime eventTime) {
            Log.i("PlayerState", "onMediaPeriodReleased");
        }

        @Override
        public void onReadingStarted(EventTime eventTime) {
            Log.i("PlayerState", "onReadingStarted");
        }

        @Override
        public void onBandwidthEstimate(EventTime eventTime, int totalLoadTimeMs, long totalBytesLoaded, long bitrateEstimate) {

        }

        @Override
        public void onViewportSizeChange(EventTime eventTime, int width, int height) {

        }

        @Override
        public void onNetworkTypeChanged(EventTime eventTime, @Nullable NetworkInfo networkInfo) {

        }

        @Override
        public void onMetadata(EventTime eventTime, Metadata metadata) {
            Log.i("PlayerState", "onMetadata " + metadata.toString());
        }

        @Override
        public void onDecoderEnabled(EventTime eventTime, int trackType, DecoderCounters decoderCounters) {

        }

        @Override
        public void onDecoderInitialized(EventTime eventTime, int trackType, String decoderName, long initializationDurationMs) {

        }

        @Override
        public void onDecoderInputFormatChanged(EventTime eventTime, int trackType, Format format) {

        }

        @Override
        public void onDecoderDisabled(EventTime eventTime, int trackType, DecoderCounters decoderCounters) {

        }

        @Override
        public void onAudioSessionId(EventTime eventTime, int audioSessionId) {
            Log.i("AudioSessionId", String.valueOf(audioSessionId));
            PlayerService.this.audioSessionId = audioSessionId;
            enableEqualizer();
        }

        @Override
        public void onAudioUnderrun(EventTime eventTime, int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {

        }

        @Override
        public void onDroppedVideoFrames(EventTime eventTime, int droppedFrames, long elapsedMs) {

        }

        @Override
        public void onVideoSizeChanged(EventTime eventTime, int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

        }

        @Override
        public void onRenderedFirstFrame(EventTime eventTime, Surface surface) {

        }

        @Override
        public void onDrmKeysLoaded(EventTime eventTime) {

        }

        @Override
        public void onDrmSessionManagerError(EventTime eventTime, Exception error) {

        }

        @Override
        public void onDrmKeysRestored(EventTime eventTime) {

        }

        @Override
        public void onDrmKeysRemoved(EventTime eventTime) {

        }
    }
}

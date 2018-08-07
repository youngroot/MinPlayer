package com.ivanroot.minplayer.fragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hwangjr.rxbus.Bus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.activity.MainActivity;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.player.constants.PlayerActions;
import com.ivanroot.minplayer.player.constants.PlayerEvents;
import com.ivanroot.minplayer.player.RxBus;
import com.ivanroot.minplayer.utils.Pair;
import com.ivanroot.minplayer.utils.Utils;
import static com.ivanroot.minplayer.player.constants.PlayerKeys.*;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.blurry.Blurry;


public class ControllerFragment extends Fragment {
    public static final String NAME = "ControllerFragment";

    private ProgressBar smallProgress;
    private ImageView smallAlbumArt;
    private TextView smallTitle;
    private TextView smallArtist;
    private TextView smallAlbum;
    private ImageButton smallPrevBtn;
    private ImageButton smallPlayBtn;
    private ImageButton smallNextBtn;
    private ViewGroup smallViewContainer;

    private SeekBar playbackProgress;
    private ImageView bigAlbumArt;
    private ImageView albumArt;
    private TextView title;
    private TextView artist;
    private TextView album;
    private ImageButton nextBtn;
    private ImageButton playBtn;
    private ImageButton prevBtn;
    private ImageButton shuffleBtn;
    private ImageButton repeatBtn;
    private ProgressBar loadProgress;
    private SlidingUpPanelLayout panelLayout;

    private int delayedTime = 1000;
    private Bus rxBus = RxBus.getInstance();
    private Disposable screenUpdater;
    private Audio currAudio;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rxBus.register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.i(toString(),"onCreateView");
        View view = inflater.inflate(R.layout.controller_fragment, container, false);
        prepareViews(view);
        prepareListeners();
        prepareScreenUpdater();

        if(savedInstanceState != null)
            applyAlphaToSmallViews(savedInstanceState.getFloat("smallAlpha",1));

        rxBus.post(PlayerActions.ACTION_GET_METADATA, this);
//        rxBus.post(ACTION_IS_PLAYING,this);
//        rxBus.post(ACTION_IS_SHUFFLED,this);
//        rxBus.post(ACTION_GET_RP_MODE,this);
        return view;
    }

    private void prepareViews(View view) {
        smallProgress = (ProgressBar) view.findViewById(R.id.smallProgress);
        smallAlbumArt = (ImageView) view.findViewById(R.id.smallAlbumArt);
        smallTitle = (TextView) view.findViewById(R.id.smallTitle);
        smallArtist = (TextView) view.findViewById(R.id.smallArtist);
        smallAlbum = (TextView) view.findViewById(R.id.smallAlbum);
        smallPrevBtn = (ImageButton) view.findViewById(R.id.smallPrevBtn);
        smallPlayBtn = (ImageButton) view.findViewById(R.id.smallPlayBtn);
        smallNextBtn = (ImageButton) view.findViewById(R.id.smallNextBtn);
        smallViewContainer = (ViewGroup) view.findViewById(R.id.small_controller_layout);

        playbackProgress = (SeekBar) view.findViewById(R.id.songProgress);
        loadProgress = (ProgressBar)view.findViewById(R.id.loadProgress);
        bigAlbumArt = (ImageView) view.findViewById(R.id.bigAlbumArt);
        albumArt = (ImageView) view.findViewById(R.id.albumArt);
        title = (TextView) view.findViewById(R.id.txtTitle);
        artist = (TextView) view.findViewById(R.id.txtArtist);
        album = (TextView) view.findViewById(R.id.txtAlbum);
        playBtn = (ImageButton) view.findViewById(R.id.playBtn);
        prevBtn = (ImageButton) view.findViewById(R.id.prevBtn);
        nextBtn = (ImageButton) view.findViewById(R.id.nextBtn);
        shuffleBtn = (ImageButton) view.findViewById(R.id.shuffleBtn);
        repeatBtn = (ImageButton) view.findViewById(R.id.repeatBtn);

        if(panelLayout == null && getActivity() instanceof MainActivity)
            panelLayout = ((MainActivity)getActivity()).getPanelLayout();
    }

    private void prepareListeners(){
        smallPrevBtn.setOnClickListener(view -> rxBus.post(PlayerActions.ACTION_PREV_AUDIO,this));
        smallPlayBtn.setOnClickListener(view -> rxBus.post(PlayerActions.ACTION_PLAY_OR_PAUSE,this));
        smallNextBtn.setOnClickListener(view -> rxBus.post(PlayerActions.ACTION_NEXT_AUDIO,this));

        playbackProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                rxBus.post(PlayerActions.ACTION_SEEK_TO, seekBar.getProgress());
            }
        });
        prevBtn.setOnClickListener(view -> rxBus.post(PlayerActions.ACTION_PREV_AUDIO,this));
        prevBtn.setOnLongClickListener(v -> true);
        playBtn.setOnClickListener(view -> rxBus.post(PlayerActions.ACTION_PLAY_OR_PAUSE,this));
        nextBtn.setOnClickListener(view -> rxBus.post(PlayerActions.ACTION_NEXT_AUDIO,this));
        nextBtn.setOnLongClickListener(v -> true);
        shuffleBtn.setOnClickListener(v -> rxBus.post(PlayerActions.ACTION_CHANGE_SHUFFLE_MODE, this));
        repeatBtn.setOnClickListener(v -> rxBus.post(PlayerActions.ACTION_CHANGE_RP_MODE, this));


    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if(panelLayout != null){
            panelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
                @Override
                public void onPanelSlide(View panel, float slideOffset) {
                    applyAlphaToSmallViews(1 - slideOffset);
                }

                @Override
                public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
//                    if(getActivity() instanceof MainActivity)
//                        if (newState == SlidingUpPanelLayout.PanelState.EXPANDED)
//                            getActivity()
//                                    .getWindow()
//                                    .setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//                                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//                        else getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                    if(currAudio == null)
                        ((SlidingUpPanelLayout)panel).setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                }
            });
        }

        Blurry.with(getActivity())
                .async()
                .sampling(10)
                .color(Color.argb(80, 60, 60, 60))
                .from(BitmapFactory.decodeResource(getResources(), R.drawable.lowpoly_grey))
                .into(bigAlbumArt);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i(toString(),"onSaveInstanceState");
        if(smallViewContainer != null) {
            outState.putFloat("smallAlpha", smallViewContainer.getAlpha());
            Log.i(toString(),"small alpha: " + String.valueOf(smallViewContainer.getAlpha()));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(screenUpdater != null)
            screenUpdater.dispose();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        rxBus.unregister(this);
    }

    public void setPanelLayout(SlidingUpPanelLayout panelLayout){
        this.panelLayout = panelLayout;
    }

    private void updateView(HashMap<String, Object> state){
        currAudio = (Audio) state.get(KEY_AUDIO);
        if(currAudio != null) {
            if(panelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN)
                panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            //smallProgress.setMax((int) state.get(KEY_DURATION));
            smallTitle.setText(currAudio.getTitle());
            smallAlbum.setText(currAudio.getAlbum());
            smallArtist.setText(currAudio.getArtist());

            //playbackProgress.setMax((int) state.get(KEY_DURATION));
            title.setText(currAudio.getTitle());
            album.setText(currAudio.getAlbum());
            artist.setText(currAudio.getArtist());

            //onPlayPauseEvents((boolean)state.get(KEY_IS_PLAYING));
            onShuffleModeEvents((boolean) state.get(KEY_IS_SHUFFLED));
            onRepeatModeEvents((int)state.get(KEY_RP_MODE));

            Bitmap bitmap = Utils.getAudioAlbumArt(currAudio.getAlbumArtPath(),
                    BitmapFactory.decodeResource(getResources(), R.drawable.lowpoly_grey));

            Picasso.with(getActivity())
                    .load(Utils.getFileFromPath(currAudio.getAlbumArtPath()))
                    .error(R.drawable.default_album_art)
                    .into(smallAlbumArt);

            Picasso.with(getActivity())
                    .load(Utils.getFileFromPath(currAudio.getAlbumArtPath()))
                    .error(R.drawable.default_album_art)
                    .into(albumArt);

            Blurry.with(getActivity())
                    .async()
                    .sampling(10)
                    .color(Color.argb(80, 60, 60, 60))
                    .from(bitmap)
                    .into(bigAlbumArt);
        }
        else {
            panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        }

    }

    private void applyAlphaToSmallViews(float smallAlpha){
        if(smallViewContainer != null)
            smallViewContainer.setAlpha(smallAlpha);

    }

    private void prepareScreenUpdater() {
        screenUpdater = Observable.<List<Pair<String, Object>>>create(emitter -> {
            while (true) {

                List<Pair<String, Object>> actions = new ArrayList<>();
                if (prevBtn.isPressed()) {
                    actions.add(new Pair<>(PlayerActions.ACTION_REWIND, 250));
                    delayedTime = 100;
                } else if (nextBtn.isPressed()) {
                    actions.add(new Pair<>(PlayerActions.ACTION_FAST_FORWARD, 250));
                    delayedTime = 100;
                } else {
                    delayedTime = 1000;
                }
                actions.add(new Pair<>(PlayerActions.ACTION_GET_AUDIO_POSITION, this));
                emitter.onNext(actions);
                try {
                    Thread.sleep(delayedTime);
                } catch (InterruptedException ex) {

                }
            }
        })
                .subscribeOn(Schedulers.newThread())
                .flatMap(Observable::fromIterable)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> rxBus.post(pair.first, pair.second));
    }

    @Subscribe(tags = {@Tag(PlayerEvents.EVENT_PLAYER_PREPAIRING)})
    public void onPlayerPrepairingEvent(Object o){
        onPlayPauseEvents(false);
        loadProgress.setVisibility(View.VISIBLE);
        smallProgress.setProgress(0);
        playbackProgress.setProgress(0);
    }

    @Subscribe(tags = {@Tag(PlayerEvents.EVENT_PLAYER_READY)})
    public void onPlayerReadyEvent(HashMap<String, Object> state){
        loadProgress.setVisibility(View.INVISIBLE);
        onPlayPauseEvents((boolean)state.get(KEY_IS_PLAYING));
        smallProgress.setMax((int)state.get(KEY_DURATION));
        playbackProgress.setMax((int)state.get(KEY_DURATION));
    }

    @Subscribe(tags = {
            @Tag(PlayerEvents.EVENT_METADATA_UPDATED), @Tag(PlayerEvents.EVENT_ON_GET_METADATA),
            @Tag(PlayerEvents.EVENT_PREV_AUDIO_METADATA), @Tag(PlayerEvents.EVENT_NEXT_AUDIO_METADATA)})
    public void onMetadataEvents(HashMap<String, Object> state) {
        updateView(state);
    }

    @Subscribe(tags = {@Tag (PlayerEvents.EVENT_AUDIO_IS_PLAYING), @Tag(PlayerEvents.EVENT_AUDIO_IS_PAUSED)})
    public void onPlayPauseEvents(Boolean isPlaying) {

        if (isPlaying) {
            smallPlayBtn.setImageResource(R.drawable.ic_pause_noti);
            playBtn.setImageResource(R.drawable.ic_pause);
        } else {
            smallPlayBtn.setImageResource(R.drawable.ic_play_noti);
            playBtn.setImageResource(R.drawable.ic_play);
        }
    }

    @Subscribe(tags = {@Tag(PlayerEvents.EVENT_ON_GET_AUDIO_POSITION)})
    public void onPositionChangedEvents(Integer pos) {
        smallProgress.setProgress(pos);
        playbackProgress.setProgress(pos);
    }

    @Subscribe(tags = {@Tag(PlayerEvents.EVENT_SHUFFLED), @Tag(PlayerEvents.EVENT_UNSHUFFLED)})
    public void onShuffleModeEvents(Boolean isShuffled) {
        if (isShuffled) {
            shuffleBtn.setImageResource(R.drawable.ic_shuffle);
        } else {
            shuffleBtn.setImageResource(R.drawable.ic_shuffle_disabled);
        }
    }

    @Subscribe(tags = {@Tag(PlayerEvents.EVENT_RP_MODE_CHANGED), @Tag(PlayerEvents.EVENT_ON_GET_RP_MODE)})
    public void onRepeatModeEvents(Integer repeatMode) {
        switch (repeatMode) {
            case 0:
                repeatBtn.setImageResource(R.drawable.ic_repeat_off);
                break;
            case 1:
                repeatBtn.setImageResource(R.drawable.ic_repeat);
                break;
            case 2:
                repeatBtn.setImageResource(R.drawable.ic_repeat_once);
        }
    }
}

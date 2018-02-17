package com.ivanroot.minplayer.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hwangjr.rxbus.Bus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.activity.PlayerActivity;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.player.RxBus;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static com.ivanroot.minplayer.player.PlayerActionsEvents.ACTION_GET_AUDIO_POSITION;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.ACTION_GET_METADATA;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.ACTION_NEXT_AUDIO;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.ACTION_PLAY_OR_PAUSE;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.ACTION_PREV_AUDIO;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.EVENT_AUDIO_IS_PAUSED;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.EVENT_AUDIO_IS_PLAYING;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.EVENT_METADATA_UPDATED;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.EVENT_NEXT_AUDIO_METADATA;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.EVENT_ON_GET_AUDIO_POSITION;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.EVENT_ON_GET_METADATA;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.EVENT_ON_POSITION_CHANGED;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.EVENT_PREV_AUDIO_METADATA;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.KEY_AUDIO;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.KEY_DURATION;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.KEY_IS_PLAYING;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.KEY_POSITION;

/**
 * Created by Ivan Root on 03.07.2017.
 */

public class ControllerFragment extends Fragment {

    private ImageView albumArt;
    private TextView title;
    private TextView album;
    private TextView artist;
    private ProgressBar songProgress;
    private ImageButton playBtn;
    private ImageButton nextBtn;
    private ImageButton prevBtn;
    private Animation botTop;
    private Animation topBotDiss;
    private static final int animDuration = 300;
    private ViewGroup container;
    private Bus rxBus = RxBus.getInstance();
    private Disposable updateProgressDisposable;


    public ControllerFragment() {
        super();
    }

    public static ControllerFragment newInstance() {
        ControllerFragment controllerFragment = new ControllerFragment();
        return controllerFragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        Handler updater = new Handler();
        rxBus.register(this);
        initAnimation();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.media_controller, container, false);
        this.container = container;
        prepareViews(view);
        prepareListeners(view);
        updateProgressDisposable = Observable.interval(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(i -> RxBus.getInstance().post(ACTION_GET_AUDIO_POSITION, this));
        rxBus.post(ACTION_GET_METADATA,this);

        return view;
    }

    private void prepareViews(View view) {

        albumArt = (ImageView) view.findViewById(R.id.songAlbumArt);
        title = (TextView) view.findViewById(R.id.songTitle);
        album = (TextView) view.findViewById(R.id.songAlbum);
        artist = (TextView) view.findViewById(R.id.songArtist);
        songProgress = (ProgressBar) view.findViewById(R.id.songProgress);
        playBtn = (ImageButton) view.findViewById(R.id.playBtn);
        prevBtn = (ImageButton) view.findViewById(R.id.prevBtn);
        nextBtn = (ImageButton) view.findViewById(R.id.nextBtn);
    }

    private void prepareListeners(View view) {

        view.setOnClickListener(v -> getActivity().startActivity(new Intent(getActivity(), PlayerActivity.class)));
        playBtn.setOnClickListener(v -> rxBus.post(ACTION_PLAY_OR_PAUSE, this));
        prevBtn.setOnClickListener(v -> rxBus.post(ACTION_PREV_AUDIO, this));
        nextBtn.setOnClickListener(v -> rxBus.post(ACTION_NEXT_AUDIO, this));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (updateProgressDisposable != null)
            updateProgressDisposable.dispose();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        rxBus.unregister(this);
    }

    private void updateView(HashMap<String, Object> state, @NonNull ViewGroup container) {

        Audio currAudio = (Audio) state.get(KEY_AUDIO);
        if (currAudio != null) {
            songProgress.setMax((int) state.get(KEY_DURATION));
            songProgress.setProgress((int)state.get(KEY_POSITION));
            title.setText(currAudio.getTitle());
            album.setText(currAudio.getAlbum());
            artist.setText(currAudio.getArtist());

            try {
                Picasso.with(getActivity())
                        .load(new File(currAudio.getAlbumArt()))
                        .error(R.drawable.default_album_art)
                        .into(albumArt);
            }catch (NullPointerException ex){
                albumArt.setImageResource(R.drawable.default_album_art);
            }

            if (!(boolean) state.get(KEY_IS_PLAYING)) {
                playBtn.setImageResource(R.drawable.ic_play_noti);
            } else {
                playBtn.setImageResource(R.drawable.ic_pause_noti);
            }

            if (container.getVisibility() == View.GONE) {
                container.setVisibility(View.VISIBLE);
                container.startAnimation(botTop);
            }
        } else {
            if (container.getVisibility() == View.VISIBLE) {
                container.startAnimation(topBotDiss);
                container.setVisibility(View.GONE);
            }
        }

    }

    @Subscribe(tags = {
            @Tag(EVENT_METADATA_UPDATED),
            @Tag(EVENT_ON_GET_METADATA),
            @Tag(EVENT_NEXT_AUDIO_METADATA),
            @Tag(EVENT_PREV_AUDIO_METADATA)
    })
    public void onMetadataEvents(HashMap<String, Object> state) {
        updateView(state, container);
    }

    @Subscribe(tags = {@Tag(EVENT_AUDIO_IS_PLAYING), @Tag(EVENT_AUDIO_IS_PAUSED)})
    public void onPlayPauseEvents(Boolean isPlaying) {
        if (isPlaying)
            playBtn.setImageResource(R.drawable.ic_pause_noti);
        else
            playBtn.setImageResource(R.drawable.ic_play_noti);
    }

    @Subscribe(tags = {@Tag(EVENT_ON_POSITION_CHANGED),@Tag(EVENT_ON_GET_AUDIO_POSITION)})
    public void onPositionChangedEvents(Integer pos) {
        songProgress.setProgress(pos);
    }

    private void initAnimation() {

        botTop = AnimationUtils.loadAnimation(getActivity(), R.anim.bot_top);
        topBotDiss = AnimationUtils.loadAnimation(getActivity(), R.anim.top_bot_dissapear);
        botTop.setDuration(animDuration);
        topBotDiss.setDuration(animDuration);
    }

}

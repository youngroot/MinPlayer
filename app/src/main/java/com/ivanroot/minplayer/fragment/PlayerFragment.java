package com.ivanroot.minplayer.fragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hwangjr.rxbus.Bus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.player.RxBus;
import com.ivanroot.minplayer.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.blurry.Blurry;

import static com.ivanroot.minplayer.player.PlayerActionsEvents.ACTION_CHANGE_RP_MODE;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.ACTION_CHANGE_SHUFFLE_MODE;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.ACTION_FAST_FORWARD;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.ACTION_GET_AUDIO_POSITION;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.ACTION_GET_METADATA;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.ACTION_NEXT_AUDIO;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.ACTION_PLAY_OR_PAUSE;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.ACTION_PREV_AUDIO;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.ACTION_REWIND;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.ACTION_SEEK_TO;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.EVENT_AUDIO_IS_PAUSED;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.EVENT_AUDIO_IS_PLAYING;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.EVENT_METADATA_UPDATED;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.EVENT_NEXT_AUDIO_METADATA;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.EVENT_ON_GET_AUDIO_POSITION;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.EVENT_ON_GET_METADATA;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.EVENT_ON_GET_RP_MODE;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.EVENT_PREV_AUDIO_METADATA;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.EVENT_RP_MODE_CHANGED;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.EVENT_SHUFFLED;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.EVENT_UNSHUFFLED;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.KEY_AUDIO;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.KEY_DURATION;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.KEY_IS_PLAYING;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.KEY_IS_SHUFFLED;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.KEY_RP_MODE;

/**
 * Created by Ivan Root on 10.06.2017.
 */

public class PlayerFragment extends Fragment {

    private ImageView albumArt;
    private ImageView bigAlbumArt;
    private SeekBar songProgress;
    private TextView title;
    private TextView album;
    private TextView artist;
    private TextView secNow;
    private TextView secLeft;
    private ImageButton closeBtn;
    private ImageButton playBtn;
    private ImageButton nextBtn;
    private ImageButton prevBtn;
    private ImageButton shuffleBtn;
    private ImageButton repeatBtn;
    private Animation zoomIn;
    private Animation zoomOut;
    private Animation leftRight;
    private Animation rightLeft;
    private Animation bounce;
    private Animation fadeIn;
    private Animation fadeOut;
    private Disposable screenUpdater;
    private Bus rxBus = RxBus.getInstance();
    private int delayedTime = 1000;
    private static final int animDuration = 500;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rxBus.register(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.player_fragment_layout, container, false);
        prepareViews(view);
        prepareListeners();
        initAnimations();
        prepareScreenUpdater();
        rxBus.post(ACTION_GET_METADATA,this);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(screenUpdater != null)
            screenUpdater.dispose();
    }

    private void prepareScreenUpdater() {

        screenUpdater = Observable.<List<Pair<String,Object>>>create(emitter ->{
            while (true) {

                List<Pair<String,Object>> actions = new ArrayList<>();
                if (prevBtn.isPressed()) {
                    actions.add(new Pair<>(ACTION_REWIND,250));
                    delayedTime = 100;
                } else if (nextBtn.isPressed()) {
                    actions.add(new Pair<>(ACTION_FAST_FORWARD,250));
                    delayedTime = 100;
                } else {
                    delayedTime = 1000;
                }
                actions.add(new Pair<>(ACTION_GET_AUDIO_POSITION,this));
                emitter.onNext(actions);
                try {
                    Thread.sleep(delayedTime);
                }catch (InterruptedException ex){

                }
            }
        })
                .subscribeOn(Schedulers.newThread())
                .flatMap(Observable::fromIterable)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> rxBus.post(pair.first,pair.second));
    }

    private void prepareListeners() {
        playBtn.setOnClickListener(v -> rxBus.post(ACTION_PLAY_OR_PAUSE,this));
        prevBtn.setOnClickListener(v -> rxBus.post(ACTION_PREV_AUDIO,this));
        prevBtn.setOnLongClickListener(v -> true);
        nextBtn.setOnClickListener(v -> rxBus.post(ACTION_NEXT_AUDIO,true));
        nextBtn.setOnLongClickListener(v -> true);
        shuffleBtn.setOnClickListener(v -> rxBus.post(ACTION_CHANGE_SHUFFLE_MODE,this));
        repeatBtn.setOnClickListener(v -> rxBus.post(ACTION_CHANGE_RP_MODE,this));
        songProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int position, boolean fromUser) {

                int duration = seekBar.getMax();
                //secNow.setText(formatTime(position));
                //secLeft.setText("-" + formatTime(duration - position));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                rxBus.post(ACTION_SEEK_TO,seekBar.getProgress());
            }
        });
    }

    @Subscribe(tags = {@Tag(EVENT_ON_GET_AUDIO_POSITION)})
    public void onPositionChangedEvents(Integer pos) {
        songProgress.setProgress(pos);
    }



    private void prepareViews(View view) {
        albumArt = (ImageView) view.findViewById(R.id.albumArt);
        bigAlbumArt = (ImageView) view.findViewById(R.id.bigAlbumArt);
        songProgress = (SeekBar) view.findViewById(R.id.songProgress);

        title = (TextView) view.findViewById(R.id.txtTitle);
        album = (TextView) view.findViewById(R.id.txtAlbum);
        artist = (TextView) view.findViewById(R.id.txtArtist);

        secNow = (TextView) view.findViewById(R.id.secNow);
        secLeft = (TextView) view.findViewById(R.id.secLeft);

        closeBtn = (ImageButton) view.findViewById(R.id.closeBtn);
        playBtn = (ImageButton) view.findViewById(R.id.playBtn);
        prevBtn = (ImageButton) view.findViewById(R.id.prevBtn);
        nextBtn = (ImageButton) view.findViewById(R.id.nextBtn);

        shuffleBtn = (ImageButton) view.findViewById(R.id.shuffleBtn);
        repeatBtn = (ImageButton) view.findViewById(R.id.repeatBtn);

        closeBtn.setOnClickListener(v -> getActivity().onBackPressed());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        rxBus.unregister(this);
    }

    public static PlayerFragment newInstance() {

        PlayerFragment playerFragment = new PlayerFragment();
        return playerFragment;
    }


    private void updateView(HashMap<String,Object> state) {

        Audio currAudio = (Audio) state.get(KEY_AUDIO);

        songProgress.setMax((int) state.get(KEY_DURATION));

        title.setText(currAudio.getTitle());
        album.setText(currAudio.getAlbum());
        artist.setText(currAudio.getArtist());


        /*Bitmap bitmap = Utils.extractAudioAlbumArt(currAudio.getData());
        if (bitmap != null)
            albumArt.setImageBitmap(bitmap);
        else
            albumArt.setImageResource(R.drawable.default_album_art);
        */

        /*
        Palette palette = Palette.generate(bmp);

        int bgColor = palette.getDarkVibrantColor(palette.getVibrantColor(Color.WHITE));
        int elementColor = palette.getVibrantColor((Color.WHITE));

        if(elementColor == bgColor){
            bgColor = palette.getDominantColor(Color.WHITE);
            if(elementColor == Color.WHITE){
                if(bgColor == elementColor)
                    elementColor = Color.BLACK;
            }
            elementColor = palette.getDarkVibrantColor(Color.BLACK);
        }
        */

        Bitmap bitmap = Utils.getAudioAlbumArt(currAudio.getAlbumArt(),
                BitmapFactory.decodeResource(getResources(),R.drawable.lowpoly_grey));

        try {
            Picasso.with(getActivity())
                    .load(new File(currAudio.getAlbumArt()))
                    .error(R.drawable.default_album_art)
                    .into(albumArt);
        }catch (NullPointerException ex){
            albumArt.setImageResource(R.drawable.default_album_art);
        }

        Blurry.with(getActivity())
                .async()
                .sampling(10)
                .color(Color.argb(80,60,60,60))
                .from(bitmap)
                .into(bigAlbumArt);

    }

    @Subscribe(tags = {@Tag(EVENT_METADATA_UPDATED), @Tag(EVENT_ON_GET_METADATA)})
    public void onMetadataEvents(HashMap<String,Object> state) {
            updateView(state);
            firstAnimation(state);
    }

    @Subscribe(tags = {@Tag(EVENT_NEXT_AUDIO_METADATA)})
    public void onNextAudioEvent(HashMap<String,Object> state) {

            albumArt.startAnimation(fadeOut);
            secNow.startAnimation(fadeOut);
            secLeft.startAnimation(fadeOut);
            title.startAnimation(fadeOut);
            album.startAnimation(fadeOut);
            artist.startAnimation(fadeOut);
            updateView(state);
            albumArt.startAnimation(fadeIn);
            secNow.startAnimation(fadeIn);
            secLeft.startAnimation(fadeIn);
            title.startAnimation(rightLeft);
            album.startAnimation(rightLeft);
            artist.startAnimation(rightLeft);

    }

    @Subscribe(tags = {@Tag(EVENT_PREV_AUDIO_METADATA)})
    public void onPrevAudioEvent(HashMap<String,Object> state) {

            albumArt.startAnimation(fadeOut);
            secNow.startAnimation(fadeOut);
            secLeft.startAnimation(fadeOut);
            title.startAnimation(fadeOut);
            album.startAnimation(fadeOut);
            artist.startAnimation(fadeOut);
            updateView(state);
            albumArt.startAnimation(fadeIn);
            secNow.startAnimation(fadeIn);
            secLeft.startAnimation(fadeIn);
            title.startAnimation(leftRight);
            album.startAnimation(leftRight);
            artist.startAnimation(leftRight);

    }

    @Subscribe(tags = {@Tag(EVENT_AUDIO_IS_PLAYING), @Tag(EVENT_AUDIO_IS_PAUSED)})
    public void onPlayPauseEvents(Boolean isPlaying) {

        if (isPlaying) {
            albumArt.startAnimation(zoomIn);
            playBtn.setImageResource(R.drawable.ic_pause);
        }else {
            albumArt.startAnimation(zoomOut);
            playBtn.setImageResource(R.drawable.ic_play);
        }
    }

    @Subscribe(tags = {@Tag(EVENT_SHUFFLED),@Tag(EVENT_UNSHUFFLED)})
    public void onShuffleModeEvents(Boolean isShuffled) {
        if (isShuffled) {
            shuffleBtn.setImageResource(R.drawable.ic_shuffle);
        } else {
            shuffleBtn.setImageResource(R.drawable.ic_shuffle_disabled);
        }
    }

    @Subscribe(tags = {@Tag(EVENT_RP_MODE_CHANGED),@Tag(EVENT_ON_GET_RP_MODE)})
    public void rpModeActions(Integer repeatMode) {
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

    private void initAnimations() {

        zoomIn = AnimationUtils.loadAnimation(getActivity(), R.anim.zoom_in);
        zoomOut = AnimationUtils.loadAnimation(getActivity(), R.anim.zoom_out);
        leftRight = AnimationUtils.loadAnimation(getActivity(), R.anim.left_right);
        rightLeft = AnimationUtils.loadAnimation(getActivity(), R.anim.right_left);
        bounce = AnimationUtils.loadAnimation(getActivity(), R.anim.bounce);
        fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);

        zoomIn.setDuration(animDuration);
        zoomOut.setDuration(animDuration);
        leftRight.setDuration(animDuration);
        rightLeft.setDuration(animDuration);
        bounce.setDuration(animDuration);
        fadeOut.setDuration(animDuration);
        fadeIn.setDuration(animDuration);

    }

    private void firstAnimation(HashMap<String,Object> state) {

            onPlayPauseEvents((boolean)state.get(KEY_IS_PLAYING));
            onShuffleModeEvents((boolean)state.get(KEY_IS_SHUFFLED));
            rpModeActions((int)state.get(KEY_RP_MODE));
            secNow.startAnimation(leftRight);
            secLeft.startAnimation(rightLeft);
            songProgress.startAnimation(bounce);
            title.startAnimation(leftRight);
            album.startAnimation(rightLeft);
            artist.startAnimation(leftRight);
            closeBtn.startAnimation(bounce);
            playBtn.startAnimation(bounce);
            prevBtn.startAnimation(leftRight);
            nextBtn.startAnimation(rightLeft);
            shuffleBtn.startAnimation(leftRight);
            repeatBtn.startAnimation(rightLeft);

    }


    private String formatTime(int millis) {

        int sec = (millis / 1000) % 60;
        int min = (millis / 1000) / 60;

        String Sec = "";
        String Min = "";

        if (sec < 10) Sec += "0";
        Sec += String.valueOf(sec);
        Min += String.valueOf(min);

        return Min + ":" + Sec;
    }

}

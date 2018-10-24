package com.ivanroot.minplayer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cleveroad.audiovisualization.AudioVisualization;
import com.cleveroad.audiovisualization.DbmHandler;
import com.cleveroad.audiovisualization.GLAudioVisualizationView;
import com.cleveroad.audiovisualization.VisualizerDbmHandler;
import com.hwangjr.rxbus.Bus;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.player.constants.PlayerActions;
import com.ivanroot.minplayer.player.constants.PlayerEvents;

public class WaveInFragment extends Fragment {

    private ViewGroup visHolder;
    private GLAudioVisualizationView visualizationView;
    private VisualizerDbmHandler dbmHandler;
    private Bus rxBus = RxBus.get();

    public WaveInFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rxBus.register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        visualizationView = new GLAudioVisualizationView.Builder(getActivity())
                .setBubblesSize(R.dimen.bubble_size)
                .setBubblesRandomizeSize(true)
                .setWavesHeight(R.dimen.wave_height)
                .setWavesFooterHeight(R.dimen.footer_height)
                .setWavesCount(7)
                .setLayersCount(3)
                .setBackgroundColorRes(R.color.av_color_bg)
                .setLayerColors(R.array.av_colors)
                .setBubblesPerLayer(16)
                .build();

        return visualizationView;
    }

    @Subscribe(tags = {@Tag(PlayerEvents.EVENT_ON_GET_AUDIO_SESSION_ID)})
    public void onAudioSessionId(Integer audioSessionId) {
        dbmHandler = DbmHandler.Factory.newVisualizerHandler(getActivity(), audioSessionId);
        visualizationView.linkTo(dbmHandler);
        visualizationView.onResume();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(visualizationView != null)
            visualizationView.onResume();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rxBus.post(PlayerActions.ACTION_GET_AUDIO_SESSION_ID, this);
    }

    @Override
    public void onPause() {
        if (visualizationView != null)
            visualizationView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        rxBus.unregister(this);
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        if (visualizationView != null)
            visualizationView.release();
        super.onDestroyView();
    }

}

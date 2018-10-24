package com.ivanroot.minplayer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cleveroad.audiovisualization.DbmHandler;
import com.cleveroad.audiovisualization.GLAudioVisualizationView;
import com.cleveroad.audiovisualization.SpeechRecognizerDbmHandler;
import com.ivanroot.minplayer.R;

public class WaveInRecorderFragment extends Fragment {

    private GLAudioVisualizationView visualizationView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SpeechRecognizerDbmHandler speechRecHandler = DbmHandler.Factory.newSpeechRecognizerHandler(getContext());
        //speechRecHandler.innerRecognitionListener();
        visualizationView.linkTo(speechRecHandler);
    }


    @Override
    public void onResume() {
        super.onResume();
        visualizationView.onResume();
    }

    @Override
    public void onPause() {
        visualizationView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        visualizationView.release();
        super.onDestroyView();
    }
}

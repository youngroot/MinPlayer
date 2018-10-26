package com.ivanroot.minplayer.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cleveroad.audiovisualization.DbmHandler;
import com.cleveroad.audiovisualization.GLAudioVisualizationView;
import com.cleveroad.audiovisualization.SpeechRecognizerDbmHandler;
import com.ivanroot.minplayer.R;

public class WaveInRecognizerFragment extends Fragment {

    private GLAudioVisualizationView visualizationView;
    private SpeechRecognizerDbmHandler dbmHandler;

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
        dbmHandler = DbmHandler.Factory.newSpeechRecognizerHandler(getContext());
        dbmHandler.innerRecognitionListener(new SimpleRecognitionListener());
        visualizationView.linkTo(dbmHandler);
        dbmHandler.startListening(getRecognizerIntent());

    }

    private Intent getRecognizerIntent(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getContext().getPackageName());
        return intent;
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

    private static class SimpleRecognitionListener implements RecognitionListener{
        @Override
        public void onReadyForSpeech(Bundle bundle) {

        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float v) {

        }

        @Override
        public void onBufferReceived(byte[] bytes) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int i) {

        }

        @Override
        public void onResults(Bundle bundle) {

        }

        @Override
        public void onPartialResults(Bundle bundle) {

        }

        @Override
        public void onEvent(int i, Bundle bundle) {

        }
    }
}

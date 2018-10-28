package com.ivanroot.minplayer.fragment;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.cleveroad.audiovisualization.DbmHandler;
import com.hwangjr.rxbus.Bus;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.player.constants.PlayerActions;
import com.ivanroot.minplayer.player.constants.PlayerEvents;

import me.bogerchan.niervisualizer.NierVisualizerManager;
import me.bogerchan.niervisualizer.renderer.IRenderer;
import me.bogerchan.niervisualizer.renderer.circle.CircleBarRenderer;
import me.bogerchan.niervisualizer.renderer.circle.CircleRenderer;
import me.bogerchan.niervisualizer.renderer.circle.CircleSolidRenderer;
import me.bogerchan.niervisualizer.renderer.circle.CircleWaveRenderer;
import me.bogerchan.niervisualizer.renderer.columnar.ColumnarType1Renderer;
import me.bogerchan.niervisualizer.renderer.columnar.ColumnarType3Renderer;
import me.bogerchan.niervisualizer.renderer.columnar.ColumnarType4Renderer;
import me.bogerchan.niervisualizer.renderer.line.LineRenderer;
import me.bogerchan.niervisualizer.renderer.other.ArcStaticRenderer;

public class NierFragment extends Fragment {
    private NierVisualizerManager visualizerManager = new NierVisualizerManager();
    private SurfaceView surfaceView;
    private IRenderer[] renderers = new IRenderer[]{
            //new CircleSolidRenderer(),
            new CircleBarRenderer(),
            //new LineRenderer(true)
            new CircleWaveRenderer(),
    };
    private Bus rxBus = RxBus.get();
    private boolean initialized = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rxBus.register(this);
    }

    @Nullable
    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        surfaceView = (SurfaceView) inflater.inflate(R.layout.nier_fragment_layout, container, false)
                .findViewById(R.id.surface_view);
        return surfaceView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rxBus.post(PlayerActions.ACTION_GET_AUDIO_SESSION_ID, this);
    }

    @Subscribe(tags = {@Tag(PlayerEvents.EVENT_ON_GET_AUDIO_SESSION_ID)})
    public void onAudioSessionId(Integer audioSessionId) {
        visualizerManager.init(audioSessionId);
        initialized = true;
        visualizerManager.start(surfaceView, renderers);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (initialized)
            visualizerManager.start(surfaceView, renderers);
    }

    @Override
    public void onPause() {
        if (initialized)
            visualizerManager.stop();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        visualizerManager.stop();
        visualizerManager.release();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        rxBus.unregister(this);
    }
}

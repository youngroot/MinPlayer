package com.ivanroot.minplayer.visualization;

import android.content.Context;
import android.graphics.PixelFormat;
import android.media.audiofx.Visualizer;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Ivan Root on 03.11.2017.
 */

public class VisualizerSurface extends SurfaceView implements SurfaceHolder.Callback {

    private Disposable visualizerSubscription;
    private Visualizer mVisualizer;
    private byte[] fft;
    private BaseVisualization baseVisualization;

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mVisualizer.release();
    }

    public VisualizerSurface(Context context, int audioSessionId, BaseVisualization baseVisualization) {
        super(context);
        getHolder().addCallback(this);
        this.baseVisualization = baseVisualization;
        mVisualizer = new Visualizer(audioSessionId);
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        fft = new byte[mVisualizer.getCaptureSize()];
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        visualizerSubscription = Observable.interval(16, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.newThread())
                .doOnSubscribe(d -> mVisualizer.setEnabled(true))
                .doOnDispose(() -> mVisualizer.setEnabled(false))
                .map(tick -> {
                    mVisualizer.getFft(fft);
                    return baseVisualization
                                    .setFftData(fft)
                                    .setWidth(getWidth())
                                    .setHeight(getHeight())
                                    .visualize(surfaceHolder.lockCanvas());

                })
                .subscribe(surfaceHolder::unlockCanvasAndPost,
                        error -> Log.e("Error!",error.getMessage()));
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if(visualizerSubscription != null)
            visualizerSubscription.dispose();
    }
}

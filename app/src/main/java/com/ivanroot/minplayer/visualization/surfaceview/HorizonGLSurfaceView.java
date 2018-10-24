package com.ivanroot.minplayer.visualization.surfaceview;

import android.content.Context;
import android.media.audiofx.Visualizer;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.SurfaceHolder;

import com.yalantis.waves.util.Horizon;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class HorizonGLSurfaceView extends GLSurfaceView {
    private Disposable visualizerSubscription;
    private Horizon horizon;
    private Visualizer visualizer;
    private byte[] fft;

    public HorizonGLSurfaceView(Context context, int audioSessionId, int backgroundColor) {
        super(context);
        visualizer = new Visualizer(audioSessionId);
        visualizer.setEnabled(false);
        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        fft = new byte[visualizer.getCaptureSize()];

        int sampleRate = 44100;
        int bitPerSample = 16;

        horizon = new Horizon(this, backgroundColor, sampleRate, 2, bitPerSample);
        horizon.setMaxVolumeDb(120);

    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        visualizer.release();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        visualizerSubscription = Observable.interval(160, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.newThread())
                .doOnSubscribe(d -> visualizer.setEnabled(true))
                .doOnDispose(() -> visualizer.setEnabled(false))
                .doOnNext(tick -> {
                    visualizer.getFft(fft);
                    String s = "fft data: ";
                    for(int i = 0; i < fft.length; i++)
                        s += String.valueOf(fft[i]);

                    Log.i("FFT", s);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tick -> horizon.updateView(fft),
                        error -> Log.e("Error!",error.getMessage()));

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        super.surfaceDestroyed(surfaceHolder);
        if(visualizerSubscription != null)
            visualizerSubscription.dispose();
    }

}
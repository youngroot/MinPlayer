package com.ivanroot.minplayer.visualization.wallpapers;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.audiofx.Visualizer;
import android.opengl.GLSurfaceView;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.ivanroot.minplayer.R;
import com.yalantis.waves.util.Horizon;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HorizonWallpaperService extends WallpaperServiceBase {
    private int sampleRate;

    @Override
    public void onCreate() {
        super.onCreate();
        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        sampleRate = Integer.valueOf(audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE));
    }

    @Override
    public Engine onCreateEngine() {
        return new WallpaperEngine();
    }

    private class WallpaperEngine extends WallpaperEngineBase {
        private Horizon horizon;
        private WallpaperGLSurfaceView glSurfaceView;
        private Visualizer visualizer;
        private byte[] fft;
        private Disposable disposable;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            glSurfaceView = new WallpaperGLSurfaceView(HorizonWallpaperService.this);

            visualizer = new Visualizer(0);
            visualizer.setEnabled(false);
            visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

            fft = new byte[visualizer.getCaptureSize()];

            horizon = new Horizon(glSurfaceView, getResources().getColor(R.color.colorHorizonBgDefault), sampleRate, 1, 16);

            disposable = rxPreferences.getInteger("horizon_frame_update_ms", 45)
                    .asObservable()
                    .observeOn(Schedulers.newThread())
                    .switchMap(ms -> Observable.interval(ms, TimeUnit.MILLISECONDS)
                            .doOnSubscribe(i -> visualizer.setEnabled(true))
                            .doOnDispose(() -> visualizer.release())
                            .doOnNext(i -> visualizer.getFft(fft)))
                    .subscribe(i -> horizon.updateView(fft));
        }

        @Override
        public void onDestroy() {
            disposable.dispose();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                glSurfaceView.onResume();
            } else {
                glSurfaceView.onPause();
            }
        }
    }
}

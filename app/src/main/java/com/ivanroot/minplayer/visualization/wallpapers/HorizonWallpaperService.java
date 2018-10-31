package com.ivanroot.minplayer.visualization.wallpapers;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.audiofx.Visualizer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;

import com.ivanroot.minplayer.R;
import com.yalantis.waves.util.Horizon;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
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

    public static class RestarterService extends Service {
        private Disposable disposable;

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            disposable = Completable.create(emitter -> {
                stopService(new Intent(this, HorizonWallpaperService.class));
                Log.i(toString(), "Started");
                Thread.sleep(500);
                emitter.onComplete();
            }).subscribeOn(Schedulers.newThread())
                    .doOnComplete(() -> startService(new Intent(this, HorizonWallpaperService.class)))
                    .doOnDispose(this::stopSelf)
                    .subscribe();

            return super.onStartCommand(intent, flags, startId);
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
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
        private Disposable msDisposable;
        private Disposable colorDisposable;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            glSurfaceView = new WallpaperGLSurfaceView(HorizonWallpaperService.this);

            visualizer = new Visualizer(0);
            visualizer.setEnabled(false);
            visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

            fft = new byte[visualizer.getCaptureSize()];

            int defaultBgColor = sharedPreferences.getInt("horizon_background_color", getResources().getColor(R.color.colorHorizonBgDefault));

            horizon = new Horizon(glSurfaceView, defaultBgColor, sampleRate, 1, 16);

            msDisposable = rxPreferences.getInteger("horizon_frame_update_ms", 45)
                    .asObservable()
                    .observeOn(Schedulers.newThread())
                    .switchMap(ms -> Observable.interval(ms, TimeUnit.MILLISECONDS)
                            .doOnSubscribe(i -> visualizer.setEnabled(true))
                            .doOnDispose(() -> visualizer.release())
                            .doOnNext(i -> visualizer.getFft(fft)))
                    .subscribe(i -> horizon.updateView(fft));

            colorDisposable = rxPreferences.getInteger("horizon_background_color")
                    .asObservable()
                    .subscribe(i -> startService(new Intent(HorizonWallpaperService.this, RestarterService.class)));
        }

        @Override
        public void onDestroy() {
            msDisposable.dispose();
            colorDisposable.dispose();
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

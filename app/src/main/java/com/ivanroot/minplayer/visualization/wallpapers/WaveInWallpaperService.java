package com.ivanroot.minplayer.visualization.wallpapers;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLSurfaceView;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import com.cleveroad.audiovisualization.DbmHandler;
import com.cleveroad.audiovisualization.GLAudioVisualizationView;
import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.ivanroot.minplayer.R;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class WaveInWallpaperService extends WallpaperServiceBase {

    @Override
    public Engine onCreateEngine() {
        return new WallpaperEngine();
    }

    private class WallpaperEngine extends WallpaperEngineBase {
        private Context context = WaveInWallpaperService.this;
        private WaveInWallpaperService service = WaveInWallpaperService.this;
        private WallpaperGLSurfaceView audioVisualizationView;
        private DbmHandler dbmHandler;
        private GLAudioVisualizationView.AudioVisualizationRenderer renderer;
        private Disposable prefDisposable;

        private boolean handlerPaused = true;

        private int colorBgDefault = service.getResources().getColor(R.color.colorWaveInBgDefault);
        private int colorLayer1Default = service.getResources().getColor(R.color.colorWaveInLayer1Default);
        private int colorLayer2Default = service.getResources().getColor(R.color.colorWaveInLayer2Default);
        private int colorLayer3Default = service.getResources().getColor(R.color.colorWaveInLayer3Default);

        private GLAudioVisualizationView.Builder getVisualizationViewBuilder() {

            int colorBg = sharedPreferences.getInt("wave_in_background_color", colorBgDefault);
            int[] layerColors = new int[] {
                    sharedPreferences.getInt("wave_in_layer_1_color", colorLayer1Default),
                    sharedPreferences.getInt("wave_in_layer_2_color", colorLayer2Default),
                    sharedPreferences.getInt("wave_in_layer_3_color", colorLayer3Default),
            };

            return new GLAudioVisualizationView.Builder(WaveInWallpaperService.this)
                    .setBubblesSize(R.dimen.bubble_size)
                    .setBubblesRandomizeSize(true)
                    .setWavesHeight(R.dimen.wave_height)
                    .setWavesFooterHeight(R.dimen.footer_height)
                    .setWavesCount(7)
                    .setLayersCount(3)
                    .setBackgroundColor(colorBg)
                    .setLayerColors(layerColors)
                    .setBubblesPerLayer(16);

        }

        private Observable<GLAudioVisualizationView.ColorsBuilder> getColorsBuilderObservable() {

            Observable<Integer> bgColorObservable = rxPreferences.getInteger("wave_in_background_color")
                    .asObservable();

            Observable<Integer> layer1ColorObservable = rxPreferences.getInteger("wave_in_layer_1_color")
                    .asObservable();

            Observable<Integer> layer2ColorObservable = rxPreferences.getInteger("wave_in_layer_2_color")
                    .asObservable();

            Observable<Integer> layer3ColorObservable = rxPreferences.getInteger("wave_in_layer_3_color")
                    .asObservable();

            return Observable.combineLatest(bgColorObservable,
                    layer1ColorObservable,
                    layer2ColorObservable,
                    layer3ColorObservable,
                    (bg, l1, l2, l3) -> new GLAudioVisualizationView.ColorsBuilder(context)
                            .setBackgroundColor(bg)
                            .setLayerColors(new int[]{l1, l2, l3})
            );
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            audioVisualizationView = new WallpaperGLSurfaceView(service);
            dbmHandler = DbmHandler.Factory.newVisualizerHandler(service, 0);
            renderer = new GLAudioVisualizationView.RendererBuilder(getVisualizationViewBuilder())
                    .glSurfaceView(audioVisualizationView)
                    .handler(dbmHandler)
                    .build();

            audioVisualizationView.setEGLContextClientVersion(2);
            audioVisualizationView.setRenderer(renderer);

            //audioVisualizationView.onResume();
            //dbmHandler.onResume();

            prefDisposable = getColorsBuilderObservable()
                    .skip(1)
                    .subscribe(builder -> renderer.updateConfiguration(builder));
        }

        @Override
        public void onDestroy() {
                dbmHandler.release();
                audioVisualizationView.onDestroy();

            if (prefDisposable != null)
                prefDisposable.dispose();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                audioVisualizationView.onResume();
                if(handlerPaused) {
                    dbmHandler.onResume();
                    handlerPaused = false;
                }
            } else {
                //dbmHandler.onPause();
                audioVisualizationView.onPause();
            }
        }
    }
}

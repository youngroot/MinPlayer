package com.ivanroot.minplayer.visualization.wallpapers;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import com.cleveroad.audiovisualization.DbmHandler;
import com.cleveroad.audiovisualization.GLAudioVisualizationView;
import com.ivanroot.minplayer.R;

public class WaveInWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new WallpaperEngine();
    }

    private class WallpaperEngine extends Engine {

        private WallpaperGLSurfaceView audioVisualizationView;
        private DbmHandler dbmHandler;
        private GLAudioVisualizationView.AudioVisualizationRenderer renderer;

        private GLAudioVisualizationView.Builder getVisualizationViewBuilder() {
            return new GLAudioVisualizationView.Builder(WaveInWallpaperService.this)
                    .setBubblesSize(R.dimen.bubble_size)
                    .setBubblesRandomizeSize(true)
                    .setWavesHeight(R.dimen.wave_height)
                    .setWavesFooterHeight(R.dimen.footer_height)
                    .setWavesCount(7)
                    .setLayersCount(3)
                    .setBackgroundColorRes(R.color.av_color_bg)
                    .setLayerColors(R.array.av_colors)
                    .setBubblesPerLayer(16);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            WallpaperService service = WaveInWallpaperService.this;
            audioVisualizationView = new WallpaperGLSurfaceView(service);
            dbmHandler = DbmHandler.Factory.newVisualizerHandler(service, 0);

            renderer = new GLAudioVisualizationView.RendererBuilder(getVisualizationViewBuilder())
                    .glSurfaceView(audioVisualizationView)
                    .handler(dbmHandler)
                    .build();

            audioVisualizationView.setEGLContextClientVersion(2);
            audioVisualizationView.setRenderer(renderer);

            audioVisualizationView.onResume();
            dbmHandler.onResume();
        }

        @Override
        public void onDestroy() {
            dbmHandler.release();
            audioVisualizationView.onDestroy();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                //audioVisualizationView.onResume();
                //dbmHandler.onResume();
            } else {
                //dbmHandler.onPause();
                //audioVisualizationView.onPause();
            }
        }

        private class WallpaperGLSurfaceView extends GLSurfaceView {

            WallpaperGLSurfaceView(Context context) {
                super(context);
            }

            @Override
            public SurfaceHolder getHolder() {
                return getSurfaceHolder();
            }

            public void onDestroy() {
                super.onDetachedFromWindow();
            }
        }
    }

}

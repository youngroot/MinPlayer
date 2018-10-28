package com.ivanroot.minplayer.visualization.wallpapers;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import me.bogerchan.niervisualizer.NierVisualizerManager;
import me.bogerchan.niervisualizer.renderer.IRenderer;
import me.bogerchan.niervisualizer.renderer.circle.CircleBarRenderer;
import me.bogerchan.niervisualizer.renderer.circle.CircleWaveRenderer;

public class NierWallpaperService extends WallpaperService {

    private NierVisualizerManager visualizerManager = new NierVisualizerManager();

    @Override
    public void onCreate() {
        super.onCreate();
        visualizerManager.init(0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        visualizerManager.release();
    }

    @Override
    public Engine onCreateEngine() {
        return new WallpaperEngine();
    }

    private class WallpaperEngine extends Engine {
        private SurfaceView surfaceView;

        private IRenderer[] renderers = new IRenderer[]{
                new CircleBarRenderer(),
                new CircleWaveRenderer(),
        };

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            surfaceView = new WallpaperSurfaceView(NierWallpaperService.this);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
                if (visible) {
                    visualizerManager.start(surfaceView, renderers);
                } else {
                    visualizerManager.stop();
                }
        }

        private class WallpaperSurfaceView extends SurfaceView {

            WallpaperSurfaceView(Context context) {
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

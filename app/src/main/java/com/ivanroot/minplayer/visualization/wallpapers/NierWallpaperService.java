package com.ivanroot.minplayer.visualization.wallpapers;

import android.content.Context;
import android.service.wallpaper.WallpaperService;
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
        private WallpaperSurfaceView wallpaperSurfaceView;

        private IRenderer[] renderers = new IRenderer[]{
                new CircleBarRenderer(),
                new CircleWaveRenderer(),
        };

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            wallpaperSurfaceView = new WallpaperSurfaceView(NierWallpaperService.this);
            visualizerManager.start(wallpaperSurfaceView, renderers);
        }


        @Override
        public void onVisibilityChanged(boolean visible) {
//                if (visible) {
//                    visualizerManager.start(wallpaperSurfaceView, renderers);
//                } else {
//                    visualizerManager.stop();
//                }
        }

        @Override
        public void onDestroy() {
            visualizerManager.stop();
            wallpaperSurfaceView.onDestroy();
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

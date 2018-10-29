package com.ivanroot.minplayer.visualization.wallpapers;

import android.content.Context;
import android.graphics.Paint;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.ivanroot.minplayer.R;

import me.bogerchan.niervisualizer.NierVisualizerManager;
import me.bogerchan.niervisualizer.renderer.IRenderer;
import me.bogerchan.niervisualizer.renderer.circle.CircleBarRenderer;
import me.bogerchan.niervisualizer.renderer.circle.CircleWaveRenderer;
import me.bogerchan.niervisualizer.util.NierAnimator;

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
        //visualizerManager.stop();
        visualizerManager.release();
    }

    @Override
    public Engine onCreateEngine() {
        return new WallpaperEngine();
    }

    private class WallpaperEngine extends Engine {
        private WallpaperSurfaceView wallpaperSurfaceView;

        private IRenderer[] renderers;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            Paint p1 = new Paint();
            Paint p2 = new Paint();

            p1.setColor(getResources().getColor(R.color.colorPink));
            p2.setColor(getResources().getColor(R.color.colorGrey));

            renderers = new IRenderer[]{
                    new CircleBarRenderer(p1, 8, CircleBarRenderer.Type.TYPE_A_AND_TYPE_B, 1f, 1f, new NierAnimator()),
                    new CircleWaveRenderer(p1, 8, CircleWaveRenderer.Type.TYPE_A, 1f, 1f, new NierAnimator()),
            };

            wallpaperSurfaceView = new WallpaperSurfaceView(NierWallpaperService.this);
            visualizerManager.start(wallpaperSurfaceView, renderers);
        }


        @Override
        public void onVisibilityChanged(boolean visible) {
                /*if (visible) {
                    visualizerManager.start(wallpaperSurfaceView, renderers);
                } else {
                    visualizerManager.stop();
                }*/
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

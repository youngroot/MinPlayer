package com.ivanroot.minplayer.visualization.wallpapers;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLSurfaceView;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

import com.f2prateek.rx.preferences2.RxSharedPreferences;

public abstract class WallpaperServiceBase extends WallpaperService {
    protected SharedPreferences sharedPreferences;
    protected RxSharedPreferences rxPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        rxPreferences = RxSharedPreferences.create(sharedPreferences);
    }

    protected abstract class WallpaperEngineBase extends Engine{
        protected class WallpaperGLSurfaceView extends GLSurfaceView {

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

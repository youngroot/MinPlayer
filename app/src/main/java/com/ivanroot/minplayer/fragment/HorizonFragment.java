package com.ivanroot.minplayer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.visualization.surfaceview.HorizonGLSurfaceView;

public class HorizonFragment extends Fragment {
    private HorizonGLSurfaceView horizonGLSurfaceView;

    private int getThemePrimaryColor(){
        final TypedValue value = new TypedValue();
        getActivity().getTheme ().resolveAttribute (R.attr.colorPrimary, value, true);
        return value.data;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        horizonGLSurfaceView = new HorizonGLSurfaceView(getActivity(), 0, getResources().getColor(R.color.colorBlue));
        return horizonGLSurfaceView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(toString(), "onResume, glSurfaceView == " + String.valueOf(horizonGLSurfaceView));
        horizonGLSurfaceView.onResume();
    }

    @Override
    public void onPause() {
        Log.i(toString(), "onResume, glSurfaceView == " + String.valueOf(horizonGLSurfaceView));
        horizonGLSurfaceView.onPause();
        super.onPause();
    }
}

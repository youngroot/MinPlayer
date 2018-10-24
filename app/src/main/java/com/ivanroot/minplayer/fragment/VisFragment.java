package com.ivanroot.minplayer.fragment;

import android.app.Fragment;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.visualization.BandVisualization;
import com.ivanroot.minplayer.visualization.surfaceview.VisualizerSurface;

/**
 * Created by Ivan Root on 02.11.2017.
 */

public class VisFragment extends Fragment {

    public static final String NAME = "VisFragment";
    private int audioSessionId = 0;
    private static final int STEELBLUE = 0xFF4682B4;
    private static final int VIOLET = 0xFFEE82EE;
    private VisualizerSurface visSurface;

    public VisFragment() {}

    public VisFragment(int audioSessionId) {
        this.audioSessionId = audioSessionId;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if(savedInstanceState != null){
            audioSessionId = savedInstanceState.getInt("audio_session_id");
        }
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getActivity().getTheme();
        theme.resolveAttribute(R.attr.colorAccent, typedValue, true);
        @ColorInt int color = typedValue.data;
        BandVisualization vis = new BandVisualization()
                .setBandsAmount(28)
                .useCenterDoubleBands(true)
                .useDecibels(true)
                .useRoundedCorners(true)
                .setColors(Color.WHITE, color, color);
        visSurface = new VisualizerSurface(getActivity(), audioSessionId, vis);
        return visSurface;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("audio_session_id",audioSessionId);
        super.onSaveInstanceState(outState);
    }

}

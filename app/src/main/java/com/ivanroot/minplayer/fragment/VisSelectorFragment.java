package com.ivanroot.minplayer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.adapter.VisFragmentPagerAdapter;

public class VisSelectorFragment extends NavFragmentBase {
    public static final String NAME = "VisSelectorFragment";
    private ViewPager viewPager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.vis_selector_fragment_layout, container, false);
        setupDrawer(view);
        viewPager = (ViewPager)view.findViewById(R.id.viewPager);
        VisFragmentPagerAdapter adapter = new VisFragmentPagerAdapter(((FragmentActivity)getActivity()).getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        return view;
    }

    @Override
    protected String getActionBarTitle() {
        return getResources().getString(R.string.visualizer);
    }
}

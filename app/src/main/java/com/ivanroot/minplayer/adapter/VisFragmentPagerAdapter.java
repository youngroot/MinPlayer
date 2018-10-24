package com.ivanroot.minplayer.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.ivanroot.minplayer.fragment.HorizonRecorderFragment;
import com.ivanroot.minplayer.fragment.WaveInFragment;

public class VisFragmentPagerAdapter extends FragmentStatePagerAdapter {

    private Fragment[] fragments = new Fragment[]{
            //new WaveInRecorderFragment(),
            new WaveInFragment(),
            new HorizonRecorderFragment()
    };

    public VisFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }
}

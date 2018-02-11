package com.ivanroot.minplayer.fragment;

import android.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.activity.MainActivity;

/**
 * Created by Ivan Root on 30.12.2017.
 */

public abstract class NavFragmentBase extends Fragment {

    protected MainActivity activity;
    protected Toolbar toolbar;
    protected DrawerLayout drawer;
    protected ActionBarDrawerToggle toggle;

    protected void setupDrawer(View view) {
        activity = (MainActivity) getActivity();
        toolbar = (Toolbar)view.findViewById(R.id.toolbar);
        drawer = (DrawerLayout)activity.findViewById(R.id.drawerLayout);
        toggle = new ActionBarDrawerToggle(getActivity(),drawer, toolbar,R.string.drawer_open,R.string.drawer_close);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setHomeButtonEnabled(true);
        toggle.setDrawerIndicatorEnabled(true);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
    }
}

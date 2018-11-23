package com.ivanroot.minplayer.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.activity.MainActivity;

import retrofit.RestAdapter;

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
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        drawer = (DrawerLayout) activity.findViewById(R.id.drawerLayout);
        toggle = new ActionBarDrawerToggle(activity, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setHomeButtonEnabled(true);
        toggle.setDrawerIndicatorEnabled(true);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
    }

    public void setActionBarTitle(String title){
        try {
            Log.i(toString(),"onSetActionBarTitle");
            activity.getSupportActionBar().setTitle(title);
        } catch (NullPointerException ex) {
            Log.e(toString(), ex.toString());
            ex.printStackTrace();
        }
    }

    public abstract String getActionBarTitle();

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setActionBarTitle(getActionBarTitle());
    }
}

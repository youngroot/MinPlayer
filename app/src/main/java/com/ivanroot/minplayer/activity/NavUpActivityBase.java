package com.ivanroot.minplayer.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.ivanroot.minplayer.R;

public abstract class NavUpActivityBase extends NightModeResponsibleActivity {
    protected Toolbar toolbar;
    protected int layoutResId;

    public NavUpActivityBase(int layoutResId){
        this.layoutResId = layoutResId;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutResId);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            Log.e(toString(), ex.getMessage());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

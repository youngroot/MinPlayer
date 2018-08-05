package com.ivanroot.minplayer.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.fragment.SettingsFragment;

import io.reactivex.disposables.Disposable;

public class SettingsActivity extends NightModeResponsibleActivity {
    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            Log.d("Error", ex.getMessage());
        }

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_holder, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

package com.ivanroot.minplayer.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.fragment.SettingsFragment;
import com.ivanroot.minplayer.utils.Utils;

import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SettingsActivity extends AppCompatActivity {

    private final AppCompatActivity activity = this;
    private Toolbar toolbar;
    private Disposable disposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        disposable = Utils.getNightModeObservableAndApplyTheme(this).subscribe();
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
    protected void onDestroy() {
        super.onDestroy();
        if(disposable != null)
            disposable.dispose();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

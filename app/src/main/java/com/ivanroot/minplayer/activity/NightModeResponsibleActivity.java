package com.ivanroot.minplayer.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.ivanroot.minplayer.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class NightModeResponsibleActivity extends AppCompatActivity {

    private final NightModeResponsibleActivity activity = this;
    private Disposable prefDisposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        RxSharedPreferences rxPreferences = RxSharedPreferences.create(preferences);
        final boolean nightMode = preferences.getBoolean("pref_key_night_mode", false);

        setTheme(nightMode ? R.style.MinPlayerNightTheme : R.style.MinPlayerOriginalTheme);

        prefDisposable = rxPreferences.getBoolean("pref_key_night_mode")
                .asObservable()
                .skip(1)
                .delay(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(val -> activity.recreate())
                .subscribe();

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (prefDisposable != null)
            prefDisposable.dispose();
    }
}

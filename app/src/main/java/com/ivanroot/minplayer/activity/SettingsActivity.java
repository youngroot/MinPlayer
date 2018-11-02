package com.ivanroot.minplayer.activity;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.ivanroot.minplayer.R;

import java.util.Objects;

public class SettingsActivity extends NavUpActivityBase {

    public SettingsActivity(){
        super(R.layout.activity_settings);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            outState.putString("ActionBarTitle", getSupportActionBar().getTitle().toString());
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String action = null;

        if (intent != null) {
            action = intent.getAction();
        }

        if (savedInstanceState == null) {

            Fragment fragment;

            if (Objects.equals(action, "wave_in_wallpaper_settings")) {
                getSupportActionBar()
                        .setTitle(getResources().getString(R.string.wave_in_wallpaper));
                fragment = new WaveInSettingsFragment();

            } else if (Objects.equals(action, "horizon_wallpaper_settings")) {
                getSupportActionBar()
                        .setTitle(getResources().getString(R.string.horizon_wallpaper));
                fragment = new HorizonSettingsFragment();

            } else {
                fragment = new SettingsFragment();
            }

            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_holder, fragment)
                    .commit();
        } else {
            getSupportActionBar()
                    .setTitle(savedInstanceState.getString("ActionBarTitle"));
        }
    }

    public static class SettingsFragment extends PreferenceFragment {


        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
    }

    static public class WaveInSettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.wave_in_wallpaper_preferences);
            PreferenceManager.setDefaultValues(getActivity(), R.xml.wave_in_wallpaper_preferences, false);
        }
    }

    static public class HorizonSettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.horizon_wallpaper_preferences);
            PreferenceManager.setDefaultValues(getActivity(), R.xml.horizon_wallpaper_preferences, false);
        }
    }
}

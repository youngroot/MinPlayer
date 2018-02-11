package com.ivanroot.minplayer.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.ivanroot.minplayer.fragment.PlayerFragment;
import com.ivanroot.minplayer.R;

public class PlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        Window w = getWindow(); // in Activity's onCreate() for instance
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.PlayerHolder, new PlayerFragment())
                .commit();

    }

}

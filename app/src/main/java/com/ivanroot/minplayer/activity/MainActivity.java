package com.ivanroot.minplayer.activity;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.hwangjr.rxbus.Bus;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.fragment.ControllerFragment;
import com.ivanroot.minplayer.fragment.PlaylistFragment;
import com.ivanroot.minplayer.fragment.PlaylistSelectorFragment;
import com.ivanroot.minplayer.fragment.VisFragment;
import com.ivanroot.minplayer.player.PlayerService;
import com.ivanroot.minplayer.player.RxBus;
import com.ivanroot.minplayer.storio.PlaylistTable;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private PlayerService player;
    private boolean wasStarted = false;
    private Toolbar toolbar;
    private FloatingActionButton addBtn;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private String playlistName;
    private Map<String, Integer> itemId;
    private ControllerFragment controllerFragment;
    private Bus rxBus = RxBus.getInstance();
    public static final String ACTION_LAUNCH_FRAGMENT = "com.ivanroot.minplayer.action_launch_fragment";
    public static final String EVENT_FRAGMENT_LAUNCHED = "com.ivanroot.minplayer.action_launch_fragment";

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {


            PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) service;
            player = binder.getService();
            controllerFragment = new ControllerFragment();
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.controllerHolder, controllerFragment)
                    .commit();
            Log.i("onServiceConnected", player.getClass().getName());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            wasStarted = false;
        }
    };

    private void initItemId() {

        itemId = new HashMap<>();
        itemId.put(PlaylistFragment.NAME, R.id.all_audio);
        itemId.put(PlaylistSelectorFragment.NAME, R.id.playlists);
        itemId.put(VisFragment.NAME, R.id.spectrum);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rxBus.register(this);
        setContentView(R.layout.activity_main);
        bindService(new Intent(this, PlayerService.class), conn, Context.BIND_AUTO_CREATE);
        initItemId();
        setupDrawer();

        if (savedInstanceState == null) {

            Log.i("MainActivity", "savedInstanceState == null");
            startPlayerService();
            fragmentLauncher(R.id.all_audio);
            navigationView.setCheckedItem(R.id.all_audio);

        } else {

            wasStarted = savedInstanceState.getBoolean("wasStarted");
            playlistName = savedInstanceState.getString("name");
            //getSupportActionBar().setTitle(savedInstanceState.getString("title", getResources().getString(R.string.app_name)));
        }
    }


    private void startPlayerService() {
        if (!wasStarted) {
            startService(new Intent(this, PlayerService.class));
            wasStarted = true;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //drawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {

            Fragment fragment = getFragmentManager()
                    .findFragmentById(R.id.fragmentHolder);

            switch (fragment.getTag()) {

                case PlaylistFragment.NAME:
                    String name = ((PlaylistFragment)fragment).getPlaylistName();
                    if(!name.equals(PlaylistTable.Playlist.ALL_TRACKS_PLAYLIST)) {
                        fragmentLauncher(R.id.playlists);
                        navigationView.setCheckedItem(R.id.playlists);
                    }
                    break;

                case PlaylistSelectorFragment.NAME:
                    fragmentLauncher(R.id.all_audio);
                    navigationView.setCheckedItem(R.id.all_audio);
                    break;

                case VisFragment.NAME:
                    fragmentLauncher(R.id.spectrum);
                    navigationView.setCheckedItem(R.id.spectrum);
                    break;

                default:
                    fragmentLauncher(R.id.all_audio);
                    navigationView.setCheckedItem(R.id.all_audio);
                    break;
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //drawerToggle.onConfigurationChanged(newConfig);
    }

    private void stopPlayerService() {
        if (wasStarted && isFinishing()) {
            wasStarted = false;
            stopService(new Intent(this, PlayerService.class));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rxBus.unregister(this);
        unbindService(conn);
        stopPlayerService();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("wasStarted", wasStarted);
        outState.putString("title", getSupportActionBar().getTitle().toString());
        if (playlistName != null) {
            outState.putString("name", playlistName);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawer() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        drawerToggle =
                new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
            // Called when drawer is opened
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            // Called when drawer is closed
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }
        };
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        fragmentLauncher(id);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void fragmentLauncher(Integer itemId) {

        Fragment fragment = null;

        switch (itemId) {

            case R.id.all_audio:
                fragment = new PlaylistFragment(PlaylistTable.Playlist.ALL_TRACKS_PLAYLIST);
                break;

            case R.id.playlists:
                fragment = new PlaylistSelectorFragment();
                break;

            case R.id.spectrum:
                fragment = new VisFragment(0);
                break;

            case R.id.settings:
                fragment = null;
                break;
        }

        if (fragment != null) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentHolder, fragment, fragment.toString())
                    .commit();
        }

    }

}

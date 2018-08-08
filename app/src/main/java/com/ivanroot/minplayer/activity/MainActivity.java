package com.ivanroot.minplayer.activity;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.hwangjr.rxbus.Bus;
import com.hwangjr.rxbus.RxBus;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.fragment.ControllerFragment;
import com.ivanroot.minplayer.fragment.DiskFragment;
import com.ivanroot.minplayer.fragment.PlayerFragment;
import com.ivanroot.minplayer.fragment.PlaylistFragment;
import com.ivanroot.minplayer.fragment.PlaylistSelectorFragment;
import com.ivanroot.minplayer.fragment.VisFragment;
import com.ivanroot.minplayer.player.PlayerService;
import com.ivanroot.minplayer.playlist.PlaylistManager;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends NightModeResponsibleActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private PlayerService player;
    private boolean wasStarted = false;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private String playlistName;
    private Map<String, Integer> itemId;
    private SlidingUpPanelLayout panelLayout;
    private ControllerFragment controllerFragment;
    private PlayerFragment playerFragment;
    private Bus rxBus = RxBus.get();

    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) service;
            player = binder.getService();
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
        setupSlidingPanel();

        if (savedInstanceState == null) {
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
            controllerFragment = new ControllerFragment();
            playerFragment = new PlayerFragment();
            //controllerFragment.setPanelLayout(panelLayout);
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.controllerHolder, controllerFragment, ControllerFragment.NAME)
                    .commit();

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
        } else if (panelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {

            Fragment fragment = getFragmentManager()
                    .findFragmentById(R.id.fragmentHolder);

            switch (fragment.getTag()) {
                case PlaylistFragment.NAME:
                    String name = ((PlaylistFragment) fragment).getPlaylistName();
                    if (!name.equals(PlaylistManager.ALL_TRACKS_PLAYLIST)) {
                        fragmentLauncher(R.id.playlists);
                        navigationView.setCheckedItem(R.id.playlists);
                    }
                    break;

                case PlaylistSelectorFragment.NAME:
                    fragmentLauncher(R.id.all_audio);
                    navigationView.setCheckedItem(R.id.all_audio);
                    break;

                case VisFragment.NAME:
                    fragmentLauncher(R.id.all_audio);
                    navigationView.setCheckedItem(R.id.all_audio);
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

    private void setupSlidingPanel() {
        panelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        panelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                Log.i("PanelState", newState.name());
                if (newState != SlidingUpPanelLayout.PanelState.COLLAPSED && newState != SlidingUpPanelLayout.PanelState.HIDDEN)
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                else
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }
        });
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
        String tag = null;
        switch (itemId) {

            case R.id.all_audio:
                fragment = new PlaylistFragment(PlaylistManager.ALL_TRACKS_PLAYLIST);
                tag = PlaylistFragment.NAME;
                break;

            case R.id.playlists:
                fragment = new PlaylistSelectorFragment();
                tag = PlaylistSelectorFragment.NAME;
                break;
            case R.id.disk:
                fragment = new DiskFragment();
                tag = DiskFragment.NAME;
                break;

            case R.id.spectrum:
                fragment = new VisFragment(0);
                tag = VisFragment.NAME;
                break;

            case R.id.settings:
                fragment = null;
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }

        if (fragment != null) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentHolder, fragment, tag)
                    .commit();
        }

    }

    public SlidingUpPanelLayout getPanelLayout() {
        return panelLayout;
    }

}

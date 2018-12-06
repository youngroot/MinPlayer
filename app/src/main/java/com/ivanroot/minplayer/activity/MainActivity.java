package com.ivanroot.minplayer.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.hwangjr.rxbus.Bus;
import com.hwangjr.rxbus.RxBus;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.fragment.AllTracksPlaylistFragment;
import com.ivanroot.minplayer.fragment.ControllerFragment;
import com.ivanroot.minplayer.fragment.DiskFragment;
import com.ivanroot.minplayer.fragment.PlayerFragment;
import com.ivanroot.minplayer.fragment.PlaylistFragment;
import com.ivanroot.minplayer.fragment.PlaylistSelectorFragment;
import com.ivanroot.minplayer.fragment.VisFragment;
import com.ivanroot.minplayer.fragment.VisSelectorFragment;
import com.ivanroot.minplayer.player.PlayerService;
import com.ivanroot.minplayer.playlist.PlaylistManager;
import com.ivanroot.minplayer.utils.Pair;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class MainActivity extends NightModeResponsibleActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static final String ACTION_OPEN_PLAYLISTS = "action_open_playlists";

    private PlayerService player;
    private boolean wasStarted = false;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private Map<String, Integer> itemId;
    private SlidingUpPanelLayout panelLayout;
    private ControllerFragment controllerFragment;
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
        itemId.put(VisFragment.NAME, R.id.visualizer);
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

            SharedPreferences sharedPreferences = getSharedPreferences(PlayerService.SERVICE_NAME, MODE_PRIVATE);
            long playlistId = sharedPreferences.getLong(PlayerService.PREF_LAST_PLAYLIST_ID, PlaylistManager.ALL_TRACKS_PLAYLIST_ID);
            launchPlaylistFragment(playlistId);
        } else {
            wasStarted = savedInstanceState.getBoolean("wasStarted");
        }
    }

    private void startPlayerService() {
        if (!wasStarted) {
            startService(new Intent(this, PlayerService.class));
            controllerFragment = new ControllerFragment();
            PlayerFragment playerFragment = new PlayerFragment();

            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.controller_holder, controllerFragment, ControllerFragment.NAME)
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
                    .findFragmentById(R.id.fragment_holder);

            switch (fragment.getTag()) {
                case AllTracksPlaylistFragment.NAME:
                    break;

                case PlaylistFragment.NAME:
                    fragmentLauncher(R.id.playlists);
                    navigationView.setCheckedItem(R.id.playlists);
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

    public void fragmentLauncher(Integer itemId) {
        String tag = null;
        Pair<Fragment, Boolean> fragmentBooleanPair = new Pair<>(null, false);

        switch (itemId) {

            case R.id.all_audio:
                tag = AllTracksPlaylistFragment.NAME;
                fragmentBooleanPair = getExistingOrNewFragment(tag, AllTracksPlaylistFragment.class);
                break;

            case R.id.playlists:
                tag = PlaylistSelectorFragment.NAME;
                fragmentBooleanPair = getExistingOrNewFragment(tag, PlaylistSelectorFragment.class);
                break;

            case R.id.disk:
                tag = DiskFragment.NAME;
                fragmentBooleanPair = getExistingOrNewFragment(tag, DiskFragment.class);
                break;

            case R.id.visualizer:
                tag = VisSelectorFragment.NAME;
                fragmentBooleanPair = getExistingOrNewFragment(tag, VisFragment.class);
                break;

            case R.id.settings:
                fragmentBooleanPair.first = null;
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }

        if (fragmentBooleanPair.first != null) {
            FragmentTransaction transaction = getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_holder, fragmentBooleanPair.first, tag);

            if (!fragmentBooleanPair.second)
                transaction.addToBackStack(tag);

            transaction.commit();
        }
    }

    public SlidingUpPanelLayout getPanelLayout() {
        return panelLayout;
    }

    public void launchPlaylistFragment(long playlistId) {
        if (Objects.equals(playlistId, PlaylistManager.ALL_TRACKS_PLAYLIST_ID)) {
            fragmentLauncher(R.id.all_audio);
            navigationView.setCheckedItem(R.id.all_audio);
            return;
        }

        if (Objects.equals(playlistId, PlaylistManager.DISK_ALL_TRACKS_PLAYLIST_ID)) {
            fragmentLauncher(R.id.disk);
            navigationView.setCheckedItem(R.id.disk);
            return;
        }

        PlaylistFragment playlistFragment = new PlaylistFragment();
        playlistFragment.setPlaylistId(playlistId);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_holder, playlistFragment, PlaylistFragment.NAME)
                .commit();

        navigationView.setCheckedItem(R.id.playlists);

    }

    private Pair<Fragment, Boolean> getExistingOrNewFragment(@NonNull String tag, Class fragmentClass) {
        Fragment fragment = getFragmentManager().findFragmentByTag(tag);

        if (fragment != null)
            return new Pair<>(fragment, true);

        try {
            return new Pair<>((Fragment) Class.forName(fragmentClass.getName()).newInstance(), false);
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new Pair<>(null, false);
    }
}

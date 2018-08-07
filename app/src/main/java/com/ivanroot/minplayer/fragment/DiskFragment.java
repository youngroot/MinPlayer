package com.ivanroot.minplayer.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hwangjr.rxbus.Bus;
import com.ivanroot.minplayer.activity.MainActivity;
import com.ivanroot.minplayer.activity.TokenActivity;
import com.ivanroot.minplayer.adapter.BasePlaylistAdapter;
import com.ivanroot.minplayer.adapter.DiskPlaylistAdapter;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.audio.OnAudioClickListener;
import com.ivanroot.minplayer.disk.RestClientUtil;
import com.ivanroot.minplayer.player.RxBus;
import com.ivanroot.minplayer.playlist.Playlist;
import com.ivanroot.minplayer.playlist.PlaylistManager;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.RestClient;
import com.ivanroot.minplayer.R;

import java.util.List;

import static com.ivanroot.minplayer.player.constants.PlayerActions.ACTION_PLAY_AUDIO;
import static com.ivanroot.minplayer.player.constants.PlayerActions.ACTION_SET_PLAYLIST;

public class DiskFragment extends NavFragmentBase {
    public static final String NAME = "DiskFragment";
    private String accessToken;
    private RestClient restClient;
    private FastScrollRecyclerView audioRecycler;
    private DiskPlaylistAdapter adapter;
    private List<Audio> downloading;
    private MainActivity activity;
    private Bus rxBus = RxBus.getInstance();


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            accessToken = data.getStringExtra("access_token");
            onTokenReceived();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rxBus.register(this);
        activity = (MainActivity) getActivity();
        adapter = new DiskPlaylistAdapter(getActivity());
        checkAndGetToken();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.all_tracks_playlist_fragment, container, false);
        setupDrawer(view);
        setupRecycler(view);
        prepareListeners(view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity.getSupportActionBar().setTitle(getResources().getString(R.string.yandex_disk));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        rxBus.unregister(this);
        adapter.dispose();
    }


    private void checkAndGetToken() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        accessToken = preferences.getString(TokenActivity.PREF_ACCESS_TOKEN, null);
        if (accessToken == null) {
            Intent intent = new Intent(getActivity(), TokenActivity.class);
            startActivityForResult(intent, 1);
            return;
        }
        onTokenReceived();
    }

    public void onTokenReceived() {
        restClient = RestClientUtil.getInstance(new Credentials("", accessToken));
        adapter.setRestClient(restClient);
        adapter.subscribe();
    }

    private void setupRecycler(View view) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        audioRecycler = (FastScrollRecyclerView) view.findViewById(R.id.audio_recycler);
        audioRecycler.setHasFixedSize(true);
        audioRecycler.setLayoutManager(layoutManager);
        audioRecycler.setAdapter(adapter);
    }

    private void prepareListeners(View view) {
        adapter.setOnMoreBtnClickListener((moreBtn, playlist, i) -> {

        });

        adapter.setAudioClickListener((audio, playlistName) -> {
            Log.i("Clicked", playlistName + " " + audio.getCloudPath());
            rxBus.post(ACTION_SET_PLAYLIST, playlistName);
            rxBus.post(ACTION_PLAY_AUDIO, audio);
        });

    }
}

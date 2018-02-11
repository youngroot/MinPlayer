package com.ivanroot.minplayer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hwangjr.rxbus.Bus;
import com.ivanroot.minplayer.adapter.AudioRecyclerAdapter;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.player.RxBus;
import com.ivanroot.minplayer.playlist.PlaylistManager;
import com.ivanroot.minplayer.storio.PlaylistTable;
import com.simplecityapps.recyclerview_fastscroll.interfaces.OnFastScrollStateChangeListener;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import static com.ivanroot.minplayer.player.PlayerActionsEvents.*;

/**
 * Created by Ivan Root on 17.12.2017.
 */

public class PlaylistRecyclerFragment extends NavFragmentBase {

    public static final String NAME = "PlaylistRecyclerFragment";
    private static final String BUNDLE_RECYCLER_LAYOUT = "classname.recycler.layout";


    private String playlistName;
    private AudioRecyclerAdapter adapter;
    private FastScrollRecyclerView audioRecyclerView;
    private FloatingActionButton playFab;
    private PlaylistManager playlistManager;
    private AppBarLayout appBarLayout;
    private Bus rxBus = RxBus.getInstance();

    public PlaylistRecyclerFragment() {}

    public PlaylistRecyclerFragment(String playlistName) {
        this.playlistName = playlistName;
    }

    public static PlaylistRecyclerFragment newInstance(String playlistName) {
        return new PlaylistRecyclerFragment(playlistName);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null)
            playlistName = savedInstanceState.getString("playlist_name");
        adapter = new AudioRecyclerAdapter(getActivity(), playlistName);
        playlistManager = PlaylistManager.getInstance();
        rxBus.register(this);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = getView(inflater, container);
        setupDrawer(view);
        setupRecycler(view);
        prepareListeners(view);
        if (savedInstanceState != null) {
            audioRecyclerView.getLayoutManager()
                    .onRestoreInstanceState(savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT));
        }

        return view;
    }

    private void prepareListeners(View view) {


        if(!playlistName.equals(PlaylistTable.ALL_TRACKS_PLAYLIST)) {

            playFab = (FloatingActionButton) view.findViewById(R.id.fab_play);
            playFab.setOnClickListener(v -> {
                rxBus.post(ACTION_SET_PLAYLIST, playlistName);
                if(adapter.getPlaylist().size() > 0) {
                    rxBus.post(ACTION_PLAY_AUDIO, adapter.getPlaylist().getAudio(0));
                }
                appBarLayout.setExpanded(false, true);
            });
        }

        adapter.setOnAudioClickListener((audio, playlistName) -> {
            rxBus.post(ACTION_SET_PLAYLIST,playlistName);
            rxBus.post(ACTION_PLAY_AUDIO,audio);

        });
    }

    private View getView(LayoutInflater inflater, @Nullable ViewGroup container) {

        int layoutResource = (playlistName.equals(PlaylistTable.ALL_TRACKS_PLAYLIST)
                ? R.layout.all_tracks_recycler_layout
                : R.layout.playlist_recycler_layout);
        View view = inflater.inflate(layoutResource, container, false);

        appBarLayout = (AppBarLayout) view.findViewById(R.id.app_bar);

        return view;
    }

    private void setupRecycler(View view) {

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        audioRecyclerView = (FastScrollRecyclerView) view.findViewById(R.id.audio_recycler);
        audioRecyclerView.setHasFixedSize(true);
        audioRecyclerView.setLayoutManager(layoutManager);
        audioRecyclerView.setStateChangeListener(new OnFastScrollStateChangeListener() {

            @Override
            public void onFastScrollStart() {
                //appBarLayout.setExpanded(false, true);
                //playFab.hide();
            }

            @Override
            public void onFastScrollStop() {
                //appBarLayout.setExpanded(true, true);
                //playFab.show();
            }

        });

        /*audioRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && playFab.getVisibility() == View.VISIBLE) {
                    playFab.hide();
                } else if (dy < 0 && playFab.getVisibility() != View.VISIBLE) {
                    playFab.show();
                }
            }
        });*/

        audioRecyclerView.setAdapter(adapter);
    }

    public String getPlaylistName() {
        return playlistName;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String title = playlistManager.getTitleFromPlaylistName(playlistName,getActivity());
        activity.getSupportActionBar().setTitle(title);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (playlistName != null)
            outState.putString("playlist_name", playlistName);
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, audioRecyclerView.getLayoutManager().onSaveInstanceState());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (adapter != null)
            adapter.dispose();
        rxBus.unregister(this);
        super.onDestroy();
    }

    @Override
    public String toString() {
        return NAME;
    }
}

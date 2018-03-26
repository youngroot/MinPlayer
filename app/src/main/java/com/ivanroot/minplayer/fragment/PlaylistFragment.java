package com.ivanroot.minplayer.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hwangjr.rxbus.Bus;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.adapter.PlaylistRecyclerAdapter;
import com.ivanroot.minplayer.player.RxBus;
import com.ivanroot.minplayer.playlist.PlaylistItem;
import com.ivanroot.minplayer.playlist.PlaylistManager;
import com.ivanroot.minplayer.utils.Utils;
import com.simplecityapps.recyclerview_fastscroll.interfaces.OnFastScrollStateChangeListener;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.squareup.picasso.Picasso;

import static com.ivanroot.minplayer.player.PlayerActionsEvents.ACTION_PLAY_AUDIO;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.ACTION_SET_PLAYLIST;

/**
 * Created by Ivan Root on 17.12.2017.
 */

public class PlaylistFragment extends NavFragmentBase {

    public static final String NAME = "PlaylistFragment";
    private static final String BUNDLE_RECYCLER_LAYOUT = "classname.recycler.layout";


    private PlaylistItem playlistItem;
    private String playlistName = null;
    private PlaylistRecyclerAdapter adapter;
    private FastScrollRecyclerView audioRecyclerView;
    private FloatingActionButton playFab;
    private PlaylistManager playlistManager;
    private AppBarLayout appBarLayout;
    private ImageView playlistImage = null;
    private Bus rxBus = RxBus.getInstance();

    public PlaylistFragment() {}

    public PlaylistFragment(@NonNull String playlistName){
        this.playlistName = playlistName;
    }

    public PlaylistFragment(@NonNull PlaylistItem playlistItem) {
        this.playlistItem = playlistItem;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        playlistManager = PlaylistManager.getInstance();
        if(playlistName != null)
            playlistItem = playlistManager.getPlaylistItem(getActivity(),playlistName);
        adapter = new PlaylistRecyclerAdapter(getActivity(), playlistItem.getName());
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

        if(!playlistItem.getName().equals(PlaylistManager.ALL_TRACKS_PLAYLIST)) {
            playlistImage = (ImageView) view.findViewById(R.id.playlistImage);
        }
        return view;
    }

    private void prepareListeners(View view) {


        if(!playlistItem.getName().equals(PlaylistManager.ALL_TRACKS_PLAYLIST)) {

            playFab = (FloatingActionButton) view.findViewById(R.id.fab_play);
            playFab.setOnClickListener(v -> {
                rxBus.post(ACTION_SET_PLAYLIST, playlistItem);
                if(adapter.getPlaylist().size() > 0) {
                    rxBus.post(ACTION_PLAY_AUDIO, adapter.getPlaylist().getAudio(0));
                }
                appBarLayout.setExpanded(false, true);
            });
        }

        adapter.setAudioClickListener((audio, playlistName) -> {
            rxBus.post(ACTION_SET_PLAYLIST,playlistName);
            rxBus.post(ACTION_PLAY_AUDIO,audio);

        });

        adapter.setNewPlaylistUpdateListener(playlist -> {

        });
    }

    private View getView(LayoutInflater inflater, @Nullable ViewGroup container) {

        int layoutResource = (playlistItem.getName().equals(PlaylistManager.ALL_TRACKS_PLAYLIST)
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
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(audioRecyclerView.getContext(),LinearLayoutManager.VERTICAL);
        audioRecyclerView.addItemDecoration(mDividerItemDecoration);
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

    public PlaylistItem getPlaylistItem() {
        return playlistItem;
    }

    public String getPlaylistName(){
        return playlistName;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String title = playlistManager.getTitleFromPlaylistName(getActivity(),playlistItem.getName());
        activity.getSupportActionBar().setTitle(title);
        if(!playlistItem.getName().equals(PlaylistManager.ALL_TRACKS_PLAYLIST)) {
            Picasso.with(getActivity())
                    .load(Utils.getFileFromPath(playlistItem.getImagePath()))
                    .error(R.drawable.default_album_art)
                    .into(playlistImage);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
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

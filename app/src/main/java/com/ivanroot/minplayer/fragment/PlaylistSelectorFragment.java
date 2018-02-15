package com.ivanroot.minplayer.fragment;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hwangjr.rxbus.Bus;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.adapter.PlaylistSelectorAdapter;
import com.ivanroot.minplayer.player.RxBus;
import com.ivanroot.minplayer.playlist.PlaylistManager;
import com.ivanroot.minplayer.storio.PlaylistTable;
import com.simplecityapps.recyclerview_fastscroll.interfaces.OnFastScrollStateChangeListener;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Created by Ivan Root on 12.02.2018.
 */

public class PlaylistSelectorFragment extends NavFragmentBase{

    public static final String NAME = "PlaylistSelectorFragment";
    private FastScrollRecyclerView recyclerView;
    private PlaylistSelectorAdapter adapter;
    private FloatingActionButton addFab;



    @Override
    public String toString() {
        return NAME;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new PlaylistSelectorAdapter(getActivity());
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.playlist_selector_layout, container, false);
        setupDrawer(view);
        setupRecycler(view);
        addFab = (FloatingActionButton)view.findViewById(R.id.add_playlist_fab);
        addFab.setOnClickListener(v -> showPlaylistCreationDialog());
        adapter.setPlaylistClickListener((playlistName -> {
            PlaylistRecyclerFragment playlistRecyclerFragment = new PlaylistRecyclerFragment(playlistName);
            getActivity()
                    .getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentHolder,playlistRecyclerFragment,playlistRecyclerFragment.toString())
                    .commit();
        }));
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String title = getResources().getString(R.string.playlists);
        activity.getSupportActionBar().setTitle(title);
    }

    private void setupRecycler(View view) {

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView = (FastScrollRecyclerView)view.findViewById(R.id.playlist_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setStateChangeListener(new OnFastScrollStateChangeListener() {

            @Override
            public void onFastScrollStart() {

            }

            @Override
            public void onFastScrollStop() {

            }

        });

        recyclerView.setAdapter(adapter);
    }

    private void showPlaylistCreationDialog(){
        PlaylistAddDialog dialog = new PlaylistAddDialog();
        String tag = getResources().getString(R.string.add_playlist);
        dialog.show(getFragmentManager(),tag);
    }

    @Override
    public void onDestroy() {
        if (adapter != null)
            adapter.dispose();
        super.onDestroy();
    }
}

package com.ivanroot.minplayer.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hwangjr.rxbus.Bus;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.adapter.PlaylistRecyclerAdapter;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.player.RxBus;
import com.ivanroot.minplayer.playlist.Playlist;
import com.ivanroot.minplayer.playlist.PlaylistItem;
import com.ivanroot.minplayer.playlist.PlaylistManager;
import com.ivanroot.minplayer.utils.Utils;
import com.simplecityapps.recyclerview_fastscroll.interfaces.OnFastScrollStateChangeListener;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.squareup.picasso.Picasso;

import java.io.File;

import static com.ivanroot.minplayer.player.PlayerActionsEvents.ACTION_PLAY_AUDIO;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.ACTION_SET_PLAYLIST;

/**
 * Created by Ivan Root on 17.12.2017.
 */

public class PlaylistFragment extends NavFragmentBase {

    public static final String NAME = "PlaylistFragment";
    private static final String BUNDLE_RECYCLER_LAYOUT = "classname.recycler.layout";


    private PlaylistItem playlistItem = null;
    private String playlistName = null;
    private PlaylistRecyclerAdapter adapter;
    private FastScrollRecyclerView audioRecyclerView;
    private FloatingActionButton playFab;
    private PlaylistManager playlistManager;
    private AppBarLayout appBarLayout;
    private ImageView playlistImage = null;
    private ImageView[] playlistImages = null;
    private Bus rxBus = RxBus.getInstance();

    public PlaylistFragment(){}

    public PlaylistFragment(@NonNull String playlistName){
        this.playlistName = playlistName;
    }

    public PlaylistFragment(@NonNull PlaylistItem playlistItem) {
        this.playlistItem = playlistItem;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        playlistManager = PlaylistManager.getInstance();

        if(savedInstanceState == null) {
            if(playlistItem != null)
                playlistName = playlistItem.getName();

        }
        else {
            playlistName = savedInstanceState.getString("playlist_name");
        }

        adapter = new PlaylistRecyclerAdapter(getActivity(), playlistName);
        rxBus.register(this);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = getView(inflater, container);
        setupDrawer(view);
        setupRecycler(view);


        if (savedInstanceState != null) {
            audioRecyclerView.getLayoutManager()
                    .onRestoreInstanceState(savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT));
        }

        if(!playlistName.equals(PlaylistManager.ALL_TRACKS_PLAYLIST)) {
            playlistImages = new ImageView[]{
                    view.findViewById(R.id.SubPlaylistImage1),
                    view.findViewById(R.id.SubPlaylistImage2),
                    view.findViewById(R.id.SubPlaylistImage3),
                    view.findViewById(R.id.SubPlaylistImage4)

            };
        }

        prepareListeners(view);
        return view;
    }

    private void prepareListeners(View view) {


        if(!playlistName.equals(PlaylistManager.ALL_TRACKS_PLAYLIST)) {

            playFab = view.findViewById(R.id.fab_play);
            playFab.setOnClickListener(v -> {
                rxBus.post(ACTION_SET_PLAYLIST, playlistName);
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

        adapter.setNewPlaylistUpdateListener(this::setImages);
    }

    private View getView(LayoutInflater inflater, @Nullable ViewGroup container) {

        int layoutResource = (playlistName.equals(PlaylistManager.ALL_TRACKS_PLAYLIST)
                ? R.layout.all_tracks_recycler_layout
                : R.layout.playlist_recycler_layout);
        View view = inflater.inflate(layoutResource, container, false);

        appBarLayout = view.findViewById(R.id.app_bar);

        return view;
    }

    private void setupRecycler(View view) {

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        audioRecyclerView = view.findViewById(R.id.audio_recycler);
        audioRecyclerView.setHasFixedSize(true);
        audioRecyclerView.setLayoutManager(layoutManager);
        //DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(audioRecyclerView.getContext(),LinearLayoutManager.VERTICAL);
        //audioRecyclerView.addItemDecoration(mDividerItemDecoration);
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


    public String getPlaylistName(){
        return playlistName;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String title = playlistManager.getTitleFromPlaylistName(getActivity(),playlistName);
        activity.getSupportActionBar().setTitle(title);
        if(!playlistName.equals(PlaylistManager.ALL_TRACKS_PLAYLIST)) {

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, audioRecyclerView.getLayoutManager().onSaveInstanceState());
        outState.putString("playlist_name",playlistName);
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

    private void setImages(Playlist playlist){
        try {
            int i = 0;
            for (Audio audio : playlist.getAudioList()) {
                if (i == 4) break;
                String imagePath = audio.getAlbumArtPath();
                if (imagePath != null) {
                    Picasso.with(getActivity())
                            .load(Utils.getFileFromPath(imagePath))
                            .into(playlistImages[i]);

                    i++;
                }
            }
        }catch (NullPointerException ex){
            ex.printStackTrace();
            Log.e(toString(),ex.getMessage());
        }
    }
}

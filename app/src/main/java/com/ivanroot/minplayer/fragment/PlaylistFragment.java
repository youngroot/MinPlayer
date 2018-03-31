package com.ivanroot.minplayer.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hwangjr.rxbus.Bus;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.adapter.PlaylistAdapter;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.player.RxBus;
import com.ivanroot.minplayer.playlist.Playlist;
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


    private PlaylistItem playlistItem = null;
    private String playlistName = null;
    private PlaylistAdapter adapter;
    private FastScrollRecyclerView audioRecyclerView;
    private FloatingActionButton playFab;
    private PlaylistManager playlistManager;
    private AppBarLayout appBarLayout;
    private ImageView playlistImage = null;
    private ImageView[] playlistImages = null;
    private CardView playlistHeaderCard;
    private TextView playlistNameView;
    private TextView playlistSizeView;
    private Bus rxBus = RxBus.getInstance();
    private boolean selectorDialogIsActive = false;
    private Audio selectedAudio;

    public PlaylistFragment(){}

    public PlaylistFragment(@NonNull String playlistName){
        this.playlistName = playlistName;
    }

    public PlaylistFragment(@NonNull PlaylistItem playlistItem) {
        this.playlistItem = playlistItem;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playlistManager = PlaylistManager.getInstance();

        if(savedInstanceState == null) {
            if(playlistItem != null)
                playlistName = playlistItem.getName();

        }
        else {
            playlistName = savedInstanceState.getString("playlist_name");
        }

        adapter = new PlaylistAdapter(getActivity(), playlistName);
        rxBus.register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = getView(inflater, container);
        setupDrawer(view);
        setupRecycler(view);

        if (savedInstanceState != null) {
            audioRecyclerView
                    .getLayoutManager()
                    .onRestoreInstanceState(savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT));
        }

        if(!playlistName.equals(PlaylistManager.ALL_TRACKS_PLAYLIST)) {
            playlistHeaderCard = (CardView)view.findViewById(R.id.playlistHeaderCard);
            //playlistHeaderCard.getBackground().setAlpha(100);
            playlistNameView = (TextView)view.findViewById(R.id.playlistName);
            playlistSizeView = (TextView)view.findViewById(R.id.playlistSize);
            playlistImages = new ImageView[]{
                    (ImageView) view.findViewById(R.id.SubPlaylistImage1),
                    (ImageView) view.findViewById(R.id.SubPlaylistImage2),
                    (ImageView) view.findViewById(R.id.SubPlaylistImage3),
                    (ImageView) view.findViewById(R.id.SubPlaylistImage4)

            };
        }

        prepareListeners(view);
        return view;
    }

    private void prepareListeners(View view) {

        if(!playlistName.equals(PlaylistManager.ALL_TRACKS_PLAYLIST)) {

            playFab = (FloatingActionButton) view.findViewById(R.id.fab_play);
            playFab.setOnClickListener(v -> {
                rxBus.post(ACTION_SET_PLAYLIST, playlistName);
                if(adapter.getPlaylist().size() > 0) {
                    rxBus.post(ACTION_PLAY_AUDIO, adapter.getPlaylist().getAudio(0));
                }
                appBarLayout.setExpanded(false, true);
            });
        }

        adapter.setOnMoreBtnClickListener((v, playlist, i) -> {

            PopupMenu popupMenu = new PopupMenu(getActivity(), v);
            popupMenu.inflate(R.menu.audio_item_more_menu);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.add_to_playlist:
                        selectedAudio = adapter.getPlaylist().getAudio(i);
                        showPlaylistSelectionDialog(selectedAudio);
                        return true;
                    default:
                        return false;
                }
            });
            popupMenu.show();
        });

        adapter.setAudioClickListener((audio, playlistName) -> {
            rxBus.post(ACTION_SET_PLAYLIST,playlistName);
            rxBus.post(ACTION_PLAY_AUDIO,audio);

        });

        adapter.setNewPlaylistUpdateListener(playlist -> {
            setImages(playlist);
            setText(playlist);
        });
    }

    private View getView(LayoutInflater inflater, @Nullable ViewGroup container) {

        int layoutResource = (playlistName.equals(PlaylistManager.ALL_TRACKS_PLAYLIST)
                ? R.layout.all_tracks_playlist_fragment
                : R.layout.playlist_fragment);
        View view = inflater.inflate(layoutResource, container, false);

        appBarLayout = (AppBarLayout) view.findViewById(R.id.app_bar);

        return view;
    }

    private void setupRecycler(View view) {

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        audioRecyclerView = (FastScrollRecyclerView) view.findViewById(R.id.audio_recycler);
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

        audioRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            private String logMessage;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState){
                logMessage = "onScrollStateChanged: new state: " + newState;
                Log.i(toString(),logMessage);

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                logMessage = "onScrolled dx: " + dx + " dy: " + dy;
                Log.i(toString(),logMessage);
            }

        });

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

        if(savedInstanceState != null){
            selectorDialogIsActive = savedInstanceState.getBoolean("selector_dialog_is_active");
            String json = savedInstanceState.getString("selected_audio");
            selectedAudio = new Gson().fromJson(json,Audio.class);

            if(selectorDialogIsActive){
                PlaylistSelectorDialog dialog = (PlaylistSelectorDialog)getActivity()
                        .getFragmentManager()
                        .findFragmentByTag(getActivity().getResources().getString(R.string.add_to_playlist));
                if(dialog != null){
                    dialog.setPlaylistItemClickListener(playlistItem -> {
                        playlistManager.addToPlaylist(getActivity(),playlistItem.getName(),selectedAudio);
                    dialog.setDialogDismissListener(dialogInterface -> selectorDialogIsActive = false);
                    });
                }
            }

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Gson gson = new Gson();
        String json = gson.toJson(selectedAudio);
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, audioRecyclerView.getLayoutManager().onSaveInstanceState());
        outState.putString("playlist_name",playlistName);
        outState.putBoolean("selector_dialog_is_active",selectorDialogIsActive);
        outState.putString("selected_audio",json);
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

    private void setText(Playlist playlist){
       try {

           playlistNameView.setText(playlist.getName());
           String playlistSize = playlist.size()+ " " + getActivity().getResources().getString(R.string.songs);
           playlistSizeView.setText(playlistSize);
       }catch (NullPointerException ex){
           ex.printStackTrace();
           Log.e(toString(),ex.getMessage());
       }

    }

    public void showPlaylistSelectionDialog(Audio audio){
        PlaylistSelectorDialog dialog = new PlaylistSelectorDialog();
        dialog.setPlaylistItemClickListener(playlistItem -> playlistManager.addToPlaylist(getActivity(),playlistItem.getName(),audio));
        dialog.show(getActivity().getFragmentManager(),getActivity().getResources().getString(R.string.add_to_playlist));
        selectorDialogIsActive = true;
        dialog.setDialogDismissListener(dialogInterface -> selectorDialogIsActive = false);
    }

}

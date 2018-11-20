package com.ivanroot.minplayer.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hwangjr.rxbus.Bus;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.activity.SearchActivity;
import com.ivanroot.minplayer.adapter.PlaylistAdapter;
import com.ivanroot.minplayer.adapter.SimpleItemTouchHelperCallback;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.disk.constants.AudioStatus;
import com.ivanroot.minplayer.disk.service.AudioTransferServiceBase;
import com.ivanroot.minplayer.disk.service.AudioUploadService;
import com.ivanroot.minplayer.playlist.Playlist;
import com.ivanroot.minplayer.playlist.PlaylistItem;
import com.ivanroot.minplayer.playlist.PlaylistManager;
import com.ivanroot.minplayer.utils.Utils;
import com.simplecityapps.recyclerview_fastscroll.interfaces.OnFastScrollStateChangeListener;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.squareup.picasso.Picasso;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static com.ivanroot.minplayer.player.constants.PlayerActions.ACTION_PLAY_AUDIO;
import static com.ivanroot.minplayer.player.constants.PlayerActions.ACTION_SET_PLAYLIST;

/**
 * Created by Ivan Root on 17.12.2017.
 */

public class PlaylistFragment extends NavFragmentBase {

    public static final String NAME = "PlaylistFragment";
    private static final String BUNDLE_RECYCLER_LAYOUT = "classname.recycler.layout";


    private PlaylistItem playlistItem = null;
    protected String playlistName = null;
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
    private EditText playlistEditedNameView;
    private ImageButton searchButton;
    private ImageButton moreButton;
    private Button playlistModifyCancelButton;
    private Button playlistModifyOkButton;
    private Bus rxBus = RxBus.get();
    private boolean selectorDialogIsActive = false;
    private Audio selectedAudio;
    private LinearLayoutManager layoutManager;
    private RecyclerView.SmoothScroller smoothScroller;
    private boolean playlistModifyModeEnabled = false;
    private ItemTouchHelper itemTouchHelper;
    private Observable<Playlist> playlistObservable;

    public PlaylistFragment() {
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playlistManager = PlaylistManager.getInstance();

        if (savedInstanceState == null) {
            if (playlistItem != null)
                playlistName = playlistItem.getName();

        } else {
            playlistName = savedInstanceState.getString("playlist_name");
        }

        adapter = new PlaylistAdapter(getActivity());
        rxBus.register(this);

        setupPlaylistObservable();
    }

    private void setupPlaylistObservable() {
        playlistObservable = playlistManager.getPlaylistObservable(getActivity(), playlistName)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(playlist -> {
                    setImages(playlist);
                    setText(playlist);
                });
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
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

        if (!playlistName.equals(PlaylistManager.ALL_TRACKS_PLAYLIST)) {
            playlistHeaderCard = (CardView) view.findViewById(R.id.playlist_header_card);
            //playlistHeaderCard.getBackground().setAlpha(100);
            playlistNameView = (TextView) view.findViewById(R.id.playlist_name);
            playlistSizeView = (TextView) view.findViewById(R.id.playlist_size);
            playlistEditedNameView = (EditText) view.findViewById(R.id.playlist_name_edit);
            playlistImages = new ImageView[]{
                    (ImageView) view.findViewById(R.id.sub_playlist_image_1),
                    (ImageView) view.findViewById(R.id.sub_playlist_image_2),
                    (ImageView) view.findViewById(R.id.sub_playlist_image_3),
                    (ImageView) view.findViewById(R.id.sub_playlist_image_4)

            };

            moreButton = (ImageButton) view.findViewById(R.id.playlist_more_btn);
            playlistModifyCancelButton = (Button) view.findViewById(R.id.cancel_btn);
            playlistModifyOkButton = (Button) view.findViewById(R.id.ok_btn);
        } else {
            searchButton = (ImageButton) view.findViewById(R.id.search_button);
            searchButton.setVisibility(View.VISIBLE);
        }

        prepareListeners(view);
        return view;
    }

    private void prepareListeners(View view) {

        if (!playlistName.equals(PlaylistManager.ALL_TRACKS_PLAYLIST)) {

            playFab = (FloatingActionButton) view.findViewById(R.id.fab_play);
            playFab.setOnClickListener(v -> {
                rxBus.post(ACTION_SET_PLAYLIST, playlistName);
                if (adapter.getItemCount() > 0) {
                    rxBus.post(ACTION_PLAY_AUDIO, adapter.getPlaylist().getAudio(0));
                }
                appBarLayout.setExpanded(false, true);
            });

            moreButton.setOnClickListener(v -> {
                View infoHolder = view.findViewById(R.id.info_layout);
                PopupMenu popupMenu = new PopupMenu(activity, infoHolder);
                popupMenu.getMenu().add(getResources().getString(R.string.modify));
                popupMenu.setOnMenuItemClickListener(item -> {
                    setPlaylistModifyModeEnabled(true);
                    return true;
                });
                popupMenu.show();
            });

            adapter.setOnModifiedPlaylistSaveListener(modifiedPlaylist -> {
                playlistManager.writePlaylist(activity, modifiedPlaylist);
                String editedName = playlistEditedNameView.getText().toString();
                Log.i(toString(), "OnSavePlaylist");

                if (!editedName.isEmpty() && playlistName != null && !playlistName.equals(editedName)) {
                    playlistManager.renamePlaylist(activity, playlistName, editedName);
                    playlistName = editedName;

                    setupPlaylistObservable();
                    adapter.subscribe(playlistObservable);
                    rxBus.post(ACTION_SET_PLAYLIST, playlistName);
                }
            });

            playlistModifyCancelButton.setOnClickListener(v -> setPlaylistModifyModeEnabled(false));

            playlistModifyOkButton.setOnClickListener(v -> {
                Log.i(toString(), "OnSaveButtonClick");
                adapter.saveModifiedPlaylist();
                setPlaylistModifyModeEnabled(false);
            });


        } else {
            searchButton.setOnClickListener(v -> startActivity(new Intent(activity, SearchActivity.class)));
        }

        adapter.setOnMoreBtnClickListener((v, playlist, i) -> {

            PopupMenu popupMenu = new PopupMenu(activity, v);
            popupMenu.inflate(R.menu.audio_item_more_menu);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.add_to_playlist:
                        selectedAudio = playlist.getAudio(i);
                        showPlaylistSelectionDialog(selectedAudio);
                        return true;

                    case R.id.upload_to_disk:
                        Intent intent = AudioTransferServiceBase
                                .getIntentFromAudio(activity, playlist.getAudio(i),
                                        AudioUploadService.class);
                        activity.startService(intent);
                        return true;

                    default:
                        return false;
                }
            });
            popupMenu.show();
        });

        adapter.setAudioClickListener((audio, playlistName) -> {
            rxBus.post(ACTION_SET_PLAYLIST, playlistName);
            rxBus.post(ACTION_PLAY_AUDIO, audio);
        });

//        adapter.setNewPlaylistUpdateListener(playlist -> {
//            setImages(playlist);
//            setText(playlist);
//        });

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

        layoutManager = new LinearLayoutManager(getActivity());
        smoothScroller = new LinearSmoothScroller(activity) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };

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
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                logMessage = "onScrollStateChanged: new state: " + newState;
                Log.i(toString(), logMessage);

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                logMessage = "onScrolled dx: " + dx + " dy: " + dy;
                Log.i(toString(), logMessage);
            }

        });

        audioRecyclerView.setAdapter(adapter);
    }


    public String getPlaylistName() {
        return playlistName;
    }

    @Override
    protected String getActionBarTitle() {
        return playlistManager.getTitleFromPlaylistName(getActivity(), playlistName);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            selectorDialogIsActive = savedInstanceState.getBoolean("selector_dialog_is_active");
            String json = savedInstanceState.getString("selected_audio");
            selectedAudio = new Gson().fromJson(json, Audio.class);

            if (selectorDialogIsActive) {
                PlaylistSelectorDialog dialog = (PlaylistSelectorDialog) getActivity()
                        .getFragmentManager()
                        .findFragmentByTag(getActivity().getResources().getString(R.string.add_to_playlist));
                if (dialog != null) {
                    dialog.setPlaylistItemClickListener(playlistItem -> {
                        playlistManager.addToPlaylist(getActivity(), playlistItem.getName(), selectedAudio);
                        dialog.setDialogDismissListener(dialogInterface -> selectorDialogIsActive = false);
                    });
                }
            }

            int pos = savedInstanceState.getInt("pos", 0);
            //smoothScroller.setTargetPosition(pos);
            //layoutManager.startSmoothScroll(smoothScroller);
            layoutManager.scrollToPositionWithOffset(pos, 0);
            Log.i(toString(), "pos: " + pos);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.subscribe(playlistObservable);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Gson gson = new Gson();
        String json = gson.toJson(selectedAudio);
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, audioRecyclerView.getLayoutManager().onSaveInstanceState());
        outState.putString("playlist_name", playlistName);
        outState.putBoolean("selector_dialog_is_active", selectorDialogIsActive);
        outState.putString("selected_audio", json);
        outState.putInt("pos", layoutManager.findFirstVisibleItemPosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        adapter.dispose();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        rxBus.unregister(this);
        super.onDestroy();
    }

    private void setImages(Playlist playlist) {
        try {
            for(int i = 0; i < playlistImages.length; i++)
                playlistImages[i].setImageResource(0);

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
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            Log.e(toString(), ex.getMessage());
        }
    }

    private void setText(Playlist playlist) {
        try {
            playlistNameView.setText(playlist.getName());
            String playlistSize = playlist.size() + " " + getActivity().getResources().getString(R.string.songs);
            playlistSizeView.setText(playlistSize);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            //Log.e(toString(), ex.getMessage());
        }

    }

    public void showPlaylistSelectionDialog(Audio audio) {
        PlaylistSelectorDialog dialog = new PlaylistSelectorDialog();
        dialog.setPlaylistItemClickListener(playlistItem -> playlistManager.addToPlaylist(getActivity(), playlistItem.getName(), audio));
        dialog.show(getActivity().getFragmentManager(), getActivity().getResources().getString(R.string.add_to_playlist));
        selectorDialogIsActive = true;
        dialog.setDialogDismissListener(dialogInterface -> selectorDialogIsActive = false);
    }

    @Subscribe(tags = {@Tag(AudioStatus.STATUS_AUDIO_UPLOADED)})
    public void showAudioUploadedToast(Audio taskAudio) {
        Toast.makeText(activity, getString(R.string.upload_completed) + ": " + taskAudio.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Subscribe(tags = {@Tag(AudioStatus.STATUS_ALL_AUDIOS_UPLOADED)})
    public void showAllAudiosUploadedToast(Object object) {
        Toast.makeText(activity, getString(R.string.all_tracks_uploaded), Toast.LENGTH_SHORT).show();
    }

    public void setPlaylistModifyModeEnabled(boolean playlistModifyModeEnabled) {
        Log.i(toString(), "setPlaylistModifyModeEnabled " + playlistModifyModeEnabled);
        this.playlistModifyModeEnabled = playlistModifyModeEnabled;
        adapter.setPlaylistModifyModeEnabled(playlistModifyModeEnabled);

        if (playlistModifyModeEnabled) {
            if (itemTouchHelper == null) {
                ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
                itemTouchHelper = new ItemTouchHelper(callback);
                adapter.setItemTouchHelper(itemTouchHelper);
            }

            playlistNameView.setVisibility(View.INVISIBLE);
            playlistSizeView.setVisibility(View.INVISIBLE);
            moreButton.setVisibility(View.INVISIBLE);
            playFab.setVisibility(View.INVISIBLE);

            playlistEditedNameView.setVisibility(View.VISIBLE);
            playlistModifyCancelButton.setVisibility(View.VISIBLE);
            playlistModifyOkButton.setVisibility(View.VISIBLE);

            playlistEditedNameView.setText(adapter.getPlaylist().getName());

            itemTouchHelper.attachToRecyclerView(audioRecyclerView);
        } else {
            playlistEditedNameView.setVisibility(View.INVISIBLE);
            playlistModifyCancelButton.setVisibility(View.INVISIBLE);
            playlistModifyOkButton.setVisibility(View.INVISIBLE);

            playlistNameView.setVisibility(View.VISIBLE);
            playlistSizeView.setVisibility(View.VISIBLE);
            moreButton.setVisibility(View.VISIBLE);
            playFab.setVisibility(View.VISIBLE);

            if (itemTouchHelper != null)
                itemTouchHelper.attachToRecyclerView(null);
        }
    }
}

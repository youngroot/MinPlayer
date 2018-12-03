package com.ivanroot.minplayer.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.ivanroot.minplayer.exceptions.PlaylistAlreadyExistsException;
import com.ivanroot.minplayer.playlist.Playlist;
import com.ivanroot.minplayer.playlist.PlaylistItem;
import com.ivanroot.minplayer.playlist.PlaylistManager;
import com.ivanroot.minplayer.utils.Pair;
import com.ivanroot.minplayer.utils.Utils;
import com.simplecityapps.recyclerview_fastscroll.interfaces.OnFastScrollStateChangeListener;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import jp.wasabeef.blurry.Blurry;

import static com.ivanroot.minplayer.player.constants.PlayerActions.ACTION_ON_PLAYLIST_NAME_CHANGED;
import static com.ivanroot.minplayer.player.constants.PlayerActions.ACTION_PLAY_AUDIO;
import static com.ivanroot.minplayer.player.constants.PlayerActions.ACTION_SET_PLAYLIST;

/**
 * Created by Ivan Root on 17.12.2017.
 */

public class PlaylistFragment extends NavFragmentBase {
    public static final String NAME = "PlaylistFragment";
    private static final String BUNDLE_RECYCLER_LAYOUT = "classname.recycler.layout";

    protected long playlistId;

    private PlaylistAdapter adapter;
    private FastScrollRecyclerView audioRecyclerView;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private FloatingActionButton playFab;
    private PlaylistManager playlistManager;
    private AppBarLayout appBarLayout;
    private ImageView bigPlaylistImage;
    private ImageView[] playlistImages;
    private TextView playlistNameView;
    private TextView playlistSizeView;
    private EditText playlistEditedNameView;
    private View playlistEditedNameViewHolder;
    private ImageButton searchButton;
    private ImageButton moreButton;
    private ImageButton playlistModifyButton;
    private ImageButton playlistModifyCancelButton;
    private ImageButton playlistModifyOkButton;
    private Bus rxBus = RxBus.get();
    private boolean selectorDialogIsActive = false;
    private Audio selectedAudio;
    private LinearLayoutManager layoutManager;
    private boolean playlistModifyModeEnabled = false;
    private ItemTouchHelper itemTouchHelper;
    private Observable<Playlist> playlistObservable;
    private SlidingUpPanelLayout.PanelState prevState = SlidingUpPanelLayout.PanelState.HIDDEN;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playlistManager = PlaylistManager.getInstance();

        if (savedInstanceState != null){
            playlistId = savedInstanceState.getLong("playlist_id");
        }

        adapter = new PlaylistAdapter(getActivity());
        rxBus.register(this);

        setupPlaylistObservable();
    }

    private void setupPlaylistObservable() {
        playlistObservable = playlistManager.getPlaylistObservable(getActivity(), playlistId)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(playlist -> {
                    setImages(playlist);
                    setText(playlist);
                });
    }

    public void setPlaylistId(long playlistId) {
        this.playlistId = playlistId;
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

        if (!Objects.equals(playlistId, PlaylistManager.ALL_TRACKS_PLAYLIST_ID)) {
            collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar_layout);
            playlistNameView = (TextView) view.findViewById(R.id.playlist_name);
            playlistSizeView = (TextView) view.findViewById(R.id.playlist_size);
            playlistEditedNameView = (EditText) view.findViewById(R.id.playlist_name_edit);
            playlistEditedNameViewHolder = view.findViewById(R.id.playlist_name_edit_holder);

            bigPlaylistImage = (ImageView) view.findViewById(R.id.big_playlist_image);

            playlistImages = new ImageView[]{
                    (ImageView) view.findViewById(R.id.sub_playlist_image_1),
                    (ImageView) view.findViewById(R.id.sub_playlist_image_2),
                    (ImageView) view.findViewById(R.id.sub_playlist_image_3),
                    (ImageView) view.findViewById(R.id.sub_playlist_image_4)

            };

            moreButton = (ImageButton) view.findViewById(R.id.playlist_more_btn);
            playlistModifyButton = (ImageButton) view.findViewById(R.id.playlist_modify_btn);
            playlistModifyCancelButton = (ImageButton) view.findViewById(R.id.cancel_btn);
            playlistModifyOkButton = (ImageButton) view.findViewById(R.id.ok_btn);
        } else {
            searchButton = (ImageButton) view.findViewById(R.id.search_button);
            searchButton.setVisibility(View.VISIBLE);
        }

        prepareListeners(view);
        return view;
    }

    private void prepareListeners(View view) {
        if (!Objects.equals(playlistId, PlaylistManager.ALL_TRACKS_PLAYLIST_ID)) {

            playFab = (FloatingActionButton) view.findViewById(R.id.fab_play);
            playFab.setOnClickListener(v -> {
                rxBus.post(ACTION_SET_PLAYLIST, playlistId);
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

            playlistModifyButton.setOnClickListener(v -> setPlaylistModifyModeEnabled(true));

            playlistModifyCancelButton.setOnClickListener(v -> setPlaylistModifyModeEnabled(false));

            playlistModifyOkButton.setOnClickListener(v -> {
                Log.i(toString(), "OnSaveButtonClick");
                String editedName = playlistEditedNameView.getText().toString();

                if (adapter.getItemCount() == 0) {
                    Toast.makeText(activity, getResources().getString(R.string.playlist_cannot_be_empty), Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                if (editedName.isEmpty()) {
                    Toast.makeText(activity, getResources().getString(R.string.playlist_name_cannot_be_empty), Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                playlistManager.renamePlaylist(activity, playlistId, editedName);

                setupPlaylistObservable();
                adapter.saveModifiedPlaylist();

            });

            adapter.setOnModifiedPlaylistSaveListener(modifiedPlaylist -> {
                playlistManager.writePlaylist(activity, modifiedPlaylist);
                adapter.subscribe(playlistObservable);
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
    }

    private View getView(LayoutInflater inflater, @Nullable ViewGroup container) {
        int layoutResource = (Objects.equals(playlistId, PlaylistManager.ALL_TRACKS_PLAYLIST_ID)
                ? R.layout.all_tracks_playlist_fragment
                : R.layout.playlist_fragment);
        View view = inflater.inflate(layoutResource, container, false);

        appBarLayout = (AppBarLayout) view.findViewById(R.id.app_bar);

        return view;
    }

    private void setupRecycler(View view) {
        layoutManager = new LinearLayoutManager(getActivity());

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

    @Override
    public String getActionBarTitle() {
        return "";
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

            layoutManager.scrollToPositionWithOffset(pos, 0);
            Log.i(toString(), "pos: " + pos);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.subscribe(playlistObservable);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Gson gson = new Gson();
        String json = gson.toJson(selectedAudio);

        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, audioRecyclerView.getLayoutManager().onSaveInstanceState());
        outState.putLong("playlist_id", playlistId);
        outState.putBoolean("selector_dialog_is_active", selectorDialogIsActive);
        outState.putString("selected_audio", json);
        outState.putInt("pos", layoutManager.findFirstVisibleItemPosition());

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();
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
            bigPlaylistImage.setImageResource(0);

            for (ImageView playlistImage : playlistImages)
                playlistImage.setImageResource(0);

            Bitmap bitmap = null;

            int i = 0;
            for (Audio audio : playlist.getAudioList()) {
                if (i == 4)
                    break;

                String imagePath = audio.getAlbumArtPath();

                if (imagePath != null) {
                    Picasso.with(getActivity())
                            .load(Utils.getFileFromPath(imagePath))
                            .into(playlistImages[i]);


                    if (bitmap == null) {
                        bitmap = Utils.getAudioAlbumArt(imagePath,
                                BitmapFactory.decodeResource(getResources(), R.drawable.lowpoly_grey));

                        Blurry.with(getActivity())
                                .async()
                                .sampling(2)
                                .color(Color.argb(5, 20, 20, 20))
                                .from(bitmap)
                                .into(bigPlaylistImage);
                    }

                    i++;
                }
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            Log.e(toString(), ex.getMessage());
        }
    }

    @Override
    public void setActionBarTitle(String title) {
        super.setActionBarTitle(title);
        if (collapsingToolbarLayout != null)
            collapsingToolbarLayout.setTitle(title);
    }

    private void setText(Playlist playlist) {
        try {
            String playlistName = playlist.getName();

            if (playlistName != null) {
                setActionBarTitle(playlistManager.getTitleFromPlaylistName(activity, playlistName));
                playlistNameView.setText(playlistName);
            }

            playlistSizeView.setText(getResources().getQuantityString(R.plurals.song_plurals, playlist.size(), playlist.size()));
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
        SlidingUpPanelLayout panelLayout = activity.getPanelLayout();

        if (playlistModifyModeEnabled) {
            if (itemTouchHelper == null) {
                ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
                itemTouchHelper = new ItemTouchHelper(callback);
                adapter.setItemTouchHelper(itemTouchHelper);
            }

            playlistNameView.setVisibility(View.INVISIBLE);
            playlistSizeView.setVisibility(View.INVISIBLE);
            //moreButton.setVisibility(View.INVISIBLE);
            playlistModifyButton.setVisibility(View.INVISIBLE);
            playFab.setVisibility(View.INVISIBLE);

            playlistEditedNameViewHolder.setVisibility(View.VISIBLE);
            playlistModifyCancelButton.setVisibility(View.VISIBLE);
            playlistModifyOkButton.setVisibility(View.VISIBLE);

            playlistEditedNameView.setInputType(InputType.TYPE_CLASS_TEXT);
            playlistEditedNameView.setText(adapter.getPlaylist().getName());

            itemTouchHelper.attachToRecyclerView(audioRecyclerView);

            prevState = panelLayout.getPanelState();
            panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        } else {
            playlistEditedNameView.setInputType(InputType.TYPE_NULL);
            playlistEditedNameView.setSelected(false);

            playlistEditedNameViewHolder.setVisibility(View.INVISIBLE);

            playlistModifyCancelButton.setVisibility(View.INVISIBLE);
            playlistModifyOkButton.setVisibility(View.INVISIBLE);

            playlistNameView.setVisibility(View.VISIBLE);
            playlistSizeView.setVisibility(View.VISIBLE);
            //moreButton.setVisibility(View.VISIBLE);
            playlistModifyButton.setVisibility(View.VISIBLE);
            playFab.setVisibility(View.VISIBLE);

            if (itemTouchHelper != null)
                itemTouchHelper.attachToRecyclerView(null);

            panelLayout.setPanelState(prevState);
        }
    }
}

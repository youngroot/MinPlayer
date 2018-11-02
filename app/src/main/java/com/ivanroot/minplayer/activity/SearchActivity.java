package com.ivanroot.minplayer.activity;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.gson.Gson;
import com.hwangjr.rxbus.Bus;
import com.hwangjr.rxbus.RxBus;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.adapter.listeners.OnAudioClickListener;
import com.ivanroot.minplayer.adapter.listeners.OnMoreBtnClickListener;
import com.ivanroot.minplayer.adapter.section.AudioSection;
import com.ivanroot.minplayer.adapter.section.PlaylistItemSection;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.disk.service.AudioTransferServiceBase;
import com.ivanroot.minplayer.disk.service.AudioUploadService;
import com.ivanroot.minplayer.fragment.PlaylistSelectorDialog;
import com.ivanroot.minplayer.player.constants.PlayerActions;
import com.ivanroot.minplayer.playlist.Playlist;
import com.ivanroot.minplayer.playlist.PlaylistItem;
import com.ivanroot.minplayer.playlist.PlaylistManager;
import com.ivanroot.minplayer.utils.Pair;

import java.util.ArrayList;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

public class SearchActivity extends NavUpActivityBase {

    private EditText editSearch;
    private ImageButton cancelButton;

    public SearchActivity() {
        super(R.layout.activity_search);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editSearch = (EditText) findViewById(R.id.edit_search);
        cancelButton = (ImageButton)findViewById(R.id.cancel_button);

        if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.search_holder, new SearchFragment())
                    .commit();
        }
    }

    public EditText getEditSearch() {
        return editSearch;
    }

    public ImageButton getCancelButton(){
        return cancelButton;
    }

    public static class SearchFragment extends Fragment {
        private EditText editSearch;
        private ImageButton cancelButton;
        private SearchActivity activity;
        private Subject<String> searchTextSubject = BehaviorSubject.create();
        private AudioSection audioSection;
        private PlaylistItemSection playlistItemSection;
        private SectionedRecyclerViewAdapter adapter;
        private RecyclerView recyclerView;
        private PlaylistManager playlistManager = PlaylistManager.getInstance();
        private Disposable disposable;
        private boolean selectorDialogIsActive = false;
        private Audio selectedAudio;
        private Bus rxBus = RxBus.get();


        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            rxBus.register(this);
            activity = (SearchActivity) getActivity();
            adapter = new SectionedRecyclerViewAdapter();
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.search_fragment, container, false);

            editSearch = activity.getEditSearch();
            cancelButton = activity.getCancelButton();

            editSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    searchTextSubject.onNext(charSequence.toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if(editable.toString().length() == 0)
                        cancelButton.setVisibility(View.INVISIBLE);
                    else
                        cancelButton.setVisibility(View.VISIBLE);
                }
            });


            cancelButton.setOnClickListener(v -> editSearch.getText().clear());

            audioSection = new AudioSection(activity);
            audioSection.setOnMoreBtnClickListener((v, playlist, i) -> {
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

            audioSection.setOnAudioClickListener((audio, playlistName) -> {
                rxBus.post(PlayerActions.ACTION_SET_PLAYLIST, playlistName);
                rxBus.post(PlayerActions.ACTION_PLAY_AUDIO, audio);
            });

            audioSection.setVisible(false);

            playlistItemSection = new PlaylistItemSection(activity);
            playlistItemSection.setVisible(false);

            adapter.addSection(audioSection);
            adapter.addSection(playlistItemSection);

            recyclerView = (RecyclerView) view.findViewById(R.id.search_recycler);
            recyclerView.setLayoutManager(new LinearLayoutManager(activity));
            recyclerView.setAdapter(adapter);
            return view;
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            disposable = searchTextSubject
                    .doOnNext(text -> Log.i(toString(), "onNext " + String.valueOf(text)))
                    .filter(s -> s != null && !s.equals(""))
                    .distinctUntilChanged()
                    .compose(getOnlyKeywordItemsTransformer())
                    .subscribe(pair -> {
                        Log.i(toString(), "subscribe " + pair);
                        audioSection.setAudioList(pair.first);
                        audioSection.setVisible(!pair.first.isEmpty());
                        playlistItemSection.setPlaylistItems(pair.second);
                        playlistItemSection.setVisible(!pair.second.isEmpty());
                        adapter.notifyDataSetChanged();
                    }, throwable -> {
                        Log.e(toString(), throwable.toString());
                    });

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
            }

        }


        @Override
        public void onSaveInstanceState(Bundle outState) {
            Gson gson = new Gson();
            String json = gson.toJson(selectedAudio);
            outState.putBoolean("selector_dialog_is_active", selectorDialogIsActive);
            outState.putString("selected_audio", json);
            super.onSaveInstanceState(outState);
        }

        private ObservableTransformer<String, Pair<List<Audio>, List<PlaylistItem>>> getOnlyKeywordItemsTransformer() {
            return upstream -> upstream
                    .flatMap(keyword ->
                            Observable.combineLatest(getOnlyKeywordAudiosObservable(keyword),
                                    getOnlyKeywordPlaylistItemsObservable(keyword), Pair::new)
                                    .take(1))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());

        }

        private Observable<List<Audio>> getOnlyKeywordAudiosObservable(@NonNull String keyword) {
            return playlistManager.getAllAudiosObservable(getActivity())
                    .map(audios -> {
                        List<Audio> result = new ArrayList<>();
                        for (Audio audio : audios) {
                            Log.i(toString(), "Working on " + audio.getTitle());
                            if (audio.getTitle() != null && audio.getTitle().startsWith(keyword))
                                result.add(audio);
                            else if (audio.getAlbum() != null && audio.getAlbum().startsWith(keyword))
                                result.add(audio);
                            else if (audio.getArtist() != null && audio.getArtist().startsWith(keyword))
                                result.add(audio);

                        }
                        return result;
                    });
        }

        private Observable<List<PlaylistItem>> getOnlyKeywordPlaylistItemsObservable(@NonNull String keyword) {
            return playlistManager.getPlaylistItemsObservable(getActivity())
                    .map(playlistItems -> {
                        List<PlaylistItem> result = new ArrayList<>();
                        for (PlaylistItem playlistItem : playlistItems)
                            if (playlistItem.getName() != null && playlistItem.getName().startsWith(keyword))
                                result.add(playlistItem);

                        return result;
                    });
        }

        @Override
        public void onDestroyView() {
            if (disposable != null)
                disposable.dispose();
            super.onDestroyView();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            rxBus.unregister(this);
        }

        public void showPlaylistSelectionDialog(Audio audio) {
            PlaylistSelectorDialog dialog = new PlaylistSelectorDialog();
            dialog.setPlaylistItemClickListener(playlistItem -> playlistManager.addToPlaylist(getActivity(), playlistItem.getName(), audio));
            dialog.show(getActivity().getFragmentManager(), getActivity().getResources().getString(R.string.add_to_playlist));
            selectorDialogIsActive = true;
            dialog.setDialogDismissListener(dialogInterface -> selectorDialogIsActive = false);
        }

    }
}

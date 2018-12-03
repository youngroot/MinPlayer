package com.ivanroot.minplayer.activity;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.google.gson.Gson;
import com.hwangjr.rxbus.Bus;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.adapter.listeners.OnAudioClickListener;
import com.ivanroot.minplayer.adapter.listeners.OnAudioMoreBtnClickListener;
import com.ivanroot.minplayer.adapter.section.AudioSection;
import com.ivanroot.minplayer.adapter.section.DiskAudioSection;
import com.ivanroot.minplayer.adapter.section.PlaylistItemSection;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.disk.RestClientUtil;
import com.ivanroot.minplayer.disk.constants.AudioStatus;
import com.ivanroot.minplayer.disk.service.AudioDownloadService;
import com.ivanroot.minplayer.disk.service.AudioTransferServiceBase;
import com.ivanroot.minplayer.disk.service.AudioUploadService;
import com.ivanroot.minplayer.fragment.PlaylistSelectorDialog;
import com.ivanroot.minplayer.player.constants.PlayerActions;
import com.ivanroot.minplayer.playlist.Playlist;
import com.ivanroot.minplayer.playlist.PlaylistManager;
import com.yandex.disk.rest.RestClient;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
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
        cancelButton = (ImageButton) findViewById(R.id.cancel_button);

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

    public ImageButton getCancelButton() {
        return cancelButton;
    }

    public static class SearchFragment extends Fragment {
        private EditText editSearch;
        private ImageButton cancelButton;
        private SearchActivity activity;
        private AudioSection audioSection;
        private DiskAudioSection diskAudioSection;
        private PlaylistItemSection playlistItemSection;
        private SectionedRecyclerViewAdapter adapter;
        private RecyclerView recyclerView;
        private PlaylistManager playlistManager = PlaylistManager.getInstance();
        private boolean selectorDialogIsActive = false;
        private Audio selectedAudio;
        private Disposable disposable;
        private RxSharedPreferences rxPreferences;
        private Bus rxBus = RxBus.get();


        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            rxBus.register(this);
            activity = (SearchActivity) getActivity();
            adapter = new SectionedRecyclerViewAdapter();
            rxPreferences = RxSharedPreferences.create(PreferenceManager.getDefaultSharedPreferences(activity));
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
                    String text = charSequence.toString();

                    if (!text.matches(""))
                        cancelButton.setVisibility(View.VISIBLE);
                    else {
                        cancelButton.setVisibility(View.INVISIBLE);
                        text = "\n\n\0";
                    }

                    audioSection.filter(text);
                    diskAudioSection.filter(text);
                    playlistItemSection.filter(text);
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            cancelButton.setOnClickListener(v -> editSearch.getText().clear());

            OnAudioClickListener onAudioClickListener = (audio, playlistName) -> {
                rxBus.post(PlayerActions.ACTION_SET_PLAYLIST, playlistName);
                rxBus.post(PlayerActions.ACTION_PLAY_AUDIO, audio);
            };

            audioSection = new AudioSection(activity, "audio_section", adapter);
            audioSection.setOnAudioMoreBtnClickListener((v, playlist, i) -> {
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

            audioSection.setOnAudioClickListener(onAudioClickListener);

            diskAudioSection = new DiskAudioSection(activity, "audio_item_disk_section", adapter);
            diskAudioSection.setOnAudioClickListener(onAudioClickListener);
            diskAudioSection.setOnAudioMoreBtnClickListener((v, playlist, i) -> activity.startService(AudioTransferServiceBase.getIntentFromAudio(activity, playlist.getAudio(i), AudioDownloadService.class)));

            playlistItemSection = new PlaylistItemSection(activity, "playlist_item_section", adapter);
            playlistItemSection.setOnPlaylistClickListener(playlistName -> {
                rxBus.post(MainActivity.ACTION_OPEN_PLAYLISTS, playlistName);
                //activity.finish();
            });

            adapter.addSection(audioSection.getTag(), audioSection);
            adapter.addSection(diskAudioSection.getTag(), diskAudioSection);
            adapter.addSection(playlistItemSection.getTag(), playlistItemSection);

            recyclerView = (RecyclerView) view.findViewById(R.id.search_recycler);
            recyclerView.setLayoutManager(new LinearLayoutManager(activity));
            recyclerView.setAdapter(adapter);
            return view;
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
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
                            playlistManager.addToPlaylist(getActivity(), playlistItem.getId(), selectedAudio);
                            dialog.setDialogDismissListener(dialogInterface -> selectorDialogIsActive = false);
                        });
                    }
                }
            }

            audioSection.subscribe(playlistManager.getAllAudiosObservable(activity));
            playlistItemSection.subscribe(playlistManager.getPlaylistItemsObservable(activity));

            disposable = RestClientUtil.asObservable(rxPreferences, TokenActivity.PREF_ACCESS_TOKEN)
                    .observeOn(AndroidSchedulers.mainThread())
                    .filter(map -> map.containsKey(RestClientUtil.KEY_REST_CLIENT))
                    .map(map -> ((RestClient) map.get(RestClientUtil.KEY_REST_CLIENT)))
                    .subscribe(restClient -> diskAudioSection
                            .subscribe(playlistManager
                                    .getDiskAllTracksObservable(activity, restClient)
                                    .distinctUntilChanged()));
        }


        @Override
        public void onSaveInstanceState(Bundle outState) {
            Gson gson = new Gson();
            String json = gson.toJson(selectedAudio);
            outState.putBoolean("selector_dialog_is_active", selectorDialogIsActive);
            outState.putString("selected_audio", json);
            super.onSaveInstanceState(outState);
        }

        @Override
        public void onDestroyView() {
            audioSection.dispose();
            playlistItemSection.dispose();

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
            dialog.setPlaylistItemClickListener(playlistItem -> playlistManager.addToPlaylist(getActivity(), playlistItem.getId(), audio));
            dialog.show(getActivity().getFragmentManager(), getActivity().getResources().getString(R.string.add_to_playlist));
            selectorDialogIsActive = true;
            dialog.setDialogDismissListener(dialogInterface -> selectorDialogIsActive = false);
        }

        @Subscribe(tags = {@Tag(AudioStatus.STATUS_AUDIO_DOWNLOAD_PREPARING)})
        public void setAudioPreparingStatus(Audio taskAudio) {
            diskAudioSection.setStatus(taskAudio, AudioStatus.STATUS_AUDIO_DOWNLOAD_PREPARING);
        }

//    @Subscribe(tags = {@Tag(AudioStatus.STATUS_AUDIO_DOWNLOADING)})
//    public void setAudioDownloadingStatus(Pair<String, Pair<Long, Long>> state){
//        adapter.setStatus(state, AudioStatus.STATUS_AUDIO_DOWNLOADING);
//    }

        @Subscribe(tags = {@Tag(AudioStatus.STATUS_AUDIO_DOWNLOADED)})
        public void setAudioDownloadedStatus(Audio taskAudio) {
            diskAudioSection.setStatus(taskAudio, AudioStatus.STATUS_AUDIO_DOWNLOADED);
            String title = taskAudio.getTitle();
            Toast.makeText(activity, getResources().getString(R.string.download_completed) + ": " + title, Toast.LENGTH_SHORT).show();
        }

        @Subscribe(tags = {@Tag(AudioStatus.STATUS_ALL_AUDIOS_DOWNLOADED)})
        public void showAllAudiosDownloadedToast(Object object) {
            Toast.makeText(activity, getString(R.string.all_tracks_downloaded), Toast.LENGTH_SHORT).show();
        }

        @Subscribe(tags = {@Tag(AudioStatus.STATUS_AUDIO_DOWNLOAD_CANCELED)})
        public void setAudioCanceledStatus(Audio taskAudio) {
            diskAudioSection.setStatus(taskAudio, AudioStatus.STATUS_AUDIO_DOWNLOAD_CANCELED);
            String title = taskAudio.getTitle();
            Toast.makeText(activity, getResources().getString(R.string.download_canceled) + ": " + title, Toast.LENGTH_SHORT).show();
        }
    }
}

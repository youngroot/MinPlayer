package com.ivanroot.minplayer.fragment;

import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.hwangjr.rxbus.Bus;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.activity.MainActivity;
import com.ivanroot.minplayer.activity.TokenActivity;
import com.ivanroot.minplayer.adapter.DiskPlaylistAdapter;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.disk.constants.AudioStatus;
import com.ivanroot.minplayer.disk.RestClientUtil;
import com.ivanroot.minplayer.disk.service.AudioDownloadService;
import com.ivanroot.minplayer.disk.service.AudioTransferServiceBase;
import com.ivanroot.minplayer.playlist.Playlist;
import com.ivanroot.minplayer.playlist.PlaylistManager;
import com.ivanroot.minplayer.utils.RxNetworkChangeReceiver;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.http.UnauthorizedException;

import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;


import static com.ivanroot.minplayer.player.constants.PlayerActions.ACTION_PLAY_AUDIO;
import static com.ivanroot.minplayer.player.constants.PlayerActions.ACTION_SET_PLAYLIST;

public class DiskFragment extends NavFragmentBase {
    public static final String NAME = "DiskFragment";
    private FastScrollRecyclerView audioRecycler;
    private DiskPlaylistAdapter adapter;
    private MainActivity activity;
    private Disposable restPrefDisposable;
    private PlaylistManager playlistManager = PlaylistManager.getInstance();
    private Bus rxBus = RxBus.get();
    private TextView statusText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rxBus.register(this);
        activity = (MainActivity) getActivity();
        adapter = new DiskPlaylistAdapter(getActivity());
    }

    private void setupRestClient() {
        RxSharedPreferences rxPreferences = RxSharedPreferences.create(PreferenceManager.getDefaultSharedPreferences(activity));
        Observable<String> tokenObservable = rxPreferences.getString(TokenActivity.PREF_ACCESS_TOKEN)
                .asObservable();

        Observable<NetworkInfo> networkInfoObservable = RxNetworkChangeReceiver.create(getActivity())
                .register()
                .asObservable();

        restPrefDisposable = RestClientUtil.asObservable(tokenObservable, networkInfoObservable)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(state -> {
                    RestClient restClient = (RestClient) state.get(RestClientUtil.KEY_REST_CLIENT);
                    NetworkInfo networkInfo = (NetworkInfo) state.get(RestClientUtil.KEY_NETWORK_INFO);
                    Object error = state.get(RestClientUtil.KEY_ERROR);

                    if (!networkInfo.isConnected()) {
                        notifyNoInternetConnection();
                    } else if (error instanceof UnauthorizedException) {
                        notifyDiskAuthorizationError();
                        getToken();
                    } else {
                        statusText.setVisibility(View.INVISIBLE);
                        audioRecycler.setVisibility(View.VISIBLE);
                    }

                    if(restClient != null){
                        Observable<Playlist> playlistObservable = playlistManager.getPlaylistObservable(activity, restClient, PlaylistManager.DISK_ALL_TRACKS_PLAYLIST);
                        adapter.subscribe(playlistObservable);
                    }
                });
    }

    private void notifyDiskAuthorizationError() {
        audioRecycler.setVisibility(View.INVISIBLE);
        statusText.setVisibility(View.VISIBLE);
        statusText.setText(getResources().getString(R.string.disk_login_error));
    }

    private void notifyNoInternetConnection() {
        audioRecycler.setVisibility(View.INVISIBLE);
        statusText.setVisibility(View.VISIBLE);
        statusText.setText(getResources().getString(R.string.no_internet_connection));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.disk_fragment, container, false);
        statusText = (TextView) view.findViewById(R.id.status_text);

        setupDrawer(view);
        setupRecycler(view);
        setupRestClient();
        prepareListeners();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setupRestClient();
    }

    @Override
    public void onStop() {
        super.onStop();
        restPrefDisposable.dispose();
        adapter.dispose();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        rxBus.unregister(this);
    }

    private void getToken() {
        Intent intent = new Intent(getActivity(), TokenActivity.class);
        startActivityForResult(intent, 1);
    }


    private void setupRecycler(View view) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        audioRecycler = (FastScrollRecyclerView) view.findViewById(R.id.audio_recycler);
        audioRecycler.setHasFixedSize(true);
        audioRecycler.setLayoutManager(layoutManager);
        audioRecycler.setAdapter(adapter);
    }

    private void prepareListeners() {
        adapter.setOnMoreBtnClickListener((moreBtn, playlist, i) -> activity.startService(AudioTransferServiceBase.getIntentFromAudio(activity, playlist.getAudio(i), AudioDownloadService.class)));

        adapter.setAudioClickListener((audio, playlistName) -> {
            rxBus.post(ACTION_SET_PLAYLIST, playlistName);
            rxBus.post(ACTION_PLAY_AUDIO, audio);
        });
    }

    @Subscribe(tags = {@Tag(AudioStatus.STATUS_AUDIO_DOWNLOAD_PREPARING)})
    public void setAudioPreparingStatus(Audio taskAudio) {
        adapter.setStatus(taskAudio, AudioStatus.STATUS_AUDIO_DOWNLOAD_PREPARING);
    }

//    @Subscribe(tags = {@Tag(AudioStatus.STATUS_AUDIO_DOWNLOADING)})
//    public void setAudioDownloadingStatus(Pair<String, Pair<Long, Long>> state){
//        adapter.setStatus(state, AudioStatus.STATUS_AUDIO_DOWNLOADING);
//    }

    @Subscribe(tags = {@Tag(AudioStatus.STATUS_AUDIO_DOWNLOADED)})
    public void setAudioDownloadedStatus(Audio taskAudio) {
        adapter.setStatus(taskAudio, AudioStatus.STATUS_AUDIO_DOWNLOADED);
        String title = taskAudio.getTitle();
        Toast.makeText(activity, getResources().getString(R.string.download_completed) + ": " + title, Toast.LENGTH_SHORT).show();
    }

    @Subscribe(tags = {@Tag(AudioStatus.STATUS_ALL_AUDIOS_DOWNLOADED)})
    public void showAllAudiosDownloadedToast(Object object) {
        Toast.makeText(activity, getString(R.string.all_tracks_downloaded), Toast.LENGTH_SHORT).show();
    }

    @Subscribe(tags = {@Tag(AudioStatus.STATUS_AUDIO_DOWNLOAD_CANCELED)})
    public void setAudioCanceledStatus(Audio taskAudio) {
        adapter.setStatus(taskAudio, AudioStatus.STATUS_AUDIO_DOWNLOAD_CANCELED);
        String title = taskAudio.getTitle();
        Toast.makeText(activity, getResources().getString(R.string.download_canceled) + ": " + title, Toast.LENGTH_SHORT).show();
    }

    @Override
    public String getActionBarTitle() {
        return getResources().getString(R.string.yandex_disk);
    }
}

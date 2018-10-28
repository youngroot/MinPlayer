package com.ivanroot.minplayer.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.hwangjr.rxbus.Bus;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.activity.MainActivity;
import com.ivanroot.minplayer.activity.TokenActivity;
import com.ivanroot.minplayer.adapter.DiskPlaylistAdapter;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.disk.AudioDownloadService;
import com.ivanroot.minplayer.disk.AudioStatus;
import com.ivanroot.minplayer.disk.RestClientUtil;
import com.ivanroot.minplayer.utils.RxNetworkChangeReceiver;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.http.UnauthorizedException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;


import static com.ivanroot.minplayer.player.constants.PlayerActions.ACTION_PLAY_AUDIO;
import static com.ivanroot.minplayer.player.constants.PlayerActions.ACTION_SET_PLAYLIST;

public class DiskFragment extends NavFragmentBase {
    public static final String NAME = "DiskFragment";
    private String accessToken;
    private FastScrollRecyclerView audioRecycler;
    private DiskPlaylistAdapter adapter;
    private MainActivity activity;
    private Disposable restDisposable;
    private Bus rxBus = RxBus.get();
    private TextView statusText;


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            accessToken = data.getStringExtra("access_token");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rxBus.register(this);
        activity = (MainActivity) getActivity();
        adapter = new DiskPlaylistAdapter(getActivity());
    }

    private void setupRestClient(View view) {
        statusText = (TextView) view.findViewById(R.id.statusText);
        Observable<String> tokenObservable = RxSharedPreferences.create(PreferenceManager.getDefaultSharedPreferences(getActivity()))
                .getString(TokenActivity.PREF_ACCESS_TOKEN)
                .asObservable();

        Observable<NetworkInfo> networkInfoObservable = RxNetworkChangeReceiver.create(getActivity())
                .register()
                .asObservable();

        restDisposable = RestClientUtil.asObservable(tokenObservable, networkInfoObservable)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(state -> {
                    RestClient restClient = (RestClient)state.get(RestClientUtil.KEY_REST_CLIENT);
                    NetworkInfo networkInfo = (NetworkInfo)state.get(RestClientUtil.KEY_NETWORK_INFO);
                    Object error = state.get(RestClientUtil.KEY_ERROR);

                    if (!networkInfo.isConnected()) {
                        audioRecycler.setVisibility(View.INVISIBLE);
                        statusText.setVisibility(View.VISIBLE);
                        statusText.setText(getResources().getString(R.string.no_internet_connection));
                    } else if (error != null && error instanceof UnauthorizedException) {
                        audioRecycler.setVisibility(View.INVISIBLE);
                        statusText.setVisibility(View.VISIBLE);
                        statusText.setText(getResources().getString(R.string.disk_login_error));
                        getToken();
                    } else {
                        statusText.setVisibility(View.INVISIBLE);
                        audioRecycler.setVisibility(View.VISIBLE);
                    }

                    adapter.subscribeOnDisk(restClient);
                });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.disk_fragment_layout, container, false);
        setupDrawer(view);
        setupRecycler(view);
        setupRestClient(view);
        prepareListeners();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        rxBus.unregister(this);
        adapter.dispose();
        restDisposable.dispose();
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
        adapter.setOnMoreBtnClickListener((moreBtn, playlist, i) -> activity.startService(getIntentFromAudio(playlist.getAudio(i))));

        adapter.setAudioClickListener((audio, playlistName) -> {
            rxBus.post(ACTION_SET_PLAYLIST, playlistName);
            rxBus.post(ACTION_PLAY_AUDIO, audio);
        });
    }

    @NonNull
    private Intent getIntentFromAudio(Audio audio) {
        Intent intent = new Intent(activity, AudioDownloadService.class);
        intent.putExtra(AudioDownloadService.EXTRA_AUDIO_SIZE, audio.getSize());
        intent.putExtra(AudioDownloadService.EXTRA_AUDIO_PATH, audio.getCloudData());
        intent.putExtra(AudioDownloadService.EXTRA_MD5_HASH, audio.getMd5Hash());
        intent.putExtra(AudioDownloadService.EXTRA_AUDIO_TITLE, audio.getTitle());
        return intent;
    }

    @Subscribe(tags = {@Tag(AudioStatus.STATUS_AUDIO_PREPARING)})
    public void setAudioPreparingStatus(String md5Hash) {
        adapter.setStatus(md5Hash, AudioStatus.STATUS_AUDIO_PREPARING);
    }

    @Subscribe(tags = {@Tag(AudioStatus.STATUS_AUDIO_DOWNLOADED)})
    public void setAudioDownloadedStatus(String md5Hash) {
        adapter.setStatus(md5Hash, AudioStatus.STATUS_AUDIO_DOWNLOADED);
        Toast.makeText(activity, getResources().getString(R.string.download_complete), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected String getActionBarTitle() {
        return getResources().getString(R.string.yandex_disk);
    }
}

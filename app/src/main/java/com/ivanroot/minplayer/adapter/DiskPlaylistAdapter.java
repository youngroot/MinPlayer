package com.ivanroot.minplayer.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hwangjr.rxbus.Bus;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.adapter.viewholder.DiskAudioViewHolder;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.disk.AudioStatus;
import com.ivanroot.minplayer.playlist.Playlist;
import com.ivanroot.minplayer.playlist.PlaylistManager;
import com.ivanroot.minplayer.utils.Pair;
import com.yandex.disk.rest.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class DiskPlaylistAdapter extends BasePlaylistAdapter<Audio, DiskAudioViewHolder> {
    private Map<String, Pair<String, Pair<Long, Long>>> statuses = new HashMap<>();

    public DiskPlaylistAdapter(Activity activity) {
        super(activity);
    }

    @NonNull
    private Disposable getPlaylistDisposable(Activity activity, RestClient restClient) {
        return playlistManager.getDiskAllTracksObservable(activity, restClient)
                .map(list -> new Playlist(PlaylistManager.DISK_ALL_TRACKS_PLAYLIST).setAudioList(list))
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setPlaylist);
    }

    public void subscribeOnDisk(RestClient restClient) {
        if (playlistDisposable != null)
            playlistDisposable.dispose();
        if (restClient != null)
            playlistDisposable = getPlaylistDisposable(activity, restClient);
    }

    @Override
    public DiskAudioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.audio_item_disk, parent, false);
        return new DiskAudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DiskAudioViewHolder holder, int position) {
        Audio audio = playlist.getAudio(position);
        Pair<String, Pair<Long, Long>> status = new Pair<>(AudioStatus.STATUS_AUDIO_ONLY_ONLINE, new Pair<>(0l, 0l));

        if (statuses.get(audio.getMd5Hash()) != null)
            status = statuses.get(audio.getMd5Hash());

        holder.representItem(activity, audio, status);
        holder.itemView.setOnClickListener(v -> audioClickListener.OnAudioClick(audio, playlist.getName()));
        holder.setMoreBtnOnClickListener(v -> moreBtnListener.onMoreBtnClick(v, playlist, position));
    }

    public void setStatus(Pair<String, Pair<Long, Long>> state, String status) {
        String md5Hash = state.first;
        statuses.put(md5Hash, new Pair<>(status, state.second));
        notifyDataSetChanged();
    }
}

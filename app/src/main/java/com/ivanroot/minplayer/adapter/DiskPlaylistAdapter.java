package com.ivanroot.minplayer.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.adapter.viewholder.DiskAudioViewHolder;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.disk.constants.AudioStatus;
import com.ivanroot.minplayer.playlist.Playlist;
import com.ivanroot.minplayer.playlist.PlaylistManager;
import com.yandex.disk.rest.RestClient;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class DiskPlaylistAdapter extends BasePlaylistAdapter<Audio, DiskAudioViewHolder> {
    private Map<String, String> statuses = new HashMap<>();

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
        String status = AudioStatus.STATUS_AUDIO_ONLY_ONLINE;

        if (statuses.get(audio.getMd5Hash()) != null)
            status = statuses.get(audio.getMd5Hash());

        holder.representItem(activity, audio, status);
        holder.itemView.setOnClickListener(v -> audioClickListener.onAudioClick(audio, playlist.getName()));
        holder.setMoreBtnOnClickListener(v -> moreBtnListener.onMoreBtnClick(v, playlist, position));
    }

    public void setStatus(Audio taskAudio, String status) {
        String md5Hash = taskAudio.getMd5Hash();
        statuses.put(md5Hash, status);
        notifyDataSetChanged();
    }
}

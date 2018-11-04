package com.ivanroot.minplayer.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.adapter.viewholder.AudioViewHolder;
import com.ivanroot.minplayer.audio.Audio;

import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by Ivan Root on 16.12.2017.
 */

public class PlaylistAdapter extends BasePlaylistAdapter<Audio, AudioViewHolder> {

    private Activity activity;

    public PlaylistAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public AudioViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.audio_item, parent, false);
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AudioViewHolder audioViewHolder, int i) {
        audioViewHolder.representItem(activity, playlist.getAudio(i));
        audioViewHolder.itemView
                .setOnClickListener(v -> audioClickListener.onAudioClick(playlist.getAudio(i), playlist.getName()));
        audioViewHolder
                .setMoreBtnOnClickListener(v -> moreBtnListener.onMoreBtnClick(v, playlist, i));
    }

}

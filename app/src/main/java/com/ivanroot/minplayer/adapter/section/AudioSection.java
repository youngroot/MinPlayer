package com.ivanroot.minplayer.adapter.section;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.adapter.listeners.OnAudioClickListener;
import com.ivanroot.minplayer.adapter.listeners.OnMoreBtnClickListener;
import com.ivanroot.minplayer.adapter.viewholder.AudioViewHolder;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.playlist.Playlist;
import com.ivanroot.minplayer.playlist.PlaylistManager;

import java.util.ArrayList;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

public class AudioSection extends StatelessSection {
    private Context context;
    private OnAudioClickListener onAudioClickListener;
    private OnMoreBtnClickListener onMoreBtnClickListener;
    private List<Audio> audioList = new ArrayList<>();

    public AudioSection(Context context) {
        super(SectionParameters.builder()
                .itemResourceId(R.layout.audio_item)
                .headerResourceId(R.layout.audio_item_section_header)
                .build());
        this.context = context;
    }

    public void setAudioList(@NonNull List<Audio> audioList){
        this.audioList = audioList;
    }

    @Override
    public int getContentItemsTotal() {
        return audioList.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        AudioViewHolder audioViewHolder = (AudioViewHolder)holder;
        audioViewHolder.itemView.setOnClickListener(v -> onAudioClickListener.onAudioClick(audioList.get(position),
                PlaylistManager.ALL_TRACKS_PLAYLIST));
        audioViewHolder.setMoreBtnOnClickListener(v -> {
            Playlist playlist = new Playlist();
            playlist.setAudioList(audioList);
            onMoreBtnClickListener.onMoreBtnClick(v, playlist, position);
        });
        audioViewHolder.representItem(context, audioList.get(position));
    }

    public void setOnAudioClickListener(OnAudioClickListener onAudioClickListener) {
        this.onAudioClickListener = onAudioClickListener;
    }

    public void setOnMoreBtnClickListener(OnMoreBtnClickListener onMoreBtnClickListener) {
        this.onMoreBtnClickListener = onMoreBtnClickListener;
    }
}

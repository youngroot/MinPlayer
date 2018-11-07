package com.ivanroot.minplayer.adapter.section;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.adapter.listeners.OnAudioClickListener;
import com.ivanroot.minplayer.adapter.listeners.OnAudioMoreBtnClickListener;
import com.ivanroot.minplayer.adapter.viewholder.AudioViewHolder;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.playlist.Playlist;
import com.ivanroot.minplayer.playlist.PlaylistManager;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;


public class AudioSection extends BaseItemSection<Audio> {
    protected OnAudioClickListener onAudioClickListener;
    protected OnAudioMoreBtnClickListener onAudioMoreBtnClickListener;

    public AudioSection(Context context, String tag, SectionedRecyclerViewAdapter adapter) {
        super(context, SectionParameters.builder()
                .itemResourceId(R.layout.audio_item_section)
                .headerResourceId(R.layout.audio_item_section_header)
                .build(), tag, adapter);
    }

    public AudioSection(Context context, SectionParameters build, String tag, SectionedRecyclerViewAdapter adapter) {
        super(context, build, tag, adapter);
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        AudioViewHolder audioViewHolder = (AudioViewHolder)holder;
        audioViewHolder.itemView.setOnClickListener(v -> onAudioClickListener.onAudioClick(filteredData.get(position),
                PlaylistManager.ALL_TRACKS_PLAYLIST));
        audioViewHolder.setMoreBtnOnClickListener(v -> {
            Playlist playlist = new Playlist();
            playlist.setAudioList(filteredData);
            onAudioMoreBtnClickListener.onMoreBtnClick(v, playlist, position);
        });
        audioViewHolder.representItem(context, filteredData.get(position));
    }

    public void setOnAudioClickListener(OnAudioClickListener onAudioClickListener) {
        this.onAudioClickListener = onAudioClickListener;
    }

    @Override
    public boolean isItemMatchingQuery(Audio audio, String query, int position) {
        query = query.toLowerCase();
        return audio.getTitle() != null && audio.getTitle().toLowerCase().startsWith(query)||
                audio.getAlbum() != null && audio.getAlbum().toLowerCase().startsWith(query)||
                audio.getArtist() != null && audio.getArtist().toLowerCase().startsWith(query);

    }

    public void setOnAudioMoreBtnClickListener(OnAudioMoreBtnClickListener onAudioMoreBtnClickListener) {
        this.onAudioMoreBtnClickListener = onAudioMoreBtnClickListener;
    }

}

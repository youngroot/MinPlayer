package com.ivanroot.minplayer.adapter.section;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.adapter.viewholder.AudioViewHolder;
import com.ivanroot.minplayer.adapter.viewholder.DiskAudioViewHolder;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.disk.constants.AudioStatus;
import com.ivanroot.minplayer.playlist.Playlist;
import com.ivanroot.minplayer.playlist.PlaylistManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class DiskAudioSection extends AudioSection {
    private Map<Audio, String> statuses = new HashMap<>();

    public DiskAudioSection(Context context,
                            String tag, SectionedRecyclerViewAdapter adapter) {
        super(context, SectionParameters.builder()
                .itemResourceId(R.layout.audio_item_disk_section)
                .headerResourceId(R.layout.audio_item_disk_section_header)
                .build(), tag, adapter);
    }

    public DiskAudioSection(Context context, SectionParameters build, String tag, SectionedRecyclerViewAdapter adapter) {
        super(context, build, tag, adapter);
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new DiskAudioViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        Audio audio = filteredData.get(position);
        String status = AudioStatus.STATUS_AUDIO_ONLY_ONLINE;

        if (statuses.get(audio) != null)
            status = statuses.get(audio);

        DiskAudioViewHolder diskAudioViewHolder = (DiskAudioViewHolder)holder;
        diskAudioViewHolder.itemView.setOnClickListener(v -> onAudioClickListener.onAudioClick(audio,
                PlaylistManager.DISK_ALL_TRACKS_PLAYLIST));

        diskAudioViewHolder.setMoreBtnOnClickListener(v -> {
            Playlist playlist = new Playlist();
            playlist.setAudioList(filteredData);
            onAudioMoreBtnClickListener.onMoreBtnClick(v, playlist, position);
        });

        diskAudioViewHolder.representItem(context, audio, status);
    }

    public void setStatus(Audio taskAudio, String status) {
        statuses.put(taskAudio, status);
    }

    @Override
    public void onItemRemoved(int position, Audio audio) {
        super.onItemRemoved(position, audio);
        statuses.remove(audio);
    }

}

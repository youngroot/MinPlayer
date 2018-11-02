package com.ivanroot.minplayer.adapter.section;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.adapter.listeners.OnPlaylistClickListener;
import com.ivanroot.minplayer.adapter.listeners.OnPlaylistItemMoreBtnClickListener;
import com.ivanroot.minplayer.adapter.viewholder.PlaylistViewHolder;
import com.ivanroot.minplayer.playlist.PlaylistItem;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class PlaylistItemSection extends BaseItemSection<PlaylistItem> {
    private OnPlaylistClickListener onPlaylistClickListener;
    private OnPlaylistItemMoreBtnClickListener onPlaylistItemMoreBtnClickListener;

    public PlaylistItemSection(Context context, String tag, SectionedRecyclerViewAdapter adapter) {
        super(context, SectionParameters.builder()
                        .itemResourceId(R.layout.playlist_item_section)
                        .headerResourceId(R.layout.playlist_item_section_header)
                        .build(), tag, adapter);
    }


    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        PlaylistViewHolder playlistViewHolder = (PlaylistViewHolder) holder;
        playlistViewHolder.itemView.setOnClickListener(v -> onPlaylistClickListener.onPlaylistClick(filteredData.get(position).getName()));
        playlistViewHolder.setMoreBtnOnClickListener(v -> onPlaylistItemMoreBtnClickListener.onMoreBtnClick(v, filteredData.get(position)));
        playlistViewHolder.representItem(context, filteredData.get(position));
    }

    @Override
    public boolean isItemMatchingQuery(PlaylistItem item, String query, int position) {
        query = query.toLowerCase();
        return item.getName() != null && item.getName().toLowerCase().startsWith(query);
    }

    @Override
    public boolean isLowerThan(PlaylistItem item1, PlaylistItem item2) {
        int comp = item1.getName().compareTo(item2.getName());
        return comp < 0;
    }

    public void setOnPlaylistItemMoreBtnClickListener(OnPlaylistItemMoreBtnClickListener onPlaylistItemMoreBtnClickListener) {
        this.onPlaylistItemMoreBtnClickListener = onPlaylistItemMoreBtnClickListener;
    }

    public void setOnPlaylistClickListener(OnPlaylistClickListener onPlaylistClickListener) {
        this.onPlaylistClickListener = onPlaylistClickListener;
    }


}

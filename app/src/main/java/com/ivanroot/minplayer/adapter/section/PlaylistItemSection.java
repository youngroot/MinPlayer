package com.ivanroot.minplayer.adapter.section;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.adapter.viewholder.PlaylistViewHolder;
import com.ivanroot.minplayer.playlist.PlaylistItem;

import java.util.ArrayList;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

public class PlaylistItemSection extends StatelessSection {
    private Context context;
    private List<PlaylistItem> playlistItems = new ArrayList<>();

    public PlaylistItemSection(Context context) {
        super(SectionParameters.builder()
                .itemResourceId(R.layout.playlist_item)
                .headerResourceId(R.layout.playlist_item_section_header)
                .build());
        this.context = context;
    }

    public void setPlaylistItems(List<PlaylistItem> playlistItems){
        this.playlistItems = playlistItems;
    }

    @Override
    public int getContentItemsTotal() {
        return playlistItems.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        PlaylistViewHolder playlistViewHolder = (PlaylistViewHolder)holder;
        playlistViewHolder.representItem(context, playlistItems.get(position));
    }
}

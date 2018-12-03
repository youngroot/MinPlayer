package com.ivanroot.minplayer.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.adapter.listeners.OnPlaylistClickListener;
import com.ivanroot.minplayer.adapter.listeners.OnPlaylistItemMoreBtnClickListener;
import com.ivanroot.minplayer.adapter.viewholder.PlaylistViewHolder;
import com.ivanroot.minplayer.playlist.PlaylistItem;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;


/**
 * Created by Ivan Root on 12.02.2018.
 */

public class PlaylistSelectorAdapter extends RecyclerView.Adapter<PlaylistViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter, com.ivanroot.minplayer.adapter.Disposable,
        ItemRemoveInsertListObservableTransformer.OnItemRemovedListener<PlaylistItem>,
        ItemRemoveInsertListObservableTransformer.OnItemInsertedListener<PlaylistItem> {

    private List<PlaylistItem> playlistItems = new ArrayList<>();
    private ItemRemoveInsertListObservableTransformer<PlaylistItem> transformer = new ItemRemoveInsertListObservableTransformer<>(playlistItems);
    private Disposable disposable;
    private Context context;
    private OnPlaylistClickListener playlistClickListener;
    private OnPlaylistItemMoreBtnClickListener moreBtnClickListener;

    public PlaylistSelectorAdapter(Context context) {
        this.context = context;
        transformer.setOnItemRemovedListener(this);
        transformer.setOnItemInsertedListener(this);
    }

    public void setOnPlaylistClickListener(OnPlaylistClickListener playlistClickListener) {
        this.playlistClickListener = playlistClickListener;
    }

    public void setOnMoreBtnClickListener(OnPlaylistItemMoreBtnClickListener moreBtnClickListener) {
        this.moreBtnClickListener = moreBtnClickListener;
    }

    public void subscribe(Observable<List<PlaylistItem>> playlistItemObservable) {
        dispose();
        disposable = playlistItemObservable
                .observeOn(AndroidSchedulers.mainThread())
                .compose(transformer)
                .subscribe();
    }

    @Override
    public PlaylistViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.playlist_item_card, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlaylistViewHolder playlistViewHolder, int i) {
        PlaylistItem playlistItem = playlistItems.get(i);
        playlistViewHolder.representItem(context, playlistItem);
        playlistViewHolder.itemView.setOnClickListener(v -> playlistClickListener.onPlaylistClick(playlistItem.getName()));
        playlistViewHolder.setMoreBtnOnClickListener(v -> {
            if (moreBtnClickListener != null) {
                moreBtnClickListener.onMoreBtnClick(v, playlistItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        try {
            return playlistItems.size();
        } catch (NullPointerException ex) {
            return 0;
        }

    }

    @NonNull
    @Override
    public String getSectionName(int i) {
        return playlistItems
                .get(i)
                .getName()
                .substring(0, 1);
    }

    @Override
    public void dispose() {
        if (disposable != null)
            disposable.dispose();
    }

    @Override
    public void onItemRemoved(int position, PlaylistItem item) {
        try {
            notifyItemRemoved(position);
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
            Log.e(toString(), ex.toString());
        }
    }

    @Override
    public void onItemInserted(int position, PlaylistItem item) {
        try {
            notifyItemInserted(position);
        }catch (IllegalStateException ex){
            ex.printStackTrace();
            Log.e(toString(), ex.toString());
        }
    }
}

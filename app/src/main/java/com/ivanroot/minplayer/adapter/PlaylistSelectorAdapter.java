package com.ivanroot.minplayer.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.adapter.viewholder.PlaylistViewHolder;
import com.ivanroot.minplayer.playlist.OnPlaylistClickListener;
import com.ivanroot.minplayer.playlist.PlaylistItem;
import com.ivanroot.minplayer.playlist.PlaylistManager;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;


/**
 * Created by Ivan Root on 12.02.2018.
 */

public class PlaylistSelectorAdapter extends RecyclerView.Adapter<PlaylistViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {

    private List<PlaylistItem> playlistItems;
    private PlaylistManager playlistManager;
    private Disposable disposable;
    private OnPlaylistClickListener playlistClickListener;
    private Context context;

    public PlaylistSelectorAdapter(Context context) {
        this.context = context;
        playlistManager = PlaylistManager.getInstance();
        disposable = playlistManager.getPlaylistItemsObservable(context)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setPlaylistItems);




    }

    public void setOnPlaylistClickListener(OnPlaylistClickListener playlistClickListener) {
        this.playlistClickListener = playlistClickListener;
    }


    private void setPlaylistItems(List<PlaylistItem> playlistItems) {
        Log.i(this.toString(), "new List!");
        this.playlistItems = playlistItems;
        notifyDataSetChanged();
    }

    private void showDeletionDialog(String playlistName) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setMessage(R.string.remove_playlist_question)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> playlistManager.removePlaylist(context, playlistName))
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());
        builder.show();

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
        playlistViewHolder.representPlaylistItem(playlistItem);
        playlistViewHolder.itemView.setOnClickListener(v -> playlistClickListener.onPlaylistClick(playlistItem.getName()));
        playlistViewHolder.setMoreBtnOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.inflate(R.menu.playlist_more_menu);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.remove_playlist:
                        showDeletionDialog(playlistItem.getName());
                        return true;
                    default:
                        return false;
                }
            });
            popupMenu.show();
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

    public void dispose() {
        if (disposable != null)
            disposable.dispose();
    }
}

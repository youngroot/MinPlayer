package com.ivanroot.minplayer.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.adapter.viewholder.AudioViewHolder;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.playlist.Playlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Ivan Root on 16.12.2017.
 */

public class PlaylistAdapter extends BasePlaylistAdapter<Audio, AudioViewHolder> implements ItemTouchHelperAdapter {

    private Activity activity;
    private boolean playlistModifyModeEnabled = false;
    private List<Audio> originalAudioList = new ArrayList<>();
    private ItemTouchHelper itemTouchHelper;
    private OnModifiedPlaylistSaveListener onModifiedPlaylistSaveListener;

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

        if (playlistModifyModeEnabled) {
            if (itemTouchHelper != null)
                audioViewHolder.setMoreBtnOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN)
                            itemTouchHelper.startDrag(audioViewHolder);
                        return false;
                    }
                });

            audioViewHolder.itemView.setOnClickListener(null);
            audioViewHolder.setMoreBtnOnClickListener(null);
            audioViewHolder.setMoreBtnIconResource(R.drawable.ic_drag);
        } else {
            audioViewHolder.setMoreBtnOnTouchListener(null);
            audioViewHolder.itemView.setOnClickListener(v -> audioClickListener.onAudioClick(playlist.getAudio(i), playlist.getName()));
            audioViewHolder.setMoreBtnOnClickListener(v -> moreBtnListener.onMoreBtnClick(v, playlist, i));
            audioViewHolder.setMoreBtnIconResource(R.drawable.ic_more_vert);
        }
    }

    public void setPlaylistModifyModeEnabled(boolean playlistModifyModeEnabled) {
        Log.i(toString(), "setPlaylistModifyModeEnabled " + playlistModifyModeEnabled);
        this.playlistModifyModeEnabled = playlistModifyModeEnabled;

        if (playlistModifyModeEnabled) {
            originalAudioList.addAll(playlist.getAudioList());
            transformer.setPaused(true);
        } else {
            if(!originalAudioList.isEmpty()){
               playlist.getAudioList().clear();
               playlist.getAudioList().addAll(originalAudioList);
               originalAudioList.clear();
            }

            transformer.setPaused(false);
        }

        notifyDataSetChanged();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        List<Audio> audioList = playlist.getAudioList();

        if (fromPosition < toPosition)
            for (int i = fromPosition; i < toPosition; i++)
                Collections.swap(audioList, i, i + 1);

        else
            for (int i = fromPosition; i > toPosition; i--)
                Collections.swap(audioList, i, i - 1);

        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        playlist.deleteAudio(position);
        notifyItemRemoved(position);
    }

    public void setItemTouchHelper(ItemTouchHelper itemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper;
    }

    public void saveModifiedPlaylist() {
        Log.i(toString(), "saveModifiedPlaylist");
        if (playlistModifyModeEnabled) {
            originalAudioList.clear();

            if (onModifiedPlaylistSaveListener != null)
                onModifiedPlaylistSaveListener.onPlaylistSave(playlist);
        }
    }

    public void setOnModifiedPlaylistSaveListener(@NonNull OnModifiedPlaylistSaveListener onModifiedPlaylistSaveListener) {
        this.onModifiedPlaylistSaveListener = onModifiedPlaylistSaveListener;
    }

    public interface OnModifiedPlaylistSaveListener {
        void onPlaylistSave(Playlist modifiedPlaylist);
    }
}

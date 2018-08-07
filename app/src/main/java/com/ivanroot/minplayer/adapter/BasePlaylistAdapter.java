package com.ivanroot.minplayer.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.ivanroot.minplayer.adapter.viewholder.BaseItemViewHolder;
import com.ivanroot.minplayer.audio.OnAudioClickListener;
import com.ivanroot.minplayer.playlist.Playlist;
import com.ivanroot.minplayer.playlist.PlaylistManager;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import io.reactivex.disposables.Disposable;

public abstract class BasePlaylistAdapter<T, VH extends BaseItemViewHolder<T>> extends RecyclerView.Adapter<VH>
        implements FastScrollRecyclerView.SectionedAdapter{

    protected Activity activity;
    protected Playlist playlist;
    protected PlaylistManager playlistManager = PlaylistManager.getInstance();
    protected Disposable playlistDisposable;
    protected OnAudioClickListener audioClickListener;
    protected PlaylistAdapter.OnNewPlaylistUpdateListener playlistListener;
    protected PlaylistAdapter.OnMoreBtnClickListener moreBtnListener;

    public BasePlaylistAdapter(Activity activity) {
        this.activity = activity;
    }

    public void setAudioClickListener(OnAudioClickListener audioClickListener){
        this.audioClickListener = audioClickListener;
    }

    public void setNewPlaylistUpdateListener(OnNewPlaylistUpdateListener playlistListener){
        this.playlistListener = playlistListener;
    }

    public void setOnMoreBtnClickListener(OnMoreBtnClickListener moreBtnListener){
        this.moreBtnListener = moreBtnListener;
    }

    protected void setPlaylist(Playlist playlist){
        this.playlist = playlist;
        if(playlistListener != null) playlistListener.onNewPlaylist(playlist);
        notifyDataSetChanged();
    }

    public Playlist getPlaylist(){
        return this.playlist;
    }

    @Override
    public abstract VH onCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public abstract void onBindViewHolder(VH holder, int position);

    @Override
    public int getItemCount() {
        if(playlist != null)
            return playlist.size();
        else return 0;
    }

    public void dispose(){
        if(playlistDisposable != null)
            playlistDisposable.dispose();
    }

    @NonNull
    @Override
    public String getSectionName(int i) {
        return playlist.getAudio(i).getTitle().substring(0,1);
    }

    public interface OnMoreBtnClickListener{
        void onMoreBtnClick(View view, Playlist playlist, int i);
    }

    public interface OnNewPlaylistUpdateListener {
        void onNewPlaylist(Playlist playlist);
    }
}
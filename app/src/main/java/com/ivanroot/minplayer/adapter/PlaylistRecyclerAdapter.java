package com.ivanroot.minplayer.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.adapter.viewholder.AudioViewHolder;
import com.ivanroot.minplayer.audio.OnAudioClickListener;
import com.ivanroot.minplayer.playlist.Playlist;
import com.ivanroot.minplayer.playlist.PlaylistManager;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Created by Ivan Root on 16.12.2017.
 */

public class PlaylistRecyclerAdapter extends RecyclerView.Adapter<AudioViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {

    private Activity activity;
    private Playlist playlist;
    private PlaylistManager playlistManager = PlaylistManager.getInstance();
    private Disposable disposable;
    private OnAudioClickListener audioClickListener;
    private OnNewPlaylistUpdateListener playlistListener;
    private OnMoreBtnClickListener moreBtnListener;

    public PlaylistRecyclerAdapter(Activity activity, String playlistName){

        this.activity = activity;
        disposable = playlistManager.getPlaylistObservable(this.activity,playlistName)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setPlaylist);

    }

    public void setAudioClickListener(OnAudioClickListener audioClickListener){
        this.audioClickListener = audioClickListener;
    }

    public void setNewPlaylistUpdateListener(OnNewPlaylistUpdateListener playlistListener){
        this.playlistListener = playlistListener;
    }

    public void setMoreBtnListener(OnMoreBtnClickListener moreBtnListener){
        this.moreBtnListener = moreBtnListener;
    }

    private void setPlaylist(Playlist playlist){
        this.playlist = playlist;
        playlistListener.onNewPlaylist(playlist);
        notifyDataSetChanged();
    }

    public Playlist getPlaylist(){
        return this.playlist;
    }

    @Override
    public AudioViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.audio_item,parent,false);
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AudioViewHolder audioViewHolder, int i) {
        audioViewHolder.representItem(activity, playlist.getAudio(i));
        audioViewHolder.itemView
                .setOnClickListener(v -> audioClickListener.OnAudioClick(playlist.getAudio(i),playlist.getName()));
        audioViewHolder
                .setMoreBtnOnClickListener(v -> moreBtnListener.onMoreBtnClick(v,playlist,i));
    }

    @Override
    public int getItemCount() {
        if(playlist != null)
            return playlist.size();
        else return 0;
    }

    public void dispose(){
        if(disposable != null)
            disposable.dispose();
    }


    @NonNull
    @Override
    public String getSectionName(int i) {
        return playlist.getAudio(i).getTitle().substring(0,1);
    }

    public interface OnNewPlaylistUpdateListener {
        void onNewPlaylist(Playlist playlist);
    }

    public interface OnMoreBtnClickListener{
        void onMoreBtnClick(View view, Playlist playlist, int i);
    }

}

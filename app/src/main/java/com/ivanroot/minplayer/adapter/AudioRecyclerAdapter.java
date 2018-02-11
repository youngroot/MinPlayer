package com.ivanroot.minplayer.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.audio.OnAudioClickListener;
import com.ivanroot.minplayer.playlist.Playlist;
import com.ivanroot.minplayer.playlist.PlaylistManager;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Created by Ivan Root on 16.12.2017.
 */

public class AudioRecyclerAdapter extends RecyclerView.Adapter<AudioViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {

    private Context context;
    private Playlist playlist;
    private PlaylistManager playlistManager = PlaylistManager.getInstance();
    private Disposable disposable;
    private OnAudioClickListener onAudioClickListener;

    public AudioRecyclerAdapter(Context context, String playlistName){

        this.context = context;
        disposable = playlistManager.getPlaylistSubject(context)
                .flatMap(Observable::fromIterable)
                .filter(tempPlaylist -> tempPlaylist.getName().equals(playlistName))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setPlaylist);

    }

    public void setOnAudioClickListener(OnAudioClickListener onAudioClickListener){
        this.onAudioClickListener = onAudioClickListener;
    }

    private void setPlaylist(Playlist playlist){
        this.playlist = playlist;
        notifyDataSetChanged();
    }

    public Playlist getPlaylist(){
        return this.playlist;
    }

    @Override
    public AudioViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.audio_item,parent,false);
        AudioViewHolder audioViewHolder = new AudioViewHolder(view);
        audioViewHolder.representAudioItem(playlist.getAudio(i));
        return audioViewHolder;
    }

    @Override
    public void onBindViewHolder(AudioViewHolder audioViewHolder, int i) {
        audioViewHolder.representAudioItem(playlist.getAudio(i));
        audioViewHolder.itemView
                .setOnClickListener(v -> onAudioClickListener.OnAudioClick(playlist.getAudio(i),playlist.getName()));
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
}

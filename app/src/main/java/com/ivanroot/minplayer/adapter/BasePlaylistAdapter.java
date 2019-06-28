package com.ivanroot.minplayer.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.ivanroot.minplayer.adapter.listeners.OnAudioClickListener;
import com.ivanroot.minplayer.adapter.listeners.OnAudioMoreBtnClickListener;
import com.ivanroot.minplayer.adapter.viewholder.BaseItemViewHolder;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.playlist.Playlist;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public abstract class BasePlaylistAdapter<T, VH extends BaseItemViewHolder<T>> extends RecyclerView.Adapter<VH>
        implements FastScrollRecyclerView.SectionedAdapter,
        com.ivanroot.minplayer.adapter.Disposable,
        ItemRemoveInsertListObservableTransformer.OnItemRemovedListener<Audio>,
        ItemRemoveInsertListObservableTransformer.OnItemInsertedListener<Audio> {

    protected Playlist playlist = new Playlist();
    protected Disposable playlistDisposable;
    protected OnAudioClickListener audioClickListener;
    protected PlaylistAdapter.OnNewPlaylistUpdateListener playlistListener;
    protected OnAudioMoreBtnClickListener moreBtnListener;
    protected boolean playlistOrderChanged = false;
    protected ItemRemoveInsertListObservableTransformer<Audio> transformer;

    public void setAudioClickListener(OnAudioClickListener audioClickListener){
        this.audioClickListener = audioClickListener;
    }

    public void setNewPlaylistUpdateListener(OnNewPlaylistUpdateListener playlistListener){
        this.playlistListener = playlistListener;
    }

    public void setOnMoreBtnClickListener(OnAudioMoreBtnClickListener moreBtnListener){
        this.moreBtnListener = moreBtnListener;
    }

    public void setPlaylist(Playlist playlist){
        this.playlist = playlist;
        transformer.setCurrentList(playlist.getAudioList());
        if(playlistListener != null)
            playlistListener.onNewPlaylist(playlist);
        playlistOrderChanged = false;
        notifyDataSetChanged();
    }

    public Playlist getPlaylist(){
        return playlist;
    }

    public BasePlaylistAdapter() {
        super();
        transformer = new ItemRemoveInsertListObservableTransformer<>(playlist.getAudioList());
        transformer.setOnItemRemovedListener(this);
        transformer.setOnItemInsertedListener(this);
    }

    @Override
    public abstract VH onCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public abstract void onBindViewHolder(VH holder, int position);

    @Override
    public int getItemCount() {
       return playlist.size();
    }

    public void subscribe(@NonNull Observable<Playlist> playlistObservable){
        dispose();
        playlistDisposable = playlistObservable
                .observeOn(AndroidSchedulers.mainThread())
                .distinctUntilChanged()
                .doOnNext(playlist -> {
                    this.playlist.setId(playlist.getId());
                    this.playlist.setName(playlist.getName());
                    this.playlist.setImagePath(playlist.getImagePath());
                })
                .doOnNext(playlist -> {
                    if(playlistListener != null)
                        playlistListener.onNewPlaylist(playlist);
                })
                .doOnNext(playlist ->{
                    if(playlistOrderChanged){
                        setPlaylist(playlist);
                    }
                })
                .filter(playlist -> !playlistOrderChanged)
                .map(Playlist::getAudioList)
                .compose(transformer)
                .subscribe();
    }

    @Override
    public void dispose(){
        if(playlistDisposable != null)
            playlistDisposable.dispose();
    }

    @NonNull
    @Override
    public String getSectionName(int i) {
        return playlist.getAudio(i).getTitle().substring(0,1);
    }

    public interface OnNewPlaylistUpdateListener {
        void onNewPlaylist(Playlist playlist);
    }

    @Override
    public void onItemRemoved(int position, Audio audio) {
        notifyItemRemoved(position);
    }

    @Override
    public void onItemInserted(int position, Audio audio) {
        notifyItemInserted(position);
    }

    public void notifyPlaylistOrderChanged(){
        playlistOrderChanged = true;
    }
}

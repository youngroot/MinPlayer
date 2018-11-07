package com.ivanroot.minplayer.playlist;

import android.support.annotation.NonNull;

import com.ivanroot.minplayer.audio.Audio;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;

public class InsertRemoveAudioPlaylistObservableModifier implements ObservableTransformer<Playlist, Playlist> {

    private Playlist currentPlaylist;
    private OnAudioRemoveListener onAudioRemoveListener;
    private OnAudioInsertListener onAudioInsertListener;

    public InsertRemoveAudioPlaylistObservableModifier(@NonNull Playlist currentPlaylist) {
        this.currentPlaylist = currentPlaylist;
    }

    public void setCurrentPlaylist(@NonNull Playlist currentPlaylist){
        this.currentPlaylist = currentPlaylist;
    }

    public void setOnAudioRemoveListener(OnAudioRemoveListener onAudioRemoveListener) {
        this.onAudioRemoveListener = onAudioRemoveListener;
    }

    public void setOnAudioInsertListener(OnAudioInsertListener onAudioInsertListener) {
        this.onAudioInsertListener = onAudioInsertListener;
    }

    @Override
    public ObservableSource<Playlist> apply(Observable<Playlist> upstream) {
        return upstream.doOnNext(updatedPlaylist -> {
            Set<Audio> updatedAudioSet = new HashSet<>(updatedPlaylist.getAudioList());
            List<Audio> audioList = currentPlaylist.getAudioList();

            int delta = 0;

            for (int i = 0; i < audioList.size(); i++) {
                if (!updatedAudioSet.contains(audioList.get(i))) {
                    currentPlaylist.deleteAudio(i);
                    if (onAudioRemoveListener != null)
                        onAudioRemoveListener.onAudioRemove(i, audioList.get(i));
                    i -= ++delta;
                }
            }

            for(int i = 0; i < updatedPlaylist.size(); i++){
                if(i < currentPlaylist.size()){
                    if(!Objects.equals(currentPlaylist.getAudio(i), updatedPlaylist.getAudio(i))){
                        currentPlaylist.addAudio(i, updatedPlaylist.getAudio(i));
                        if(onAudioInsertListener != null)
                            onAudioInsertListener.onAudioInsert(i, updatedPlaylist.getAudio(i));
                    }

                } else {
                    currentPlaylist.addAudio(updatedPlaylist.getAudio(i));
                    if(onAudioInsertListener != null)
                        onAudioInsertListener.onAudioInsert(currentPlaylist.size() - 1, updatedPlaylist.getAudio(i));
                }
            }
        });
    }

    public interface OnAudioRemoveListener {
        void onAudioRemove(int position, Audio audio);
    }

    public interface OnAudioInsertListener {
        void onAudioInsert(int position, Audio audio);
    }
}

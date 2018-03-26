package com.ivanroot.minplayer.storio;

import android.provider.MediaStore;
import android.support.annotation.NonNull;

import com.ivanroot.minplayer.playlist.Playlist;
import com.pushtorefresh.storio3.contentresolver.operations.delete.DefaultDeleteResolver;
import com.pushtorefresh.storio3.contentresolver.queries.DeleteQuery;

/**
 * Created by ivanroot on 3/23/18.
 */

public class PlaylistDeleteResolver extends DefaultDeleteResolver<Playlist> {
    @NonNull
    @Override
    protected DeleteQuery mapToDeleteQuery(@NonNull Playlist playlist) {
        return DeleteQuery.builder()
                .uri(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI)
                .where(MediaStore.Audio.Playlists._ID + " = ?")
                .whereArgs(playlist.getId())
                .build();
    }
}

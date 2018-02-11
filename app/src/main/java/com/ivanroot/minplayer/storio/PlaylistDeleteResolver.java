package com.ivanroot.minplayer.storio;

import android.support.annotation.NonNull;


import com.ivanroot.minplayer.playlist.Playlist;
import com.pushtorefresh.storio.sqlite.operations.delete.DefaultDeleteResolver;
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery;

/**
 * Created by Ivan Root on 28.08.2017.
 */

public class PlaylistDeleteResolver extends DefaultDeleteResolver<Playlist> {
    @NonNull
    @Override
    protected DeleteQuery mapToDeleteQuery(@NonNull Playlist playlist) {

        return DeleteQuery.builder()
                .table(PlaylistTable.TABLE_PLAYLISTS)
                .where(PlaylistTable.ROW_PLAYLIST_NAME + " = ?")
                .whereArgs(playlist.getName())
                .build();

    }
}

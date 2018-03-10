package com.ivanroot.minplayer.storio;

import android.support.annotation.NonNull;

import com.ivanroot.minplayer.playlist.Playlist;
import com.ivanroot.minplayer.utils.Utils;
import com.pushtorefresh.storio3.sqlite.operations.delete.DefaultDeleteResolver;
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery;

/**
 * Created by Ivan Root on 28.08.2017.
 */

public class PlaylistDeleteResolver extends DefaultDeleteResolver<Playlist> {
    @NonNull
    @Override
    protected DeleteQuery mapToDeleteQuery(@NonNull Playlist playlist) {
        Utils.deleteFile(playlist.getImagePath());
        return DeleteQuery.builder()
                .table(PlaylistTable.TABLE)
                .where(PlaylistTable.Playlist.NAME + " = ?")
                .whereArgs(playlist.getName())
                .build();

    }
}

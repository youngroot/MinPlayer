package com.ivanroot.minplayer.storio.sqlite.playlist;

import android.support.annotation.NonNull;

import com.ivanroot.minplayer.playlist.Playlist;
import com.pushtorefresh.storio3.sqlite.operations.delete.DefaultDeleteResolver;
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery;

public class PlaylistDeleteResolver extends DefaultDeleteResolver<Playlist> {
    @NonNull
    @Override
    protected DeleteQuery mapToDeleteQuery(@NonNull Playlist playlist) {
        return DeleteQuery.builder()
                .table(PlaylistTable.TABLE_NAME)
                .where(PlaylistTable.Columns.PLAYLIST_ID + " = ?")
                .whereArgs(playlist.getId())
                .build();
    }
}

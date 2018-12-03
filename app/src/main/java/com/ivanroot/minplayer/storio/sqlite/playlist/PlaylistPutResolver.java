package com.ivanroot.minplayer.storio.sqlite.playlist;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.ivanroot.minplayer.playlist.Playlist;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.operations.put.DefaultPutResolver;
import com.pushtorefresh.storio3.sqlite.operations.put.PutResolver;
import com.pushtorefresh.storio3.sqlite.operations.put.PutResult;
import com.pushtorefresh.storio3.sqlite.queries.InsertQuery;
import com.pushtorefresh.storio3.sqlite.queries.UpdateQuery;

public class PlaylistPutResolver extends DefaultPutResolver<Playlist> {

    @NonNull
    @Override
    protected InsertQuery mapToInsertQuery(@NonNull Playlist playlist) {
        return InsertQuery.builder()
                .table(PlaylistTable.TABLE_NAME)
                .build();
    }

    @NonNull
    @Override
    protected UpdateQuery mapToUpdateQuery(@NonNull Playlist playlist) {
        return UpdateQuery.builder()
                .table(PlaylistTable.TABLE_NAME)
                .where(PlaylistTable.Columns.PLAYLIST_ID + " = ?")
                .whereArgs(playlist.getId())
                .build();
    }

    @NonNull
    @Override
    protected ContentValues mapToContentValues(@NonNull Playlist playlist) {
        final ContentValues values = new ContentValues();

        values.put(PlaylistTable.Columns.PLAYLIST_ID, playlist.getId());
        values.put(PlaylistTable.Columns.PLAYLIST_NAME, playlist.getName());

        if (playlist.getDateAdded() != null)
            values.put(PlaylistTable.Columns.DATE_ADDED, playlist.getDateAdded().getTime());

        if (playlist.getDateModified() != null)
            values.put(PlaylistTable.Columns.DATE_MODIFIED, playlist.getDateModified().getTime());

        values.put(PlaylistTable.Columns.IMAGE_PATH, playlist.getImagePath());

        Gson gson = new Gson();

        values.put(PlaylistTable.Columns.PLAYLIST_JSON, gson.toJson(playlist));

        return values;
    }
}

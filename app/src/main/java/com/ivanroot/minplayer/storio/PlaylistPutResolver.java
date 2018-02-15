package com.ivanroot.minplayer.storio;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.ivanroot.minplayer.playlist.IPlaylist;
import com.ivanroot.minplayer.playlist.Playlist;
import com.pushtorefresh.storio3.sqlite.operations.put.DefaultPutResolver;
import com.pushtorefresh.storio3.sqlite.queries.InsertQuery;
import com.pushtorefresh.storio3.sqlite.queries.UpdateQuery;


/**
 * Created by Ivan Root on 28.08.2017.
 */

public class PlaylistPutResolver extends DefaultPutResolver<Playlist> {
    @NonNull
    @Override
    protected InsertQuery mapToInsertQuery(@NonNull Playlist playlist) {
        return InsertQuery.builder()
                .table(PlaylistTable.TABLE_PLAYLISTS)
                .build();
    }

    @NonNull
    @Override
    protected UpdateQuery mapToUpdateQuery(@NonNull Playlist playlist) {

        return UpdateQuery.builder()
                .table(PlaylistTable.TABLE_PLAYLISTS)
                .where(PlaylistTable.ROW_PLAYLIST_NAME + " = ?")
                .whereArgs(playlist.getName())
                .build();
    }

    @NonNull
    @Override
    protected ContentValues mapToContentValues(@NonNull Playlist playlist) {

        Gson gson = new Gson();
        String json = gson.toJson(playlist);
        ContentValues contentValues = new ContentValues();
        contentValues.put(PlaylistTable.ROW_PLAYLIST_NAME,playlist.getName());
        contentValues.put(PlaylistTable.ROW_PLAYLIST_JSON,json);

        return contentValues;
    }
}

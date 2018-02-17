package com.ivanroot.minplayer.storio;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
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
                .table(PlaylistTable.TABLE)
                .build();
    }

    @NonNull
    @Override
    protected UpdateQuery mapToUpdateQuery(@NonNull Playlist playlist) {

        return UpdateQuery.builder()
                .table(PlaylistTable.TABLE)
                .where(PlaylistTable.PLAYLIST_NAME + " = ?")
                .whereArgs(playlist.getName())
                .build();
    }

    @NonNull
    @Override
    protected ContentValues mapToContentValues(@NonNull Playlist playlist) {

        Gson gson = new Gson();
        String json = gson.toJson(playlist);
        ContentValues contentValues = new ContentValues();
        contentValues.put(PlaylistTable.PLAYLIST_NAME,playlist.getName());
        contentValues.put(PlaylistTable.PLAYLIST_JSON,json);
        contentValues.put(PlaylistTable.PLAYLIST_IMAGE,playlist.getImage());

        return contentValues;
    }
}

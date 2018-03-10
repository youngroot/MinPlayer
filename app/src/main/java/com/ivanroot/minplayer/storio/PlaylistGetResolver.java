package com.ivanroot.minplayer.storio;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ivanroot.minplayer.playlist.Playlist;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.operations.get.DefaultGetResolver;

import java.lang.reflect.Type;

/**
 * Created by Ivan Root on 28.08.2017.
 */

public class PlaylistGetResolver extends DefaultGetResolver<Playlist> {
    @NonNull
    @Override
    public Playlist mapFromCursor(StorIOSQLite storIOSQLite, @NonNull Cursor cursor) {
        String json = cursor.getString(cursor.getColumnIndex(PlaylistTable.Playlist.JSON));
        Type typePlaylist = new TypeToken<Playlist>(){}.getType();
        Playlist playlist = new Gson().fromJson(json,typePlaylist);
        playlist.setName(cursor.getString(cursor.getColumnIndex(PlaylistTable.Playlist.NAME)));
        playlist.setImagePath(cursor.getString(cursor.getColumnIndex(PlaylistTable.Playlist.IMAGE_PATH)));
        return playlist;
    }
}

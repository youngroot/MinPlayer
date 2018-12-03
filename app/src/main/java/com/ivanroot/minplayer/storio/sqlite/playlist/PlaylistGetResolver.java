package com.ivanroot.minplayer.storio.sqlite.playlist;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.ivanroot.minplayer.playlist.Playlist;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.operations.get.DefaultGetResolver;

public class PlaylistGetResolver extends DefaultGetResolver<Playlist> {

    @NonNull
    @Override
    public Playlist mapFromCursor(@NonNull StorIOSQLite storIOSQLite, @NonNull Cursor cursor) {
        PlaylistTable.PlaylistColumnsBundle bundle = PlaylistTable.PlaylistColumnsBundle.buildFromCursor(cursor);
        String json = bundle.getPlaylistJson();
        Gson gson = new Gson();
        return gson.fromJson(json, Playlist.class);
    }
}

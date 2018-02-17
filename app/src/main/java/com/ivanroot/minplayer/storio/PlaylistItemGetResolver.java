package com.ivanroot.minplayer.storio;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.ivanroot.minplayer.playlist.PlaylistItem;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.operations.get.DefaultGetResolver;

/**
 * Created by Ivan Root on 17.02.2018.
 */

public class PlaylistItemGetResolver extends DefaultGetResolver<PlaylistItem> {
    @NonNull
    @Override
    public PlaylistItem mapFromCursor(@NonNull StorIOSQLite storIOSQLite, @NonNull Cursor cursor) {
        PlaylistItem playlistItem = new PlaylistItem();
        playlistItem.setName(cursor.getString(cursor.getColumnIndex(PlaylistTable.PLAYLIST_NAME)));
        playlistItem.setImage(cursor.getBlob(cursor.getColumnIndex(PlaylistTable.PLAYLIST_IMAGE)));
        return playlistItem;
    }
}

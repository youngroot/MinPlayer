package com.ivanroot.minplayer.storio;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.ivanroot.minplayer.playlist.PlaylistItem;
import com.pushtorefresh.storio3.sqlite.operations.put.DefaultPutResolver;
import com.pushtorefresh.storio3.sqlite.queries.InsertQuery;
import com.pushtorefresh.storio3.sqlite.queries.UpdateQuery;

/**
 * Created by Ivan Root on 17.02.2018.
 */

public class PlaylistItemPutResolver extends DefaultPutResolver<PlaylistItem> {
    @NonNull
    @Override
    protected InsertQuery mapToInsertQuery(@NonNull PlaylistItem playlistItem) {
        return InsertQuery.builder()
                .table(PlaylistTable.TABLE)
                .build();
    }

    @NonNull
    @Override
    protected UpdateQuery mapToUpdateQuery(@NonNull PlaylistItem playlistItem) {
        return UpdateQuery.builder()
                .table(PlaylistTable.TABLE)
                .where(PlaylistTable.PLAYLIST_NAME + " = ?")
                .whereArgs(playlistItem.getName())
                .build();
    }

    @NonNull
    @Override
    protected ContentValues mapToContentValues(@NonNull PlaylistItem playlistItem) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(PlaylistTable.PLAYLIST_NAME,playlistItem.getName());
        contentValues.put(PlaylistTable.PLAYLIST_IMAGE, playlistItem.getImage());
        return contentValues;
    }
}

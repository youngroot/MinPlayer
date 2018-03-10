package com.ivanroot.minplayer.storio;

import android.support.annotation.NonNull;

import com.ivanroot.minplayer.playlist.PlaylistItem;
import com.ivanroot.minplayer.utils.Utils;
import com.pushtorefresh.storio3.sqlite.operations.delete.DefaultDeleteResolver;
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery;

import java.io.File;

/**
 * Created by Ivan Root on 17.02.2018.
 */

public class PlaylistItemDeleteResolver extends DefaultDeleteResolver<PlaylistItem> {
    @NonNull
    @Override
    protected DeleteQuery mapToDeleteQuery(@NonNull PlaylistItem playlistItem) {
        Utils.deleteFile(playlistItem.getImagePath());
        return DeleteQuery.builder()
                .table(PlaylistTable.TABLE)
                .where(PlaylistTable.Playlist.NAME + " = ?")
                .whereArgs(playlistItem.getName())
                .build();
    }
}

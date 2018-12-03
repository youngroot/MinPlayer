package com.ivanroot.minplayer.storio.contentresolver.playlistitem;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.ivanroot.minplayer.playlist.PlaylistItem;
import com.pushtorefresh.storio3.contentresolver.operations.put.DefaultPutResolver;
import com.pushtorefresh.storio3.contentresolver.queries.InsertQuery;
import com.pushtorefresh.storio3.contentresolver.queries.UpdateQuery;

/**
 * Created by ivanroot on 3/24/18.
 */

public class PlaylistItemPutResolver extends DefaultPutResolver<PlaylistItem> {

    @NonNull
    @Override
    protected InsertQuery mapToInsertQuery(@NonNull PlaylistItem object) {
        return null;
    }

    @NonNull
    @Override
    protected UpdateQuery mapToUpdateQuery(@NonNull PlaylistItem object) {
        return null;
    }

    @NonNull
    @Override
    protected ContentValues mapToContentValues(@NonNull PlaylistItem object) {
        return null;
    }
}

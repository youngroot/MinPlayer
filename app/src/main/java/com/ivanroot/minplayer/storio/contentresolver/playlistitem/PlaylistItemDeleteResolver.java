package com.ivanroot.minplayer.storio.contentresolver.playlistitem;

import android.support.annotation.NonNull;

import com.ivanroot.minplayer.playlist.PlaylistItem;
import com.pushtorefresh.storio3.contentresolver.operations.delete.DefaultDeleteResolver;
import com.pushtorefresh.storio3.contentresolver.queries.DeleteQuery;

/**
 * Created by ivanroot on 3/24/18.
 */

public class PlaylistItemDeleteResolver extends DefaultDeleteResolver<PlaylistItem> {
    @NonNull
    @Override
    protected DeleteQuery mapToDeleteQuery(@NonNull PlaylistItem object) {
        return null;
    }
}

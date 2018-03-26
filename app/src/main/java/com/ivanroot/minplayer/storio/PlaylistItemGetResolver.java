package com.ivanroot.minplayer.storio;

import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import com.ivanroot.minplayer.playlist.PlaylistItem;
import com.pushtorefresh.storio3.contentresolver.StorIOContentResolver;
import com.pushtorefresh.storio3.contentresolver.operations.get.DefaultGetResolver;

/**
 * Created by ivanroot on 3/24/18.
 */

public class PlaylistItemGetResolver extends DefaultGetResolver<PlaylistItem> {
    @NonNull
    @Override
    public PlaylistItem mapFromCursor(@NonNull StorIOContentResolver contentResolver, @NonNull Cursor cursor) {
        String playlistName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME));
        PlaylistItem playlistItem = new PlaylistItem();
        playlistItem.setName(playlistName);
        return playlistItem;
    }
}

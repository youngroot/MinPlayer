package com.ivanroot.minplayer.storio.contentresolver.playlistitem;

import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ivanroot.minplayer.playlist.PlaylistItem;
import com.pushtorefresh.storio3.contentresolver.StorIOContentResolver;
import com.pushtorefresh.storio3.contentresolver.operations.get.DefaultGetResolver;
import com.pushtorefresh.storio3.contentresolver.queries.Query;

import java.lang.reflect.Type;
import java.util.Date;


/**
 * Created by ivanroot on 3/24/18.
 */

public class PlaylistItemGetResolver extends DefaultGetResolver<PlaylistItem> {
    @NonNull
    @Override
    public PlaylistItem mapFromCursor(@NonNull StorIOContentResolver contentResolver, @NonNull Cursor cursor) {
        long playlistId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists._ID));
        String playlistName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME));
        int playlistSize = 0;
        //Date dateAdded  = new Date(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists.DATE_ADDED)));
        //Date dateModified = new Date(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists.DATE_MODIFIED)));
        //Log.i(toString(),dateAdded + " " + dateModified);
        Uri membersUri = MediaStore.Audio.Playlists.Members.getContentUri("external",playlistId);
        String[] imagePaths = new String[4];

        try {

            Cursor membersCursor = contentResolver
                    .get()
                    .cursor()
                    .withQuery(Query.builder()
                            .uri(membersUri)
                            .sortOrder(MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER)
                            .build())
                    .prepare()
                    .executeAsBlocking();

            playlistSize = membersCursor.getCount();

            int i = 0;
            while (membersCursor.moveToNext()) {

                if (i == 4) break;
                long albumId = membersCursor.getLong(membersCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ALBUM_ID));
                Log.i(toString(), String.valueOf(albumId));

                if (albumId != -1) {
                    Cursor albumCursor = contentResolver.get()
                            .cursor()
                            .withQuery(Query.builder()
                                    .uri(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI)
                                    .where(MediaStore.Audio.Albums._ID + " = ?")
                                    .whereArgs(albumId)
                                    .build())
                            .prepare()
                            .executeAsBlocking();
                    albumCursor.moveToFirst();

                    String albumArtPath = albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                    albumCursor.close();

                    if (albumArtPath != null) {
                        Log.i(toString(), albumArtPath);
                        imagePaths[i] = albumArtPath;
                        i++;
                    }
                }
            }
            membersCursor.close();
        }catch (NullPointerException | CursorIndexOutOfBoundsException ex){
            ex.printStackTrace();
            Log.e(toString(),ex.getMessage());
        }
        return new PlaylistItem(playlistName,playlistSize,imagePaths,null,null);
    }
}

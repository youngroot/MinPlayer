package com.ivanroot.minplayer.storio;

import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ivanroot.minplayer.audio.Audio;
import com.pushtorefresh.storio3.contentresolver.StorIOContentResolver;
import com.pushtorefresh.storio3.contentresolver.operations.get.DefaultGetResolver;
import com.pushtorefresh.storio3.contentresolver.queries.Query;

/**
 * Created by Ivan Root on 28.08.2017.
 */

public class MediaStoreAudioGetResolver extends DefaultGetResolver<Audio> {


    @NonNull
    @Override
    public Audio mapFromCursor(StorIOContentResolver contentResolver, @NonNull Cursor cursor) {

        int albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
        String id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
        String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
        String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
        String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
        String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

        Audio audio = new Audio(id, data, title, album, artist);

        try {
            Cursor tempCursor = contentResolver.get()
                    .cursor()
                    .withQuery(Query.builder()
                            .uri(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI)
                            .where(MediaStore.Audio.Albums._ID + " = ?")
                            .whereArgs(albumId)
                            .build())
                    .prepare()
                    .executeAsBlocking();
            tempCursor.moveToFirst();

            String albumArt = tempCursor.getString(tempCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
            tempCursor.close();
            Log.i(toString(),albumArt);
            audio.setAlbumArt(albumArt);

        } catch (NullPointerException | CursorIndexOutOfBoundsException ex) {
            Log.e(toString(), ex.getMessage());
        }

        return audio;
    }
}

package com.ivanroot.minplayer.storio.contentresolver.audio;

import android.content.ContentValues;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import com.ivanroot.minplayer.audio.Audio;
import com.pushtorefresh.storio3.contentresolver.operations.put.DefaultPutResolver;
import com.pushtorefresh.storio3.contentresolver.queries.InsertQuery;
import com.pushtorefresh.storio3.contentresolver.queries.UpdateQuery;


/**
 * Created by Ivan Root on 28.08.2017.
 */

public class AudioPutResolver extends DefaultPutResolver<Audio> {

    @NonNull
    @Override
    protected InsertQuery mapToInsertQuery(@NonNull Audio audio) {
        return InsertQuery.builder()
                .uri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                .build();
    }

    @NonNull
    @Override
    protected UpdateQuery mapToUpdateQuery(@NonNull Audio audio) {
        return UpdateQuery.builder()
                .uri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                .where(MediaStore.Audio.Media._ID + " = ?")
                .whereArgs(audio.getId())
                .build();
    }

    @NonNull
    @Override
    protected ContentValues mapToContentValues(@NonNull Audio audio) {
        final ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Audio.Media._ID,audio.getId());
        contentValues.put(MediaStore.Audio.Media.DATA,audio.getLocalData());
        contentValues.put(MediaStore.Audio.Media.TITLE,audio.getTitle());
        contentValues.put(MediaStore.Audio.Media.ALBUM,audio.getAlbum());
        contentValues.put(MediaStore.Audio.Media.ARTIST,audio.getArtist());
        return contentValues;
    }
}

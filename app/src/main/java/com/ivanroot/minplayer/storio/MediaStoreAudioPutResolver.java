package com.ivanroot.minplayer.storio;

import android.content.ContentValues;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import com.ivanroot.minplayer.audio.Audio;
import com.pushtorefresh.storio.contentresolver.operations.put.DefaultPutResolver;
import com.pushtorefresh.storio.contentresolver.queries.InsertQuery;
import com.pushtorefresh.storio.contentresolver.queries.UpdateQuery;

/**
 * Created by Ivan Root on 28.08.2017.
 */

public class MediaStoreAudioPutResolver extends DefaultPutResolver<Audio> {

    private static final Uri EXTERNAL_AUDIO_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    @NonNull
    @Override
    protected InsertQuery mapToInsertQuery(@NonNull Audio audio) {

        return InsertQuery.builder()
                .uri(EXTERNAL_AUDIO_URI)
                .build();
    }

    @NonNull
    @Override
    protected UpdateQuery mapToUpdateQuery(@NonNull Audio audio) {

        return UpdateQuery.builder()
                .uri(EXTERNAL_AUDIO_URI)
                .where(MediaStore.Audio.Media._ID + " = ?")
                .whereArgs(audio.getId())
                .build();
    }

    @NonNull
    @Override
    protected ContentValues mapToContentValues(@NonNull Audio audio) {

        final ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Audio.Media._ID,audio.getId());
        contentValues.put(MediaStore.Audio.Media.DATA,audio.getData());
        contentValues.put(MediaStore.Audio.Media.TITLE,audio.getTitle());
        contentValues.put(MediaStore.Audio.Media.ALBUM,audio.getAlbum());
        contentValues.put(MediaStore.Audio.Media.ARTIST,audio.getArtist());

        return contentValues;
    }
}

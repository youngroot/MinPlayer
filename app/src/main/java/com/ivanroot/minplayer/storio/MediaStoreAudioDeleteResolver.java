package com.ivanroot.minplayer.storio;

import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import com.ivanroot.minplayer.audio.Audio;
import com.pushtorefresh.storio.contentresolver.operations.delete.DefaultDeleteResolver;
import com.pushtorefresh.storio.contentresolver.queries.DeleteQuery;

/**
 * Created by Ivan Root on 28.08.2017.
 */

public class MediaStoreAudioDeleteResolver extends DefaultDeleteResolver<Audio> {

    private static final Uri EXTERNAL_AUDIO_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    @NonNull
    @Override
    protected DeleteQuery mapToDeleteQuery(@NonNull Audio audio) {
        return DeleteQuery.builder()
                .uri(EXTERNAL_AUDIO_URI)
                .where(MediaStore.Audio.Media._ID + " = ?")
                .whereArgs(audio.getId())
                .build();
    }
}
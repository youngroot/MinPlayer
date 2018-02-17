package com.ivanroot.minplayer.storio;

import android.provider.MediaStore;
import android.support.annotation.NonNull;

import com.ivanroot.minplayer.audio.Audio;
import com.pushtorefresh.storio3.contentresolver.operations.delete.DefaultDeleteResolver;
import com.pushtorefresh.storio3.contentresolver.queries.DeleteQuery;
/**
 * Created by Ivan Root on 28.08.2017.
 */

public class MediaStoreAudioDeleteResolver extends DefaultDeleteResolver<Audio> {

    @NonNull
    @Override
    protected DeleteQuery mapToDeleteQuery(@NonNull Audio audio) {
        return DeleteQuery.builder()
                .uri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                .where(MediaStore.Audio.Media._ID + " = ?")
                .whereArgs(audio.getId())
                .build();
    }
}

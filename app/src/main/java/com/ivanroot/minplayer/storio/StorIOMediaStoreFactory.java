package com.ivanroot.minplayer.storio;


import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;

import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.playlist.Playlist;
import com.pushtorefresh.storio3.contentresolver.ContentResolverTypeMapping;
import com.pushtorefresh.storio3.contentresolver.StorIOContentResolver;
import com.pushtorefresh.storio3.contentresolver.impl.DefaultStorIOContentResolver;
import com.pushtorefresh.storio3.contentresolver.queries.Query;

import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Created by Ivan Root on 28.08.2017.
 */

public class StorIOMediaStoreFactory {

    private static StorIOContentResolver INSTANCE;
    private static BehaviorSubject<List<Audio>> subject = BehaviorSubject.create();
    private static final Uri EXTERNAL_AUDIO_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    public static synchronized StorIOContentResolver get(Context context) {
        if (INSTANCE != null)
            return INSTANCE;

        INSTANCE = DefaultStorIOContentResolver.builder()
                .contentResolver(context.getContentResolver())
                .addTypeMapping(Audio.class, ContentResolverTypeMapping.<Audio>builder()
                        .putResolver(new MediaStoreAudioPutResolver())
                        .getResolver(new MediaStoreAudioGetResolver())
                        .deleteResolver(new MediaStoreAudioDeleteResolver())
                        .build())
                .build();

        return INSTANCE;
    }

    public static Observable<List<Audio>> getAllAudioObservable(Context context, String sortOrder) {
        return StorIOMediaStoreFactory
                .get(context)
                .get()
                .listOfObjects(Audio.class)
                .withQuery(Query.builder()
                        .uri(EXTERNAL_AUDIO_URI)
                        .sortOrder(sortOrder)
                        .where(MediaStore.Audio.Media.IS_MUSIC + "!= 0")
                        .build())
                .prepare()
                .asRxFlowable(BackpressureStrategy.LATEST)
                .toObservable();

    }
}

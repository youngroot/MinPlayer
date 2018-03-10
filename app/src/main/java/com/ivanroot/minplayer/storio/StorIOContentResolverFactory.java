package com.ivanroot.minplayer.storio;


import android.content.Context;
import android.provider.MediaStore;

import com.ivanroot.minplayer.audio.Audio;
import com.pushtorefresh.storio3.contentresolver.ContentResolverTypeMapping;
import com.pushtorefresh.storio3.contentresolver.StorIOContentResolver;
import com.pushtorefresh.storio3.contentresolver.impl.DefaultStorIOContentResolver;
import com.pushtorefresh.storio3.contentresolver.queries.Query;

import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;

/**
 * Created by Ivan Root on 28.08.2017.
 */

public class StorIOContentResolverFactory {

    private static StorIOContentResolver INSTANCE;

    public static synchronized StorIOContentResolver get(Context context) {
        if (INSTANCE != null)
            return INSTANCE;

        INSTANCE = DefaultStorIOContentResolver.builder()
                .contentResolver(context.getContentResolver())
                .addTypeMapping(Audio.class, ContentResolverTypeMapping.<Audio>builder()
                        .putResolver(new AudioPutResolver())
                        .getResolver(new AudioGetResolver())
                        .deleteResolver(new AudioDeleteResolver())
                        .build())
                .build();

        return INSTANCE;
    }

    public static Observable<List<Audio>> getAllAudioObservable(Context context, String sortOrder) {
        return StorIOContentResolverFactory
                .get(context)
                .get()
                .listOfObjects(Audio.class)
                .withQuery(Query.builder()
                        .uri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                        .sortOrder(sortOrder)
                        .where(MediaStore.Audio.Media.IS_MUSIC + " != 0")
                        .build())
                .prepare()
                .asRxFlowable(BackpressureStrategy.LATEST)
                .toObservable();

    }
}

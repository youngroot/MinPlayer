package com.ivanroot.minplayer.storio;


import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;

import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.utils.Utils;
import com.ivanroot.minplayer.playlist.Playlist;
import com.pushtorefresh.storio.contentresolver.ContentResolverTypeMapping;
import com.pushtorefresh.storio.contentresolver.StorIOContentResolver;
import com.pushtorefresh.storio.contentresolver.impl.DefaultStorIOContentResolver;
import com.pushtorefresh.storio.contentresolver.queries.Query;

import java.util.List;

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

    public static Observable<List<Audio>> createMediaStoreAudioObservable(Context context, String sortOrder) {
        return Utils.v2(StorIOMediaStoreFactory
                .get(context)
                .get()
                .listOfObjects(Audio.class)
                .withQuery(Query.builder()
                        .uri(EXTERNAL_AUDIO_URI)
                        .sortOrder(sortOrder)
                        .where(MediaStore.Audio.Media.IS_MUSIC + "!= 0")
                        .build())
                .prepare()
                .asRxObservable());
    }

    public static Observable<Playlist> createPlaylistObservable(Context context, String sortOrder) {

        return Observable
                .combineLatest(
                        createMediaStoreAudioObservable(context, sortOrder)
                                .map(list -> new Playlist(PlaylistTable.ALL_TRACKS_PLAYLIST).setAudioList(list)),
                        StorIOPlaylistFactory
                                .createPlaylistObservable(context, PlaylistTable.ALL_TRACKS_PLAYLIST),
                        (playlist1, playlist2) -> {
                            if (playlist2.isShuffled()) playlist1.shuffle();
                            playlist1.setRepeatMode(playlist2.getRepeatMode());
                            playlist1.checkAndSetAudio(playlist2.getCurrentAudio());
                            return playlist1;
                        });
    }
}

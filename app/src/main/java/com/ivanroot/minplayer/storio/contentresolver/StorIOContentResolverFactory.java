package com.ivanroot.minplayer.storio.contentresolver;


import android.content.Context;
import android.provider.MediaStore;

import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.playlist.Playlist;
import com.ivanroot.minplayer.playlist.PlaylistItem;
import com.ivanroot.minplayer.storio.contentresolver.audio.AudioDeleteResolver;
import com.ivanroot.minplayer.storio.contentresolver.audio.AudioGetResolver;
import com.ivanroot.minplayer.storio.contentresolver.audio.AudioPutResolver;
import com.ivanroot.minplayer.storio.contentresolver.playlist.PlaylistDeleteResolver;
import com.ivanroot.minplayer.storio.contentresolver.playlist.PlaylistGetResolver;
import com.ivanroot.minplayer.storio.contentresolver.playlistitem.PlaylistItemDeleteResolver;
import com.ivanroot.minplayer.storio.contentresolver.playlist.PlaylistPutResolver;
import com.ivanroot.minplayer.storio.contentresolver.playlistitem.PlaylistItemGetResolver;
import com.ivanroot.minplayer.storio.contentresolver.playlistitem.PlaylistItemPutResolver;
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

    public static StorIOContentResolver get(Context context) {
        if (INSTANCE != null)
            return INSTANCE;

        INSTANCE = DefaultStorIOContentResolver.builder()
                .contentResolver(context.getContentResolver())
                .addTypeMapping(Audio.class, ContentResolverTypeMapping.<Audio>builder()
                        .putResolver(new AudioPutResolver())
                        .getResolver(new AudioGetResolver())
                        .deleteResolver(new AudioDeleteResolver())
                        .build())
                .addTypeMapping(Playlist.class, ContentResolverTypeMapping.<Playlist>builder()
                        .putResolver(new PlaylistPutResolver())
                        .getResolver(new PlaylistGetResolver())
                        .deleteResolver(new PlaylistDeleteResolver())
                        .build())
                .addTypeMapping(PlaylistItem.class, ContentResolverTypeMapping.<PlaylistItem>builder()
                        .putResolver(new PlaylistItemPutResolver())
                        .getResolver(new PlaylistItemGetResolver())
                        .deleteResolver(new PlaylistItemDeleteResolver())
                        .build())
                .build();

        return INSTANCE;
    }

    public static Observable<List<Audio>> getAllAudioObservable(Context context, String sortOrder) {
        return get(context)
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

    public static Observable<Playlist> getPlaylistObservable(Context context, String playlistName) {
        return get(context)
                .get()
                .listOfObjects(Playlist.class)
                .withQuery(Query.builder()
                        .uri(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI)
                        .where(MediaStore.Audio.Playlists.NAME + " = ?")
                        .whereArgs(playlistName)
                        .build())
                .prepare()
                .asRxFlowable(BackpressureStrategy.LATEST)
                .toObservable()
                .flatMap(Observable::fromIterable);
    }

    public static Observable<Playlist> getPlaylistObservable(Context context, long playlistId) {
        return get(context)
                .get()
                .listOfObjects(Playlist.class)
                .withQuery(Query.builder()
                        .uri(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI)
                        .where(MediaStore.Audio.Playlists._ID + " = ?")
                        .whereArgs(playlistId)
                        .build())
                .prepare()
                .asRxFlowable(BackpressureStrategy.LATEST)
                .toObservable()
                .flatMap(Observable::fromIterable);
    }

    public static Observable<List<PlaylistItem>> getPlaylistItemsObservable(Context context) {
        return get(context)
                .get()
                .listOfObjects(PlaylistItem.class)
                .withQuery(Query.builder()
                        .uri(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI)
                        .build())
                .prepare()
                .asRxFlowable(BackpressureStrategy.LATEST)
                .toObservable();
    }
}

package com.ivanroot.minplayer.storio;

import android.content.Context;

import com.ivanroot.minplayer.playlist.Playlist;
import com.ivanroot.minplayer.playlist.PlaylistItem;
import com.pushtorefresh.storio3.sqlite.SQLiteTypeMapping;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.impl.DefaultStorIOSQLite;
import com.pushtorefresh.storio3.sqlite.queries.Query;

import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;

/**
 * Created by Ivan Root on 28.08.2017.
 */

public class StorIOFactory {

    private static StorIOSQLite INSTANCE;

    public synchronized static StorIOSQLite get(Context context) {
        if (INSTANCE != null) {
            return INSTANCE;
        }

        INSTANCE = DefaultStorIOSQLite.builder()
                .sqliteOpenHelper(new DbOpenHelper(context))
                .addTypeMapping(Playlist.class, SQLiteTypeMapping.<Playlist>builder()
                        .putResolver(new PlaylistPutResolver())
                        .getResolver(new PlaylistGetResolver())
                        .deleteResolver(new PlaylistDeleteResolver())
                        .build())
                .addTypeMapping(PlaylistItem.class,SQLiteTypeMapping.<PlaylistItem>builder()
                        .putResolver(new PlaylistItemPutResolver())
                        .getResolver(new PlaylistItemGetResolver())
                        .deleteResolver(new PlaylistItemDeleteResolver())
                        .build())
                .build();

        return INSTANCE;
    }

    public static Observable<Playlist> getPlaylistObservable(Context context, String playlistName){

        return StorIOFactory
                .get(context)
                .get()
                .listOfObjects(Playlist.class)
                .withQuery(Query.builder()
                        .table(PlaylistTable.TABLE)
                        .where(PlaylistTable.Playlist.NAME + " = ?")
                        .whereArgs(playlistName)
                        .build())
                .prepare()
                .asRxFlowable(BackpressureStrategy.LATEST)
                .toObservable()
                .flatMap(Observable::fromIterable);
    }

    public static Observable<List<PlaylistItem>> getPlaylistItemsObservable(Context context){
        return StorIOFactory
                .get(context)
                .get()
                .listOfObjects(PlaylistItem.class)
                .withQuery(Query.builder()
                        .table(PlaylistTable.TABLE)
                        .where(PlaylistTable.Playlist.NAME + " != ?")
                        .whereArgs(PlaylistTable.Playlist.ALL_TRACKS_PLAYLIST)
                        .build())
                .prepare()
                .asRxFlowable(BackpressureStrategy.LATEST)
                .toObservable();
    }

}

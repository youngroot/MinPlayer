package com.ivanroot.minplayer.storio;

import android.content.Context;

import com.ivanroot.minplayer.utils.Utils;
import com.ivanroot.minplayer.playlist.Playlist;
import com.pushtorefresh.storio3.sqlite.SQLiteTypeMapping;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.impl.DefaultStorIOSQLite;
import com.pushtorefresh.storio3.sqlite.queries.Query;

import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;

/**
 * Created by Ivan Root on 28.08.2017.
 */

public class StorIOPlaylistFactory  {

    private static StorIOSQLite INSTANCE;

    public synchronized static StorIOSQLite get(Context context) {
        if (INSTANCE != null) {
            return INSTANCE;
        }

        INSTANCE = DefaultStorIOSQLite.builder()
                .sqliteOpenHelper(new DbHelper(context))
                .addTypeMapping(Playlist.class, SQLiteTypeMapping.<Playlist>builder()
                        .putResolver(new PlaylistPutResolver())
                        .getResolver(new PlaylistGetResolver())
                        .deleteResolver(new PlaylistDeleteResolver())
                        .build())
                .build();

        return INSTANCE;
    }

    public static Observable<Playlist> getPlaylistObservable(Context context, String playlistName){

        return StorIOPlaylistFactory
                .get(context)
                .get()
                .listOfObjects(Playlist.class)
                .withQuery(Query.builder()
                        .table(PlaylistTable.TABLE_PLAYLISTS)
                        .where(PlaylistTable.ROW_PLAYLIST_NAME + " = ?")
                        .whereArgs(playlistName)
                        .build())
                .prepare()
                .asRxFlowable(BackpressureStrategy.LATEST)
                .toObservable()
                .flatMap(Observable::fromIterable);
    }

    public static Observable<List<Playlist>> getAllPlaylistsObservable(Context context){
        return StorIOPlaylistFactory
                .get(context)
                .get()
                .listOfObjects(Playlist.class)
                .withQuery(Query.builder()
                        .table(PlaylistTable.TABLE_PLAYLISTS)
                        .where(PlaylistTable.ROW_PLAYLIST_NAME + " != ?")
                        .whereArgs(PlaylistTable.ALL_TRACKS_PLAYLIST)
                        .build())
                .prepare()
                .asRxFlowable(BackpressureStrategy.LATEST)
                .toObservable();
    }

}

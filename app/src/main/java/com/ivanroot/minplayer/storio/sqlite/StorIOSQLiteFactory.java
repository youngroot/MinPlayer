package com.ivanroot.minplayer.storio.sqlite;

import android.content.Context;

import com.ivanroot.minplayer.playlist.Playlist;
import com.ivanroot.minplayer.storio.sqlite.playlist.PlaylistDeleteResolver;
import com.ivanroot.minplayer.storio.sqlite.playlist.PlaylistGetResolver;
import com.ivanroot.minplayer.storio.sqlite.playlist.PlaylistPutResolver;
import com.ivanroot.minplayer.storio.sqlite.playlist.PlaylistTable;
import com.pushtorefresh.storio3.sqlite.SQLiteTypeMapping;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.impl.DefaultStorIOSQLite;
import com.pushtorefresh.storio3.sqlite.queries.Query;

import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;

public class StorIOSQLiteFactory {
    private static StorIOSQLite INSTANCE;

    public synchronized static StorIOSQLite get(Context context){
        if(INSTANCE != null)
            return INSTANCE;

        INSTANCE = DefaultStorIOSQLite.builder()
                .sqliteOpenHelper(new StorIOSQLiteOpenHelper(context))
                .addTypeMapping(Playlist.class, SQLiteTypeMapping.<Playlist>builder()
                        .putResolver(new PlaylistPutResolver())
                        .getResolver(new PlaylistGetResolver())
                        .deleteResolver(new PlaylistDeleteResolver())
                        .build())
                .build();

        return INSTANCE;
    }

    public static Observable<Playlist>getPlaylistObservable(Context context, String playlistName){
        return get(context)
                .get()
                .listOfObjects(Playlist.class)
                .withQuery(Query.builder()
                        .table(PlaylistTable.TABLE_NAME)
                        .where(PlaylistTable.Columns.PLAYLIST_NAME + " = ?")
                        .whereArgs(playlistName)
                        .build())
                .prepare()
                .asRxFlowable(BackpressureStrategy.LATEST)
                .toObservable()
                .flatMap(Observable::fromIterable);
    }

    public static Observable<Playlist>getPlaylistObservable(Context context, long playlistId){
        return get(context)
                .get()
                .listOfObjects(Playlist.class)
                .withQuery(Query.builder()
                        .table(PlaylistTable.TABLE_NAME)
                        .where(PlaylistTable.Columns.PLAYLIST_ID + " = ?")
                        .whereArgs(playlistId)
                .build())
                .prepare()
                .asRxFlowable(BackpressureStrategy.LATEST)
                .toObservable()
                .flatMap(Observable::fromIterable);
    }
}

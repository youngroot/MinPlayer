package com.ivanroot.minplayer.storio;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.ivanroot.minplayer.playlist.Playlist;

import static com.ivanroot.minplayer.storio.PlaylistTable.ALL_TRACKS_PLAYLIST;
import static com.ivanroot.minplayer.storio.PlaylistTable.DB_NAME;
import static com.ivanroot.minplayer.storio.PlaylistTable.DB_VERSION;
import static com.ivanroot.minplayer.storio.PlaylistTable.PLAYLIST_JSON;
import static com.ivanroot.minplayer.storio.PlaylistTable.PLAYLIST_NAME;
import static com.ivanroot.minplayer.storio.PlaylistTable.TABLE;
import static com.ivanroot.minplayer.storio.PlaylistTable.createTableQuery;



/**
 * Created by Ivan Root on 28.08.2017.
 */

public class DbOpenHelper extends SQLiteOpenHelper {



    public DbOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {

        Gson gson = new Gson();
        String json = gson.toJson(new Playlist(ALL_TRACKS_PLAYLIST));
        db.execSQL(createTableQuery());
        db.execSQL(initialWritePlaylistQuery(ALL_TRACKS_PLAYLIST,json));

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private String initialWritePlaylistQuery(String name, String json){

        return "INSERT INTO "
                + TABLE + " ( "
                + PLAYLIST_NAME
                + ", "
                + PLAYLIST_JSON
                + " ) VALUES ( '"
                + name
                + "', '"
                + json
                + "' );";
    }
}

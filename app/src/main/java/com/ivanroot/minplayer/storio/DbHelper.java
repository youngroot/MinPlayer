package com.ivanroot.minplayer.storio;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.ivanroot.minplayer.playlist.Playlist;
import static com.ivanroot.minplayer.storio.PlaylistTable.*;



/**
 * Created by Ivan Root on 28.08.2017.
 */

public class DbHelper extends SQLiteOpenHelper {



    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        Gson gson = new Gson();
        String json = gson.toJson(new Playlist(ALL_TRACKS_PLAYLIST));
        db.execSQL(createTableQuery());
        db.execSQL(writePlaylistQuery(ALL_TRACKS_PLAYLIST,json));

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

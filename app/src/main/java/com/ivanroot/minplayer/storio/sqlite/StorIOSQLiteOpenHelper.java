package com.ivanroot.minplayer.storio.sqlite;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.content.Context;

import com.google.gson.Gson;
import com.ivanroot.minplayer.playlist.Playlist;
import com.ivanroot.minplayer.playlist.PlaylistManager;
import com.ivanroot.minplayer.storio.sqlite.playlist.PlaylistTable;

public class StorIOSQLiteOpenHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "MinPlayer.db";

    public StorIOSQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(PlaylistTable.getTableCreationQuery());

        Playlist playlist = new Playlist(PlaylistManager.ALL_TRACKS_PLAYLIST);
        Gson gson = new Gson();
        ContentValues values = new ContentValues();

        values.put(PlaylistTable.Columns.PLAYLIST_ID, 1);
        values.put(PlaylistTable.Columns.PLAYLIST_NAME, PlaylistManager.ALL_TRACKS_PLAYLIST);
        values.put(PlaylistTable.Columns.PLAYLIST_JSON, gson.toJson(playlist));
        sqLiteDatabase.insert(PlaylistTable.TABLE_NAME, null, values);

        values.clear();
        playlist.setName(PlaylistManager.DISK_ALL_TRACKS_PLAYLIST);

        values.put(PlaylistTable.Columns.PLAYLIST_ID, 2);
        values.put(PlaylistTable.Columns.PLAYLIST_NAME, PlaylistManager.DISK_ALL_TRACKS_PLAYLIST);
        values.put(PlaylistTable.Columns.PLAYLIST_JSON, gson.toJson(playlist));
        sqLiteDatabase.insert(PlaylistTable.TABLE_NAME, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}

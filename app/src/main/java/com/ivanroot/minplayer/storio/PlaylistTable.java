package com.ivanroot.minplayer.storio;

import android.provider.MediaStore;

/**
 * Created by Ivan Root on 28.08.2017.
 */

public class PlaylistTable {

    public static final String DB_NAME = "MinPlayerDb";
    public static final int DB_VERSION = 1;
    public static final String ALL_TRACKS_PLAYLIST = "com.ivanroot.minplayer.all_tracks_playlist";
    public static final String TABLE = "playlists";
    public static final String _ID = "_id";
    public static final String PLAYLIST_NAME = "playlist_name";
    public static final String PLAYLIST_JSON = "playlist_json";
    public static final String PLAYLIST_IMAGE = "playlist_image";
    public static final String ASC_SORT_ORDER = MediaStore.Audio.Media.TITLE + " ASC";

    public static String createTableQuery(){

        return "CREATE TABLE "
                + TABLE + " ("
                + _ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                + PLAYLIST_NAME
                + " TEXT, "
                + PLAYLIST_JSON
                + " TEXT, "
                + PLAYLIST_IMAGE
                + " BLOB"
                + ");";
    }
}

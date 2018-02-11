package com.ivanroot.minplayer.storio;

import android.provider.MediaStore;

import com.google.gson.Gson;
import com.ivanroot.minplayer.playlist.IPlaylist;
import com.ivanroot.minplayer.playlist.Playlist;

/**
 * Created by Ivan Root on 28.08.2017.
 */

public class PlaylistTable {

    public static final String DB_NAME = "MinPlayerDb";
    public static final int DB_VERSION = 1;
    public static final String ALL_TRACKS_PLAYLIST = "com.ivanroot.minplayer.all_tracks_playlist";
    public static final String TABLE_PLAYLISTS = "playlists";
    public static final String ROW_ID = "_id";
    public static final String ROW_PLAYLIST_NAME = "playlist_name";
    public static final String ROW_PLAYLIST_JSON = "playlist_json";
    public static final String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

    public static String createTableQuery(){

        return "CREATE TABLE "
                + TABLE_PLAYLISTS + " ("
                + ROW_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                + ROW_PLAYLIST_NAME
                + " TEXT, "
                + ROW_PLAYLIST_JSON
                + " TEXT"
                + ");";
    }

    public static String writePlaylistQuery(String name, String json){

        return "INSERT INTO "
                + TABLE_PLAYLISTS + " ( "
                + ROW_PLAYLIST_NAME
                + ", "
                + ROW_PLAYLIST_JSON
                + " ) VALUES ( '"
                + name
                + "', '"
                + json
                + "' );";
    }
}

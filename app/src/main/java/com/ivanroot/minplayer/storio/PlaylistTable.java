package com.ivanroot.minplayer.storio;

import android.provider.MediaStore;
import android.support.annotation.NonNull;

/**
 * Created by Ivan Root on 28.08.2017.
 */

public class PlaylistTable {

    public static final String DB_NAME = "MinPlayerDb";
    public static final int DB_VERSION = 1;
    public static final String TABLE = "playlists";
    public static final String ASC_SORT_ORDER = MediaStore.Audio.Media.TITLE + " ASC";

    public static class Playlist{

        public static final String ALL_TRACKS_PLAYLIST = "com.ivanroot.minplayer.all_tracks_playlist";
        public static final String NAME = "playlist_name";
        public static final String JSON = "playlist_json";
        public static final String IMAGE_PATH = "playlist_image";
        public static final String _ID = "_id";
        public static final String IMAGE_DIR = "playlists_images";
    }

    public static String createTableQuery(){

        return "CREATE TABLE "
                + TABLE + " ("
                + Playlist._ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                + Playlist.NAME
                + " TEXT, "
                + Playlist.JSON
                + " TEXT, "
                + Playlist.IMAGE_PATH
                + " TEXT"
                + ");";
    }

    public static String renamePlaylistQuery(@NonNull String oldName,@NonNull String newName){
        return "UPDATE"
                + TABLE + " SET "
                + Playlist.NAME + " = "
                +  newName + " WHERE "
                + Playlist.NAME + " = "
                + oldName + ";";
    }


}

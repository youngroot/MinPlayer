package com.ivanroot.minplayer.storio.sqlite.playlist;

import android.database.Cursor;
import android.support.annotation.NonNull;

public class PlaylistTable {
    public static final String TABLE_NAME = "playlist_table";

    private PlaylistTable(){}

    public static class Columns {
        public static final String ID = "_id";
        public static final String PLAYLIST_ID = "playlist_id";
        public static final String PLAYLIST_NAME = "playlist_name";
        public static final String DATE_ADDED = "date_added";
        public static final String DATE_MODIFIED = "date_modified";
        public static final String IMAGE_PATH = "image_path";
        public static final String PLAYLIST_JSON = "playlist_json";
    }

    public static class PlaylistColumnsBundle{
        private long playlistId;
        private String PlaylistName;
        private long dateAdded;
        private long dateModified;
        private String imagePath;
        private String playlistJson;

        public static PlaylistColumnsBundle buildFromCursor(@NonNull Cursor cursor){
            long playlistId = cursor.getLong(cursor.getColumnIndex(PlaylistTable.Columns.PLAYLIST_ID));
            String playlistName = cursor.getString(cursor.getColumnIndex(PlaylistTable.Columns.PLAYLIST_NAME));
            long dateAdded = cursor.getLong(cursor.getColumnIndex(PlaylistTable.Columns.DATE_ADDED));
            long dateModified = cursor.getLong(cursor.getColumnIndex(PlaylistTable.Columns.DATE_MODIFIED));
            String imagePath = cursor.getString(cursor.getColumnIndex(PlaylistTable.Columns.IMAGE_PATH));
            String playlistJson = cursor.getString(cursor.getColumnIndex(PlaylistTable.Columns.PLAYLIST_JSON));

            return new PlaylistColumnsBundle(playlistId, playlistName, dateAdded, dateModified, imagePath, playlistJson);
        }

        public PlaylistColumnsBundle(long playlistId, String playlistName, long dateAdded, long dateModified, String imagePath, String playlistJson) {
            this.playlistId = playlistId;
            PlaylistName = playlistName;
            this.dateAdded = dateAdded;
            this.dateModified = dateModified;
            this.imagePath = imagePath;
            this.playlistJson = playlistJson;
        }

        public long getPlaylistId() {
            return playlistId;
        }

        public void setPlaylistId(long playlistId) {
            this.playlistId = playlistId;
        }

        public String getPlaylistName() {
            return PlaylistName;
        }

        public void setPlaylistName(String playlistName) {
            PlaylistName = playlistName;
        }

        public long getDateAdded() {
            return dateAdded;
        }

        public void setDateAdded(long dateAdded) {
            this.dateAdded = dateAdded;
        }

        public long getDateModified() {
            return dateModified;
        }

        public void setDateModified(long dateModified) {
            this.dateModified = dateModified;
        }

        public String getImagePath() {
            return imagePath;
        }

        public void setImagePath(String imagePath) {
            this.imagePath = imagePath;
        }

        public String getPlaylistJson() {
            return playlistJson;
        }

        public void setPlaylistJson(String playlistJson) {
            this.playlistJson = playlistJson;
        }

    }

    public static String getTableCreationQuery(){
        return "CREATE TABLE "
                + TABLE_NAME +  " ( "
                + Columns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Columns.PLAYLIST_ID + " LONG,"
                + Columns.PLAYLIST_NAME + " TEXT NOT NULL,"
                + Columns.DATE_ADDED + " LONG,"
                + Columns.DATE_MODIFIED + " LONG,"
                + Columns.IMAGE_PATH + " TEXT,"
                + Columns.PLAYLIST_JSON + " TEXT);";
    }

}

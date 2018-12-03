package com.ivanroot.minplayer.storio.contentresolver.playlist;

import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;


import com.ivanroot.minplayer.playlist.Playlist;
import com.ivanroot.minplayer.storio.contentresolver.audio.AudioGetResolver;
import com.pushtorefresh.storio3.contentresolver.StorIOContentResolver;
import com.pushtorefresh.storio3.contentresolver.operations.get.DefaultGetResolver;
import com.pushtorefresh.storio3.contentresolver.queries.Query;

/**
 * Created by ivanroot on 3/23/18.
 */

public class PlaylistGetResolver extends DefaultGetResolver<Playlist> {
    @NonNull
    @Override
    public Playlist mapFromCursor(@NonNull StorIOContentResolver contentResolver, @NonNull Cursor cursor) {
        long playlistId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists._ID));
        String playlistName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME));
        //Date dateAdded  = new Date(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists.DATE_ADDED)));
        //Date dateModified = new Date(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists.DATE_MODIFIED)));

        Cursor playlistMembersCursor = contentResolver.get()
                .cursor()
                .withQuery(Query.builder()
                        .uri(MediaStore.Audio.Playlists.Members.getContentUri("external",playlistId))
                        .sortOrder(MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER)
                        .build())
                .prepare()
                .executeAsBlocking();

        Playlist playlist = new Playlist(playlistName);
        playlist.setId(playlistId);
        //playlist.setDateAdded(dateAdded);
        //playlist.setDateModified(dateModified);

        AudioGetResolver audioGetResolver = new AudioGetResolver();

        try {

            while (playlistMembersCursor.moveToNext()) {
                long audioId = playlistMembersCursor.getLong(playlistMembersCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID));
                Cursor audioCursor = contentResolver.get()
                        .cursor()
                        .withQuery(Query
                                .builder()
                                .uri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                                .where(MediaStore.Audio.Media._ID + " = ?")
                                .whereArgs(audioId)
                                .build())
                        .prepare()
                        .executeAsBlocking();
                audioCursor.moveToFirst();
                playlist.addAudio(audioGetResolver.mapFromCursor(contentResolver, audioCursor));
                audioCursor.close();
            }
            playlistMembersCursor.close();
        }

        catch (NullPointerException ex){
            //ex.printStackTrace();
            //Log.e(toString(),ex.getMessage());
        }

        return playlist;
    }


}

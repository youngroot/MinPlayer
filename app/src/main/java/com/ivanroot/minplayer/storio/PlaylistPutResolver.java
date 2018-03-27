package com.ivanroot.minplayer.storio;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ivanroot.minplayer.playlist.Playlist;
import com.pushtorefresh.storio3.contentresolver.StorIOContentResolver;
import com.pushtorefresh.storio3.contentresolver.operations.put.PutResolver;
import com.pushtorefresh.storio3.contentresolver.operations.put.PutResult;

/**
 * Created by ivanroot on 3/23/18.
 */

public class PlaylistPutResolver extends PutResolver<Playlist> {

    @NonNull
    @Override
    public PutResult performPut(@NonNull StorIOContentResolver storIOContentResolver, @NonNull Playlist playlist) {
        try {

            ContentResolver contentResolver = storIOContentResolver.lowLevel().contentResolver();

            long playlistId = playlist.getId();
            boolean newPlaylist = false;

            if (playlistId == -1) {
                ContentValues value = new ContentValues();
                value.put(MediaStore.Audio.Playlists.NAME,playlist.getName());
//                value.put(MediaStore.Audio.Playlists.DATE_ADDED,playlist.getDateAdded().getTime());
                playlistId = Long.parseLong(contentResolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, value).getLastPathSegment());
                newPlaylist = true;
            }

            Uri membersUri = MediaStore.Audio.Playlists.Members.getContentUri("external",playlistId);

            if(!newPlaylist) {
                contentResolver.delete(membersUri, null, null);
            }

//            ContentValues value = new ContentValues();
//            value.put(MediaStore.Audio.Playlists.DATE_MODIFIED,playlist.getDateModified().getTime());
//            contentResolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,value);

            ContentValues[] values = new ContentValues[playlist.size()];

            for (int i = 0; i < playlist.size(); i++){
                values[i] = new ContentValues(2);
                values[i].put(MediaStore.Audio.Playlists.Members.AUDIO_ID,playlist.getAudio(i).getId());
                values[i].put(MediaStore.Audio.Playlists.Members.PLAY_ORDER,i);
            }

            contentResolver.bulkInsert(membersUri,values);
        }
        catch (Exception | Error e){
            e.printStackTrace();
            Log.e(toString(),e.getMessage());
        }
        return null;
    }

}

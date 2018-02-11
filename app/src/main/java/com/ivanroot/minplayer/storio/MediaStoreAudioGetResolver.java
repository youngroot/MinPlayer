package com.ivanroot.minplayer.storio;

import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import com.ivanroot.minplayer.audio.Audio;
import com.pushtorefresh.storio.contentresolver.operations.get.DefaultGetResolver;

/**
 * Created by Ivan Root on 28.08.2017.
 */

public class MediaStoreAudioGetResolver extends DefaultGetResolver<Audio> {

    @NonNull
    @Override
    public Audio mapFromCursor(@NonNull Cursor cursor) {

        String id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
        String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
        String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
        String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
        String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

        Audio audio = new Audio(id,data,title,album,artist);
//        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
//        mmr.setDataSource(audio.getData());
//        audio.setGenre(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));

        return audio;

    }
}

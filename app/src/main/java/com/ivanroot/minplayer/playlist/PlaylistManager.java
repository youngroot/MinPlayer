package com.ivanroot.minplayer.playlist;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.storio.StorIOContentResolverFactory;
import com.ivanroot.minplayer.utils.Utils;
import com.pushtorefresh.storio3.contentresolver.StorIOContentResolver;
import com.pushtorefresh.storio3.contentresolver.queries.DeleteQuery;
import com.pushtorefresh.storio3.contentresolver.queries.Query;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by Ivan Root on 24.06.2017.
 */

public class PlaylistManager {

    public static final String ASC_SORT_ORDER = MediaStore.Audio.Media.TITLE + " ASC";
    public static final String ALL_TRACKS_PLAYLIST = "com.ivanroot.minplayer.all_tracks_playlist";
    public static final String IMAGE_DIR = "playlists_images";
    public static final String IMAGE_PATH = "playlist_image";

    private static final PlaylistManager ourInstance = new PlaylistManager();

    private PlaylistManager() {
    }

    public static synchronized PlaylistManager getInstance() {
        return ourInstance;
    }


    public Observable<Playlist> getPlaylistObservable(Context context, String playlistName) {


        if (playlistName.equals(ALL_TRACKS_PLAYLIST))
            return StorIOContentResolverFactory
                    .getAllAudioObservable(context, ASC_SORT_ORDER)
                    .map(list -> new Playlist(ALL_TRACKS_PLAYLIST).setAudioList(list));

        return StorIOContentResolverFactory.getPlaylistObservable(context, playlistName);
    }

    public Observable<List<PlaylistItem>> getPlaylistItemsObservable(Context context) {
        return StorIOContentResolverFactory.getPlaylistItemsObservable(context);
    }

    public synchronized void writePlaylist(Context context, Playlist playlist) {

        if (playlist == null) return;

        if (Objects.equals(playlist.getName(), ALL_TRACKS_PLAYLIST))
            //playlist.setAudioList(new ArrayList<>());
            return;

        StorIOContentResolverFactory.get(context)
                .put()
                .object(playlist)
                .prepare()
                .executeAsBlocking();

    }

    public synchronized void addToPlaylist(Context context, String playlistName, Audio audio) {
        Completable.create(emitter -> {
            ContentResolver contentResolver = context.getContentResolver();

            String[] projection = new String[]{MediaStore.Audio.Playlists._ID, MediaStore.Audio.Playlists.NAME};
            String selection = MediaStore.Audio.Playlists.NAME + " = ?";
            String[] selectionArgs = new String[]{playlistName};

            Cursor cursor = contentResolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);
            cursor.moveToFirst();

            long playlistId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists._ID));
            cursor.close();

            Uri membersUri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
            cursor = contentResolver.query(membersUri, null, null, null, null);
            int playlistSize = cursor.getCount();
            cursor.close();

            ContentValues contentValues = new ContentValues(2);
            contentValues.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audio.getId());
            contentValues.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, playlistSize);

            contentResolver.insert(membersUri, contentValues);

            emitter.onComplete();


        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwable -> {
                    throwable.printStackTrace();
                    Log.e(toString(), throwable.getMessage());
                })
                .subscribe();
    }

    public PlaylistItem getPlaylistItem(Context context, String playlistName) {
        if (playlistName.equals(ALL_TRACKS_PLAYLIST)) {
            PlaylistItem playlistItem = new PlaylistItem();
            playlistItem.setName(ALL_TRACKS_PLAYLIST);
            return playlistItem;
        }

        return StorIOContentResolverFactory.get(context)
                .get()
                .object(PlaylistItem.class)
                .withQuery(Query.builder()
                        .uri(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI)
                        .where(MediaStore.Audio.Playlists.NAME + " = ?")
                        .whereArgs(playlistName)
                        .build())
                .prepare()
                .executeAsBlocking();
    }

    public synchronized void removePlaylist(Context context, String name) {
        if (!Objects.equals(name, ALL_TRACKS_PLAYLIST)) {
            StorIOContentResolverFactory.get(context)
                    .delete()
                    .byQuery(DeleteQuery
                            .builder()
                            .uri(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI)
                            .where(MediaStore.Audio.Playlists.NAME + " = ?")
                            .whereArgs(name)
                            .build())
                    .prepare()
                    .executeAsBlocking();
        }
    }


    public String getTitleFromPlaylistName(Context context, String playlistName) {
        String title;
        switch (playlistName) {
            case ALL_TRACKS_PLAYLIST:
                title = context.getResources().getString(R.string.all_tracks_playlist);
                break;
            default:
                title = playlistName;
        }

        return title;
    }

    public synchronized void renamePlaylist(Context context, @NonNull String oldName, @NonNull String newName) {

        //TODO: Implement playlist renaming
    }


}

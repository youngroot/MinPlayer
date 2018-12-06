package com.ivanroot.minplayer.playlist;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.storio.contentresolver.StorIOContentResolverFactory;
import com.ivanroot.minplayer.storio.sqlite.StorIOSQLiteFactory;
import com.ivanroot.minplayer.storio.sqlite.playlist.PlaylistTable;
import com.pushtorefresh.storio3.contentresolver.queries.DeleteQuery;
import com.pushtorefresh.storio3.contentresolver.queries.Query;
import com.yandex.disk.rest.ResourcesArgs;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.http.UnauthorizedException;
import com.yandex.disk.rest.json.Resource;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by Ivan Root on 24.06.2017.
 */

public class PlaylistManager {
    public static final String ASC_SORT_ORDER = MediaStore.Audio.Media.TITLE + " ASC";
    public static final String ALL_TRACKS_PLAYLIST = "com.ivanroot.minplayer.all_tracks_playlist";
    public static final String DISK_ALL_TRACKS_PLAYLIST = "com.ivanroot.minplayer.disk_all_tracks_playlist";
    public static final String IMAGE_DIR = "playlists_images";
    public static final String IMAGE_PATH = "playlist_image";

    public static final long ALL_TRACKS_PLAYLIST_ID = 0;
    public static final long DISK_ALL_TRACKS_PLAYLIST_ID = 1;

    private static final int period = 10000;
    private static final int limit = 500;

    private static final PlaylistManager ourInstance = new PlaylistManager();

    private PlaylistManager() {
    }

    public static synchronized PlaylistManager getInstance() {
        return ourInstance;
    }


    public Observable<Playlist> getPlaylistObservable(@NonNull Context context, RestClient restClient, long playlistId) {
        if (Objects.equals(playlistId, DISK_ALL_TRACKS_PLAYLIST_ID))
            return getPlaylistCombinedObservable(getDiskAllTracksObservable(context, restClient)
                    .map(list -> {
                        Playlist playlist = new Playlist(DISK_ALL_TRACKS_PLAYLIST);
                        playlist.setAudioList(list);
                        playlist.setId(DISK_ALL_TRACKS_PLAYLIST_ID);
                        return playlist;
                    })
                    .distinctUntilChanged(), getPlaylistSQLiteObservable(context, playlistId));

        return getPlaylistObservable(context, playlistId);
    }

    public Observable<Playlist> getPlaylistObservable(@NonNull Context context, long playlistId) {
        Observable<Playlist> playlistObservable = Objects.equals(playlistId, ALL_TRACKS_PLAYLIST_ID) ?
                StorIOContentResolverFactory
                        .getAllAudioObservable(context, ASC_SORT_ORDER)
                        .map(list -> {
                            Playlist playlist = new Playlist(ALL_TRACKS_PLAYLIST);
                            playlist.setAudioList(list);
                            playlist.setId(ALL_TRACKS_PLAYLIST_ID);
                            return playlist;
                        }) :
                StorIOContentResolverFactory.getPlaylistObservable(context, playlistId);

        Observable<Playlist> dbPlaylistObservable = getPlaylistSQLiteObservable(context, playlistId);

        return getPlaylistCombinedObservable(playlistObservable, dbPlaylistObservable);
    }

    private Observable<Playlist> getPlaylistCombinedObservable(Observable<Playlist> playlistObservable, Observable<Playlist> dbPlaylistObservable) {
        return Observable.combineLatest(playlistObservable, dbPlaylistObservable, (playlist, dbPlaylist) -> {
            if (dbPlaylist.isShuffled())
                playlist.shuffle();

            playlist.setRepeatMode(dbPlaylist.getRepeatMode());
            playlist.checkAndSetAudio(dbPlaylist.getCurrentAudio());
            playlist.setImagePath(dbPlaylist.getImagePath());

            return playlist;
        });
    }

    public Observable<List<Audio>> getAllAudiosObservable(@NonNull Context context) {
        return StorIOContentResolverFactory.getAllAudioObservable(context, ASC_SORT_ORDER);
    }

    public Observable<List<PlaylistItem>> getPlaylistItemsObservable(Context context) {
        return StorIOContentResolverFactory.getPlaylistItemsObservable(context);
    }

    public synchronized void writePlaylist(@NonNull Context context, Playlist playlist) {
        if (playlist == null)
            return;

        if (!Objects.equals(playlist.getId(), ALL_TRACKS_PLAYLIST_ID) &&
                !Objects.equals(playlist.getId(), DISK_ALL_TRACKS_PLAYLIST_ID)) {

            StorIOContentResolverFactory.get(context)
                    .put()
                    .object(playlist)
                    .prepare()
                    .executeAsBlocking();
        } else {
            playlist.setAudioList(new ArrayList<>());
        }

        StorIOSQLiteFactory.get(context)
                .put()
                .object(playlist)
                .prepare()
                .executeAsBlocking();
    }

    public synchronized void addToPlaylist(@NonNull Context context, long playlistId, @NonNull Audio audio) {
        Completable.create(emitter -> {
            ContentResolver contentResolver = context.getContentResolver();

            Uri membersUri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
            Cursor cursor = contentResolver.query(membersUri, null, null, null, null);
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

    public synchronized void removePlaylist(@NonNull Context context, long playlistId) {
        StorIOContentResolverFactory.get(context)
                .delete()
                .byQuery(DeleteQuery.builder()
                        .uri(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI)
                        .where(MediaStore.Audio.Playlists._ID + " = ?")
                        .whereArgs(playlistId).build())
                .prepare()
                .executeAsBlocking();
    }

    public Observable<List<Audio>> getDiskAllTracksObservable(@NonNull Context context, @NonNull RestClient restClient) {
        return Observable.interval(0, period, TimeUnit.MILLISECONDS)
                .map(i -> {
                    List<Audio> audioList = new ArrayList<>();
                    if (restClient != null) {
                        try {
                            List<Resource> resources = restClient.getFlatResourceList(new ResourcesArgs.Builder()
                                    .setPath("/")
                                    .setLimit(limit)
                                    .setMediaType("audio")
                                    .build())
                                    .getItems();

                            for (Resource res : resources) {
                                Audio audio = StorIOContentResolverFactory.get(context)
                                        .get()
                                        .object(Audio.class)
                                        .withQuery(Query.builder()
                                                .uri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                                                .where(MediaStore.Audio.Media.TITLE + " = ?")
                                                .whereArgs(res.getName()).build()).prepare()
                                        .executeAsBlocking();

                                if (audio == null) {
                                    audio = new Audio();
                                    audio.setSize(res.getSize());
                                    audio.setTitle(res.getName());
                                    audio.setCloudData(res.getPath().getPath());
                                }
                                audio.setMd5Hash(res.getMd5());
                                audioList.add(audio);
                            }
                        } catch (UnauthorizedException ex) {
                        }
                    }
                    return audioList;
                }).subscribeOn(Schedulers.io());
    }

    private Observable<Playlist> getPlaylistSQLiteObservable(@NonNull Context context, long playlistId) {
        int count = 0;
        try {
            count = StorIOSQLiteFactory
                    .get(context)
                    .get()
                    .numberOfResults()
                    .withQuery(com.pushtorefresh.storio3.sqlite.queries.Query.builder()
                            .table(PlaylistTable.TABLE_NAME)
                            .where(PlaylistTable.Columns.PLAYLIST_ID + " = ?")
                            .whereArgs(playlistId).build())
                    .prepare()
                    .executeAsBlocking();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }

        if (count == 0) {
            Playlist playlist = new Playlist();
            playlist.setId(playlistId);

            StorIOSQLiteFactory.get(context)
                    .put()
                    .object(playlist)
                    .prepare()
                    .executeAsBlocking();
        }

        return StorIOSQLiteFactory.getPlaylistObservable(context, playlistId);
    }

    public synchronized void renamePlaylist(@NonNull Context context, long playlistId, @NonNull String newName) {
        ContentValues newNameValue = new ContentValues(1);
        newNameValue.put(MediaStore.Audio.Playlists.NAME, newName);

        StorIOContentResolverFactory.get(context)
                .lowLevel()
                .contentResolver()
                .update(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, newNameValue, MediaStore.Audio.Playlists._ID + "=" + playlistId, null);
    }
}

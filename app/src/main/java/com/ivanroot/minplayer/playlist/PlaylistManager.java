package com.ivanroot.minplayer.playlist;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.utils.Utils;
import com.ivanroot.minplayer.storio.PlaylistTable;
import com.ivanroot.minplayer.storio.StorIOMediaStoreFactory;
import com.ivanroot.minplayer.storio.StorIOPlaylistFactory;
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery;
import com.pushtorefresh.storio3.sqlite.queries.RawQuery;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Created by Ivan Root on 24.06.2017.
 */

public class PlaylistManager {

    private static final PlaylistManager ourInstance = new PlaylistManager();

    private PlaylistManager() {
    }

    public static synchronized PlaylistManager getInstance() {
        return ourInstance;
    }


    public Observable<Playlist> getPlaylistObservable(Context context, String playlistName) {

        Observable playlistObservable = StorIOPlaylistFactory.getPlaylistObservable(context, playlistName);
        Observable audioObservable = StorIOMediaStoreFactory.getAllAudioObservable(context, PlaylistTable.sortOrder);

        if (playlistName.equals(PlaylistTable.ALL_TRACKS_PLAYLIST))
            return Observable.combineLatest(playlistObservable, audioObservable,
                    (playlist, audios) -> ((Playlist) playlist).setAudioList((List<Audio>) audios));

        else
            return Observable.combineLatest(playlistObservable, audioObservable,
                    (playlist, audios) -> removeNonexistentAudios((Playlist) playlist, (List<Audio>) audios));
    }

    public Observable<List<Playlist>> getAllPlaylistsObservable(Context context){
        return StorIOPlaylistFactory.getAllPlaylistsObservable(context);
    }

    public Observable<List<String>> getPlaylistNamesObservable(Context context) {

        String query = "SELECT * FROM "
                + PlaylistTable.TABLE_PLAYLISTS
                + " WHERE "
                + PlaylistTable.ROW_PLAYLIST_NAME
                + " != '"
                + PlaylistTable.ALL_TRACKS_PLAYLIST
                + "'";

        return StorIOPlaylistFactory.get(context)
                .get()
                .cursor()
                .withQuery(RawQuery.builder()
                        .query(query)
                        .build())
                .prepare()
                .asRxFlowable(BackpressureStrategy.LATEST)
                .toObservable()
                .map(cursor -> {
                    List<String> playlistNames = new ArrayList<>();
                    while (cursor.moveToNext())
                        playlistNames.add(cursor.getString(cursor.getColumnIndex(PlaylistTable.ROW_PLAYLIST_NAME)));
                    return playlistNames;
                });
    }

    public synchronized void writePlaylist(Context context, Playlist playlist) {

        if (playlist == null) return;

        if (Objects.equals(playlist.getName(), PlaylistTable.ALL_TRACKS_PLAYLIST)) {
            playlist.setAudioList(new ArrayList<>());
        }

        StorIOPlaylistFactory.get(context)
                .put()
                .object(playlist)
                .prepare()
                .executeAsBlocking();

    }

    public synchronized void removePlaylist(Context context, String name) {
        if (!Objects.equals(name, PlaylistTable.ALL_TRACKS_PLAYLIST)) {
            StorIOPlaylistFactory.get(context)
                    .delete()
                    .byQuery(DeleteQuery
                            .builder()
                            .table(PlaylistTable.TABLE_PLAYLISTS)
                            .where(PlaylistTable.ROW_PLAYLIST_NAME + " = ?")
                            .whereArgs(name)
                            .build())
                    .prepare()
                    .executeAsBlocking();
        }
    }


    public String getTitleFromPlaylistName(String playlistName, Context context) {
        if (playlistName != PlaylistTable.ALL_TRACKS_PLAYLIST)
            return playlistName;
        else
            return context.getResources().getString(R.string.all_tracks_playlist);
    }

    private Playlist removeNonexistentAudios(Playlist playlist, List<Audio> audios) {

        List<Audio> removeList = new ArrayList<>();
        for (Audio audio : playlist.getAudioList())
            if (!audios.contains(audio))
                removeList.add(audio);
        for (Audio audio : removeList) {
            if (playlist.getCurrentAudio().equals(audio))
                playlist.cleanCurrAudio();
            playlist.deleteAudio(audio);
        }

        return playlist;
    }

    public void renamePlaylist(String old_name, String new_name) {
        //TODO: Implement Playlist renaming
    }

}

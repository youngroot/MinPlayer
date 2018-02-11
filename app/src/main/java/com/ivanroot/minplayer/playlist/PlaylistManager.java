package com.ivanroot.minplayer.playlist;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.utils.Utils;
import com.ivanroot.minplayer.storio.PlaylistTable;
import com.ivanroot.minplayer.storio.StorIOMediaStoreFactory;
import com.ivanroot.minplayer.storio.StorIOPlaylistFactory;
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery;
import com.pushtorefresh.storio.sqlite.queries.RawQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.BiPredicate;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Created by Ivan Root on 24.06.2017.
 */

public class PlaylistManager {

    private final BehaviorSubject<List<Playlist>> playlistSubject = BehaviorSubject.create();
    private  Observable<List<Playlist>> playlistObservable;
    private static final PlaylistManager ourInstance = new PlaylistManager();
    private PlaylistManager() {}

    public static synchronized PlaylistManager getInstance(){
        return ourInstance;
    }

    public  BehaviorSubject<List<Playlist>> getPlaylistSubject(Context context) {

        if (playlistObservable != null)
            return playlistSubject;

        playlistObservable = Observable.combineLatest(

                StorIOPlaylistFactory.createPlaylistObservable(context),
                StorIOMediaStoreFactory.createPlaylistObservable(context, PlaylistTable.sortOrder),
                (playlist1, playlist2)  -> {
                    List<Playlist> playlistList = new ArrayList<>();

                    if (!playlist2.contains(playlist2.getCurrentAudio()))
                        playlist2.cleanCurrAudio();

                    if (playlist1.getName().equals(PlaylistTable.ALL_TRACKS_PLAYLIST)) {
                        playlistList.add(playlist2);
                    } else {
                        for (int i = 0; i < playlist1.size(); i++) {

                            Audio audio = playlist1.getAudio(i);

                            if (!playlist2.contains(audio)) {
                                playlist1.deleteAudio(audio);

                                if (!playlist1.contains(playlist1.getCurrentAudio()))
                                    playlist1.cleanCurrAudio();

                                Log.i("PlaylistManager", "playlist: " + playlist1.getName() + " audio: " + audio.getTitle() + "will be deleted!");
                                writePlaylist(context, playlist1);
                            }
                        }

                        playlistList.add(playlist1);
                        playlistList.add(playlist2);
                    }
                    return playlistList;
                }
        );
        playlistObservable.subscribe(playlistSubject);

        return playlistSubject;
    }

    public Observable<Cursor> getPlaylistNamesObservable(Context context) {

        String query = "SELECT * FROM "
                + PlaylistTable.TABLE_PLAYLISTS
                + " WHERE "
                + PlaylistTable.ROW_PLAYLIST_NAME
                + " != '"
                + PlaylistTable.ALL_TRACKS_PLAYLIST
                + "'";

        return Observable.interval(100, TimeUnit.MILLISECONDS)
                .flatMap(i -> Utils.v2(StorIOPlaylistFactory.get(context)
                        .get()
                        .cursor()
                        .withQuery(RawQuery.builder()
                                .query(query)
                                .build())
                        .prepare()
                        .asRxObservable()))
                .distinctUntilChanged(cursorSizeComparator());
    }

    @NonNull
    private BiPredicate<Cursor, Cursor> cursorSizeComparator() {
        return (cursor1, cursor2) -> cursor1.getCount() == cursor2.getCount();
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
                .asRxSingle()
                .toBlocking()
                .value();

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
                    .asRxSingle()
                    .toBlocking()
                    .value();
        }
    }


    public String getTitleFromPlaylistName(String playlistName, Context context){
        if(playlistName != PlaylistTable.ALL_TRACKS_PLAYLIST)
            return playlistName;
        else
            return context.getResources().getString(R.string.all_tracks_playlist);
    }

    public void renamePlaylist(String old_name, String new_name) {

    }

}

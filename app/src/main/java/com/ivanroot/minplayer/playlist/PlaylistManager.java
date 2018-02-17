package com.ivanroot.minplayer.playlist;

import android.content.Context;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.storio.PlaylistTable;
import com.ivanroot.minplayer.storio.StorIOMediaStoreFactory;
import com.ivanroot.minplayer.storio.StorIOPlaylistFactory;
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;


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
        Observable audioObservable = StorIOMediaStoreFactory.getAllAudioObservable(context, PlaylistTable.ASC_SORT_ORDER);

        if (playlistName.equals(PlaylistTable.ALL_TRACKS_PLAYLIST))
            return Observable.combineLatest(playlistObservable, audioObservable,
                    (playlist, audios) -> ((Playlist) playlist).setAudioList((List<Audio>) audios));

        else
            return Observable.combineLatest(playlistObservable, audioObservable,
                    (playlist, audios) -> removeNonexistentAudios((Playlist) playlist, (List<Audio>) audios));
    }

    public Observable<List<PlaylistItem>> getPlaylistItemsObservable(Context context){
        return StorIOPlaylistFactory.getPlaylistItemsObservable(context);
    }

    public synchronized void writePlaylist(Context context, Playlist playlist) {

        if (playlist == null) return;

        if (Objects.equals(playlist.getName(), PlaylistTable.ALL_TRACKS_PLAYLIST))
            playlist.setAudioList(new ArrayList<>());

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
                            .table(PlaylistTable.TABLE)
                            .where(PlaylistTable.PLAYLIST_NAME + " = ?")
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

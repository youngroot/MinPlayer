package com.ivanroot.minplayer.playlist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.storio.PlaylistTable;
import com.ivanroot.minplayer.storio.StorIOContentResolverFactory;
import com.ivanroot.minplayer.storio.StorIOFactory;
import com.ivanroot.minplayer.utils.Utils;
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery;
import com.pushtorefresh.storio3.sqlite.queries.Query;
import com.pushtorefresh.storio3.sqlite.queries.RawQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by Ivan Root on 24.06.2017.
 */

public class PlaylistManager {

    private static final PlaylistManager ourInstance = new PlaylistManager();

    private PlaylistManager() {}

    public static synchronized PlaylistManager getInstance() {
        return ourInstance;
    }


    public Observable<Playlist> getPlaylistObservable(Context context, String playlistName) {

        Observable playlistObservable = StorIOFactory.getPlaylistObservable(context, playlistName);
        Observable audioObservable = StorIOContentResolverFactory.getAllAudioObservable(context, PlaylistTable.ASC_SORT_ORDER);

        if (playlistName.equals(PlaylistTable.Playlist.ALL_TRACKS_PLAYLIST))
            return Observable.combineLatest(playlistObservable, audioObservable,
                    (playlist, audios) -> ((Playlist) playlist).setAudioList((List<Audio>) audios));

        else
            return Observable.combineLatest(playlistObservable, audioObservable,
                    (playlist, audios) -> removeNonexistentAudios((Playlist) playlist, (List<Audio>) audios));
    }

    public Observable<List<PlaylistItem>> getPlaylistItemsObservable(Context context){
        return StorIOFactory.getPlaylistItemsObservable(context);
    }

    public synchronized void writePlaylist(Context context, Playlist playlist) {

        if (playlist == null) return;

        if (Objects.equals(playlist.getName(), PlaylistTable.Playlist.ALL_TRACKS_PLAYLIST))
            playlist.setAudioList(new ArrayList<>());

        Completable.create(e -> {
            List<Bitmap> bitmaps = new ArrayList<>();
            for(Audio audio : playlist.getAudioList()){
                if(audio.getAlbumArtPath() != null && bitmaps.size() < 4){
                    Bitmap bitmap = BitmapFactory.decodeFile(audio.getAlbumArtPath());
                    bitmaps.add(bitmap);
                }
            }
            Bitmap bitmap = Utils.combineFourBitmapsIntoOne(bitmaps);
            String path = Utils.saveImage(context,bitmap,PlaylistTable.Playlist.IMAGE_DIR,playlist.getName());
            playlist.setImagePath(path);
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> StorIOFactory.get(context)
                        .put()
                        .object(playlist)
                        .prepare()
                        .executeAsBlocking());

    }

    public PlaylistItem getPlaylistItem(Context context, String playlistName){
        return StorIOFactory.get(context)
                .get()
                .object(PlaylistItem.class)
                .withQuery(Query.builder()
                        .table(PlaylistTable.TABLE)
                        .where(PlaylistTable.Playlist.NAME + " = ?")
                        .whereArgs(playlistName)
                        .build())
                .prepare()
                .executeAsBlocking();
    }

    public synchronized void removePlaylist(Context context, String name) {
        if (!Objects.equals(name, PlaylistTable.Playlist.ALL_TRACKS_PLAYLIST)) {
            StorIOFactory.get(context)
                    .delete()
                    .byQuery(DeleteQuery
                            .builder()
                            .table(PlaylistTable.TABLE)
                            .where(PlaylistTable.Playlist.NAME + " = ?")
                            .whereArgs(name)
                            .build())
                    .prepare()
                    .executeAsBlocking();
        }
    }


    public String getTitleFromPlaylistName(Context context, String playlistName) {
        String title;
        switch (playlistName){
            case PlaylistTable.Playlist.ALL_TRACKS_PLAYLIST:
                title = context.getResources().getString(R.string.all_tracks_playlist);
                break;
            default:
                title = playlistName;
        }

        return title;
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

    public synchronized void renamePlaylist(Context context, @NonNull String oldName, @NonNull String newName) {

        StorIOFactory.get(context)
                .executeSQL()
                .withQuery(RawQuery.builder()
                        .query(PlaylistTable.renamePlaylistQuery(oldName,newName))
                        .build())
                .prepare()
                .executeAsBlocking();
    }


}

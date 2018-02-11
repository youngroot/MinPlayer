package com.ivanroot.minplayer.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import com.ivanroot.minplayer.utils.AlbumArtLoader;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.playlist.Playlist;
import com.ivanroot.minplayer.playlist.PlaylistManager;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;


/**
 * Created by Ivan Root on 02.07.2017.
 */

public class PlaylistAdapter extends BaseAdapter {

    private Activity activity;
    private Playlist playlist;
    private AlbumArtLoader albumArtLoader;
    private TextView title;
    private TextView album;
    private TextView artist;
    private ImageView albumArt;
    private ImageButton extraBtn;
    private Animation fadeIn;
    private Disposable disposable;
    private PlaylistManager playlistManager = PlaylistManager.getInstance();
    private boolean isScrolling = false;


    public PlaylistAdapter(Activity activity,String playlistName){

        this.activity = activity;
        albumArtLoader = new AlbumArtLoader(wasAlbumArt -> {});
        fadeIn = AnimationUtils.loadAnimation(activity, R.anim.fade_in);
        fadeIn.setDuration(500);

        disposable = playlistManager.getPlaylistSubject(activity)
                .flatMap(Observable::fromIterable)
                .filter(tempPlaylist -> tempPlaylist.getName().equals(playlistName))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setPlaylist);

    }

    private void setPlaylist(Playlist playlist){
        this.playlist = playlist;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if(playlist != null)
            return playlist.size();
        else return 0;
    }

    @Override
    public Audio getItem(int position) {
        return playlist.getAudio(position);
    }

    @Override
    public long getItemId(int position) {
        return (long) position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        if(view == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            view = inflater.inflate(R.layout.audio_item, null);
        }

        title = (TextView)view.findViewById(R.id.songTitle);
        album = (TextView)view.findViewById(R.id.songAlbum);
        artist = (TextView)view.findViewById(R.id.songArtist);
        albumArt = (ImageView)view.findViewById(R.id.songAlbumArt);
        //extraBtn = (ImageButton)view.findViewById(R.id.extraBtn);

        Audio tempAudio = getItem(position);
        title.setText(tempAudio.getTitle());
        album.setText(tempAudio.getAlbum());
        artist.setText(tempAudio.getArtist());
        albumArt.setImageResource(R.drawable.default_album_art);
        albumArtLoader.setAlbumArt(tempAudio.getData(),albumArt);

        return view;
    }

    public void dispose(){
        if(disposable != null)
            disposable.dispose();
    }

}

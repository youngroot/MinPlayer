package com.ivanroot.minplayer.fragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hwangjr.rxbus.Bus;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.adapter.PlaylistAdapter;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.player.RxBus;

import static com.ivanroot.minplayer.player.PlayerActionsEvents.ACTION_PLAY_AUDIO;
import static com.ivanroot.minplayer.player.PlayerActionsEvents.ACTION_SET_PLAYLIST;

/**
 * Created by Ivan Root on 02.07.2017.
 */

public class PlaylistFragment extends Fragment {

    public static final String NAME = "PlaylistFragment";
    private ImageView playlistImage;
    private TextView playlistTitle;
    private ListView audioListView;
    private String playlistName;
    private PlaylistAdapter adapter;
    private Bitmap image;
    private Bus rxBus = RxBus.getInstance();

    public PlaylistFragment(String playlistName, Bitmap image){
        this.playlistName = playlistName;
        this.image = image;
    }

    public static PlaylistFragment newInstance(String playlistName , Bitmap image){
        PlaylistFragment playlistFragment = new PlaylistFragment(playlistName,image);
        return playlistFragment;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        if(playlistName != null){
            outState.putString("name",playlistName);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            playlistName = savedInstanceState.getString("name");
        }
        adapter = new PlaylistAdapter(getActivity(),playlistName);
        rxBus.register(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.playlist_fragment_layout,container,false);
        playlistImage = (ImageView)view.findViewById(R.id.playlistImage);
        playlistTitle = (TextView) view.findViewById(R.id.playlistTitle);
        audioListView = (ListView) view.findViewById(R.id.audioList);
        playlistImage.setImageBitmap(image);
        playlistTitle.setText(playlistName);
        audioListView.setOnItemClickListener((parent, view1, position, id) -> {

            Audio audio = adapter.getItem(position);
            rxBus.post(ACTION_SET_PLAYLIST,playlistName);
            rxBus.post(ACTION_PLAY_AUDIO,audio);
        });
        audioListView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        if(adapter != null)
            adapter.dispose();
        rxBus.unregister(this);
        super.onDestroy();
    }

    @Override
    public String toString() {
        return NAME;
    }
}

package com.ivanroot.minplayer.fragment;

import android.app.ListFragment;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.ListView;

import com.hwangjr.rxbus.Bus;
import com.ivanroot.minplayer.adapter.PlaylistAdapter;
import com.ivanroot.minplayer.player.RxBus;

import static com.ivanroot.minplayer.player.PlayerActionsEvents.*;

/**
 * Created by Ivan Root on 02.06.2017.
 */

public class AudioListFragment extends ListFragment {

    public static final String NAME = "AudioListFragment";
    private static final String LIST_INSTANCE_STATE = "list_istance_state";
    private String playlistName;
    private PlaylistAdapter adapter;
    private Parcelable listInstanceState;
    private int position = 0;
    private Bus rxBus = RxBus.getInstance();

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(LIST_INSTANCE_STATE,getListView().onSaveInstanceState());
        position = getListView().getFirstVisiblePosition();
        outState.putInt("list_position",position);
        if(playlistName != null) {
            outState.putString("playlist_name", playlistName);
        }
        super.onSaveInstanceState(outState);
    }


    public static AudioListFragment newInstance(String playlistName){

        AudioListFragment audioListFragment = new AudioListFragment();
        audioListFragment.playlistName = playlistName;
        return audioListFragment;

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null)
            playlistName = savedInstanceState.getString("playlist_name");
        adapter = new PlaylistAdapter(getActivity(),playlistName);
        rxBus.register(this);
        setListAdapter(adapter);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(savedInstanceState != null) {
            listInstanceState = savedInstanceState.getParcelable(LIST_INSTANCE_STATE);
            position = savedInstanceState.getInt("list_position");
            getListView().onRestoreInstanceState(listInstanceState);
            getListView().smoothScrollToPosition(position);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        rxBus.post(ACTION_SET_PLAYLIST,playlistName);
        rxBus.post(ACTION_PLAY_AUDIO,adapter.getItem(position));
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

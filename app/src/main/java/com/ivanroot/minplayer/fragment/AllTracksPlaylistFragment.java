package com.ivanroot.minplayer.fragment;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.playlist.PlaylistManager;

public class AllTracksPlaylistFragment extends PlaylistFragment {
    public static final String NAME = "AllTracksPlaylistFragment";

    public AllTracksPlaylistFragment(){
        playlistId = PlaylistManager.ALL_TRACKS_PLAYLIST_ID;
    }

    @Override
    public void setPlaylistId(long playlistId){}

    @Override
    public String getActionBarTitle() {
        return getResources().getString(R.string.all_tracks_playlist);
    }
}


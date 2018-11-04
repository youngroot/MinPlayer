package com.ivanroot.minplayer.fragment;

import com.ivanroot.minplayer.playlist.PlaylistManager;

public class AllTracksPlaylistFragment extends PlaylistFragment {
    public static final String NAME = "AllTracksPlaylistFragment";

    public AllTracksPlaylistFragment(){
        playlistName = PlaylistManager.ALL_TRACKS_PLAYLIST;
    }

    @Override
    public void setPlaylistName(String playlistName){}
}


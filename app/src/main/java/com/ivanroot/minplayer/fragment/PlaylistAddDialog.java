package com.ivanroot.minplayer.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.adapter.PlaylistAdapter;
import com.ivanroot.minplayer.playlist.Playlist;
import com.ivanroot.minplayer.playlist.PlaylistManager;
import com.ivanroot.minplayer.storio.PlaylistTable;

/**
 * Created by Ivan Root on 02.07.2017.
 */

public class PlaylistAddDialog extends DialogFragment {

    private Playlist playlist;
    private Activity activity;
    private PlaylistAdapter adapter;
    private PlaylistManager playlistManager = PlaylistManager.getInstance();

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
        this.activity = activity;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_playlist_dialog, null);

        playlist = new Playlist();


        ListView audioListView = (ListView) dialogView.findViewById(R.id.audioList);
        final EditText playlistName = (EditText) dialogView.findViewById(R.id.playlistName);
        Button cancelBtn = (Button) dialogView.findViewById(R.id.cancelBtn);
        Button okBtn = (Button) dialogView.findViewById(R.id.okBtn);
        adapter = new PlaylistAdapter(getActivity(),PlaylistTable.ALL_TRACKS_PLAYLIST);
        audioListView.setAdapter(adapter);
        audioListView.setOnItemClickListener((parent, view, position, id) -> playlist.addAudio(adapter.getItem(position)));

        cancelBtn.setOnClickListener(v -> dismiss());

        okBtn.setOnClickListener(v -> {
            String name = playlistName.getText().toString();
            if(!name.equals("")) {
                playlist.setName(name);
                playlist.setBitmapImage(BitmapFactory.decodeResource(getResources(),R.drawable.music_big));
                playlist.setDate("");
                playlist.setTime("");
                playlistManager.writePlaylist(activity, playlist);
                dismiss();
            }
            else{
                Toast.makeText(activity,getResources().getString(R.string.playlist_name_cannot_be_empty),Toast.LENGTH_SHORT)
                        .show();
            }
        });

        builder.setView(dialogView);
        return builder.create();
    }

}

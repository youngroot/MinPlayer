package com.ivanroot.minplayer.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.playlist.Playlist;
import com.ivanroot.minplayer.playlist.PlaylistManager;
import com.ivanroot.minplayer.utils.Pair;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Created by Ivan Root on 02.07.2017.
 */

public class PlaylistAddDialog extends DialogFragment {
    private Playlist playlist;
    private AudioSelectorAdapter adapter;
    private PlaylistManager playlistManager = PlaylistManager.getInstance();


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_playlist_dialog, null);
        playlist = new Playlist();
        ListView audioListView = (ListView) dialogView.findViewById(R.id.audio_list);
        final EditText playlistName = (EditText) dialogView.findViewById(R.id.playlist_name);
        Button cancelBtn = (Button) dialogView.findViewById(R.id.cancel_btn);
        Button okBtn = (Button) dialogView.findViewById(R.id.ok_btn);
        adapter = new AudioSelectorAdapter(getActivity());

        audioListView.setAdapter(adapter);
        cancelBtn.setOnClickListener(v -> dismiss());
        okBtn.setOnClickListener(v -> {
            if(adapter.getCount() == 0){
                Toast.makeText(getActivity(), getResources().getString(R.string.playlist_cannot_be_empty), Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            String name = playlistName.getText().toString();

            if (!name.equals("")) {
                playlist.setAudioList(adapter.getSelectedAudiosList());
                playlist.setName(name);
                playlist.setDateAdded(Calendar.getInstance().getTime());
                playlist.setDateModified(Calendar.getInstance().getTime());
                playlistManager.writePlaylist(getActivity(), playlist);
                dismiss();
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.playlist_name_cannot_be_empty), Toast.LENGTH_SHORT)
                        .show();
            }
        });

        builder.setView(dialogView);
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        adapter.dispose();
        super.onDismiss(dialog);
    }

    public static class AudioSelectorAdapter extends BaseAdapter {

        private Activity activity;
        private List<Pair<Audio,Boolean>> audioList;
        private List<Audio> selectedAudioList;
        private TextView title;
        private TextView album;
        private TextView artist;
        private CheckBox audioCheckBox;
        private Animation fadeIn;
        private Disposable disposable;
        private PlaylistManager playlistManager = PlaylistManager.getInstance();


        public AudioSelectorAdapter(Activity activity) {

            this.activity = activity;
            fadeIn = AnimationUtils.loadAnimation(activity, R.anim.fade_in);
            fadeIn.setDuration(500);
            disposable = playlistManager.getAllAudiosObservable(activity)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::setAudioList);

        }

        private void setAudioList(List<Audio> audioList) {

            this.audioList = new ArrayList<>();
            selectedAudioList = new ArrayList<>();
            for(Audio audio : audioList){
                this.audioList.add(new Pair<>(audio, false));
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if (audioList != null)
                return audioList.size();
            else return 0;
        }

        @Override
        public Pair<Audio,Boolean> getItem(int position) {
            return audioList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return (long) position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {

            if (view == null) {
                LayoutInflater inflater = activity.getLayoutInflater();
                view = inflater.inflate(R.layout.audio_item_add_playtlist_dialog, null);
            }

            title = (TextView) view.findViewById(R.id.song_title);
            album = (TextView) view.findViewById(R.id.song_album);
            artist = (TextView) view.findViewById(R.id.song_artist);
            audioCheckBox = (CheckBox) view.findViewById(R.id.audio_check_box);

            Audio tempAudio = getItem(position).first;
            title.setText(tempAudio.getTitle());
            album.setText(tempAudio.getAlbum());
            artist.setText(tempAudio.getArtist());


            audioCheckBox.setOnCheckedChangeListener(null);
            audioCheckBox.setChecked(getItem(position).second);
            audioCheckBox.setOnCheckedChangeListener((compoundButton, isChecked) -> {

                Pair<Audio,Boolean> pair = getItem(position);
                pair.second = isChecked;
                if(isChecked){
                    if(!selectedAudioList.contains(pair.first))
                    selectedAudioList.add(pair.first);
                }else {
                    selectedAudioList.remove(pair.first);
                }

            });

            return view;
        }

        public List<Audio> getSelectedAudiosList() {
           return selectedAudioList;
        }

        public void dispose() {
            if (disposable != null)
                disposable.dispose();
        }

    }
}

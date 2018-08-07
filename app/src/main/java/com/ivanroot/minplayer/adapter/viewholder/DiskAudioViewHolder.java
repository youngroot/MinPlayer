package com.ivanroot.minplayer.adapter.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.disk.AudioStatus;

public class DiskAudioViewHolder extends AudioViewHolder {

    private TextView title;
    private TextView album;
    private TextView artist;
    private ImageView albumArt;
    private ProgressBar loadProgress;

    public DiskAudioViewHolder(View itemView) {
        super(itemView);
        moreBtn = (ImageButton)itemView.findViewById(R.id.downloadBtn);
        loadProgress = (ProgressBar)itemView.findViewById(R.id.loadProgress);
    }

    public void representItem(Context context, Audio audio, int status){
        if(audio.getData() != null && status == AudioStatus.STATUS_AUDIO_DOWNLOADED) {
            super.representItem(context, audio);
            loadProgress.setVisibility(View.INVISIBLE);
            moreBtn.setVisibility(View.VISIBLE);
            moreBtn.setImageResource(R.drawable.ic_done);
        } else if(status == AudioStatus.STATUS_AUDIO_DOWNLOADING){
            moreBtn.setVisibility(View.INVISIBLE);
            loadProgress.setVisibility(View.VISIBLE);
        } else {
            loadProgress.setVisibility(View.INVISIBLE);
            moreBtn.setVisibility(View.VISIBLE);
            moreBtn.setImageResource(R.drawable.ic_download);
            super.representItem(context, audio);
        }
    }
}

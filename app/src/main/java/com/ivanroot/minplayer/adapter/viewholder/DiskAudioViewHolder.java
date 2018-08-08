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
    private ProgressBar loadProgress;

    public DiskAudioViewHolder(View itemView) {
        super(itemView);
        moreBtn = (ImageButton) itemView.findViewById(R.id.downloadBtn);
        loadProgress = (ProgressBar) itemView.findViewById(R.id.loadProgress);
    }

    public void representItem(Context context, Audio audio, String status) {
        super.representItem(context, audio);
        switch (status) {
            case AudioStatus.STATUS_AUDIO_ONLY_ONLINE:
                loadProgress.setVisibility(View.INVISIBLE);
                moreBtn.setVisibility(View.VISIBLE);
                moreBtn.setImageResource(R.drawable.ic_download);
                break;

            case AudioStatus.STATUS_AUDIO_PREPARING:
                moreBtn.setVisibility(View.INVISIBLE);
                loadProgress.setVisibility(View.VISIBLE);
                break;

            case AudioStatus.STATUS_AUDIO_DOWNLOADED:
                super.representItem(context, audio);
                loadProgress.setVisibility(View.INVISIBLE);
                moreBtn.setVisibility(View.VISIBLE);
                moreBtn.setImageResource(R.drawable.ic_done);
                break;
        }

    }
}

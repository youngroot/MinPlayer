package com.ivanroot.minplayer.adapter.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.disk.constants.AudioStatus;

public class DiskAudioViewHolder extends AudioViewHolder {
    private TextView infoTextView;
    private ProgressBar loadProgress;

    public DiskAudioViewHolder(View itemView) {
        super(itemView);
        infoTextView = (TextView)itemView.findViewById(R.id.big_info_text);
        moreBtn = (ImageButton) itemView.findViewById(R.id.download_btn);
        loadProgress = (ProgressBar) itemView.findViewById(R.id.load_progress);
    }

    public void representItem(Context context, Audio audio, String status) {
        //super.representItem(context, audio);
        infoTextView.setText(audio.getTitle());

        switch (status) {
            case AudioStatus.STATUS_AUDIO_ONLY_ONLINE:
                loadProgress.setVisibility(View.INVISIBLE);
                moreBtn.setVisibility(View.VISIBLE);
                moreBtn.setImageResource(R.drawable.ic_cloud_download);
                break;

            case AudioStatus.STATUS_AUDIO_DOWNLOAD_PREPARING:
                moreBtn.setVisibility(View.INVISIBLE);
                loadProgress.setVisibility(View.VISIBLE);
                break;

            case AudioStatus.STATUS_AUDIO_DOWNLOADED:
                //super.representItem(context, audio);
                loadProgress.setVisibility(View.INVISIBLE);
                moreBtn.setVisibility(View.INVISIBLE);
                moreBtn.setImageResource(R.drawable.ic_done);
                break;

            case AudioStatus.STATUS_AUDIO_DOWNLOAD_CANCELED:
                loadProgress.setVisibility(View.INVISIBLE);
                moreBtn.setVisibility(View.VISIBLE);
                moreBtn.setImageResource(R.drawable.ic_cloud_download);
                break;

        }

    }
}

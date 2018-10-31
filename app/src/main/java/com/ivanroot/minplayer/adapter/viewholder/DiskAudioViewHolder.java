package com.ivanroot.minplayer.adapter.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.disk.AudioDownloadService;
import com.ivanroot.minplayer.disk.AudioStatus;
import com.ivanroot.minplayer.utils.Pair;

import static com.ivanroot.minplayer.disk.AudioDownloadService.DownloadingAudioBundle;

public class DiskAudioViewHolder extends AudioViewHolder {
    private TextView infoTextView;
    private ProgressBar loadProgress;

    public DiskAudioViewHolder(View itemView) {
        super(itemView);
        infoTextView = (TextView)itemView.findViewById(R.id.bigInfoText);
        moreBtn = (ImageButton) itemView.findViewById(R.id.downloadBtn);
        loadProgress = (ProgressBar) itemView.findViewById(R.id.loadProgress);
    }

    public void representItem(Context context, Pair<String, DownloadingAudioBundle> status) {
        //super.representItem(context, audio);
        infoTextView.setText(status.second.getTaskAudio().getTitle());

        switch (status.first) {
            case AudioStatus.STATUS_AUDIO_ONLY_ONLINE:
                loadProgress.setVisibility(View.INVISIBLE);
                moreBtn.setVisibility(View.VISIBLE);
                moreBtn.setImageResource(R.drawable.ic_cloud_download);
                break;

            case AudioStatus.STATUS_AUDIO_PREPARING:
                moreBtn.setVisibility(View.INVISIBLE);
                loadProgress.setVisibility(View.VISIBLE);
                break;

            case AudioStatus.STATUS_AUDIO_DOWNLOADED:
                //super.representItem(context, audio);
                loadProgress.setVisibility(View.INVISIBLE);
                moreBtn.setVisibility(View.INVISIBLE);
                moreBtn.setImageResource(R.drawable.ic_done);
                break;

            case AudioStatus.STATUS_AUDIO_CANCELED:
                loadProgress.setVisibility(View.INVISIBLE);
                moreBtn.setVisibility(View.VISIBLE);
                moreBtn.setImageResource(R.drawable.ic_cloud_download);
                break;

        }

    }
}

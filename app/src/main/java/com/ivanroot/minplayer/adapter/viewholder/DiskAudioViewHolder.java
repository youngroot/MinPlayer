package com.ivanroot.minplayer.adapter.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.disk.AudioStatus;
import com.ivanroot.minplayer.utils.Pair;
import com.ivanroot.minplayer.utils.Utils;

public class DiskAudioViewHolder extends AudioViewHolder {
    private FrameLayout loadProgressHolder;
    private ProgressBar loadProgress;
    private ProgressBar circleLoadProgress;

    public DiskAudioViewHolder(View itemView) {
        super(itemView);
        moreBtn = (ImageButton) itemView.findViewById(R.id.downloadBtn);
        loadProgressHolder = (FrameLayout)itemView.findViewById(R.id.loadProgressHolder);
        loadProgress = (ProgressBar) itemView.findViewById(R.id.loadProgress);
        circleLoadProgress = (ProgressBar) itemView.findViewById(R.id.circleProgress);
    }

    public void representItem(Context context, Audio audio, Pair<String, Pair<Long, Long>> status) {
        super.representItem(context, audio);
        switch (status.first) {
            case AudioStatus.STATUS_AUDIO_ONLY_ONLINE:
                loadProgress.setVisibility(View.INVISIBLE);
                loadProgressHolder.setVisibility(View.INVISIBLE);
                moreBtn.setVisibility(View.VISIBLE);
                moreBtn.setImageResource(R.drawable.ic_cloud_download);
                break;

            case AudioStatus.STATUS_AUDIO_PREPARING:
                moreBtn.setVisibility(View.INVISIBLE);
                loadProgressHolder.setVisibility(View.INVISIBLE);
                loadProgress.setVisibility(View.VISIBLE);
                break;

//            case AudioStatus.STATUS_AUDIO_DOWNLOADING:
//                moreBtn.setVisibility(View.INVISIBLE);
//                loadProgress.setVisibility(View.INVISIBLE);
//                loadProgressHolder.setVisibility(View.VISIBLE);
//                circleLoadProgress.setProgress(Utils.getLoadedPercentage(status.second.first, status.second.second));

            case AudioStatus.STATUS_AUDIO_DOWNLOADED:
                super.representItem(context, audio);
                loadProgressHolder.setVisibility(View.INVISIBLE);
                loadProgress.setVisibility(View.INVISIBLE);
                moreBtn.setVisibility(View.VISIBLE);
                moreBtn.setImageResource(R.drawable.ic_done);
                break;
        }

    }
}

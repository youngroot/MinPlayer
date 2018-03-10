package com.ivanroot.minplayer.adapter.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.playlist.PlaylistItem;
import com.ivanroot.minplayer.utils.Utils;
import com.squareup.picasso.Picasso;

/**
 * Created by Ivan Root on 12.02.2018.
 */

public class PlaylistViewHolder extends RecyclerView.ViewHolder {

    private ImageView playlistImage;
    private TextView playlistName;
    private TextView date;
    private TextView time;
    private ImageButton moreBtn;

    public PlaylistViewHolder(View itemView) {
        super(itemView);
        playlistImage = (ImageView)itemView.findViewById(R.id.playlistImage);
        playlistName = (TextView)itemView.findViewById(R.id.playlistName);
        date = (TextView)itemView.findViewById(R.id.date);
        time = (TextView)itemView.findViewById(R.id.time);
        moreBtn = (ImageButton)itemView.findViewById(R.id.more_btn);
    }

    public void representPlaylistItem(Context context, PlaylistItem playlistItem){
        playlistName.setText(playlistItem.getName());
        Picasso.with(context)
                .load(Utils.getFileFromPath(playlistItem.getImagePath()))
                .error(R.drawable.music_big)
                .into(playlistImage);

    }

    public void setMoreBtnOnClickListener(View.OnClickListener onClickListener){
        moreBtn.setOnClickListener(onClickListener);
    }
}

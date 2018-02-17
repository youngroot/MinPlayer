package com.ivanroot.minplayer.adapter.viewholder;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.playlist.IPlaylist;
import com.ivanroot.minplayer.playlist.PlaylistItem;

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

    public void representPlaylistItem(IPlaylist playlist){
        playlistName.setText(playlist.getName());
        date.setText(playlist.getDate());
        time.setText(playlist.getTime());
    }

    public void representPlaylistItem(String name, Bitmap image, String creationDate, String creationTime){
        playlistName.setText(name);
        playlistImage.setImageBitmap(image);
        date.setText(creationDate);
        time.setText(creationTime);
    }

    public void representPlaylistItem(PlaylistItem playlistItem){
        playlistName.setText(playlistItem.getName());
        playlistImage.setImageBitmap(playlistItem.getBitmapImage());
    }

    public void setMoreBtnOnClickListener(View.OnClickListener onClickListener){
        moreBtn.setOnClickListener(onClickListener);
    }
}

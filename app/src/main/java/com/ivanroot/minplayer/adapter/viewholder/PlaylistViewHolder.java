package com.ivanroot.minplayer.adapter.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

    private ImageView[] playlistImages;
    private ImageView playlistImage;
    private TextView playlistName;
    private TextView playlistSize;
    private ImageButton moreBtn;

    public PlaylistViewHolder(View itemView) {
        super(itemView);
        playlistImages = new ImageView[]{
                itemView.findViewById(R.id.SubPlaylistImage1),
                itemView.findViewById(R.id.SubPlaylistImage2),
                itemView.findViewById(R.id.SubPlaylistImage3),
                itemView.findViewById(R.id.SubPlaylistImage4)

        };
        playlistName = itemView.findViewById(R.id.playlistName);
        playlistSize = itemView.findViewById(R.id.playlistSize);
        moreBtn = itemView.findViewById(R.id.more_btn);
    }

    public void representPlaylistItem(Context context, PlaylistItem playlistItem){
        try {
            playlistName.setText(playlistItem.getName());
            playlistSize.setText(playlistItem.getPlaylistSize() + " " + context.getResources().getString(R.string.songs));
            for (int i = 0; i < 4; i++) {
                String imagePath = playlistItem.getImagePaths()[i];
                if (imagePath != null) {
                    Picasso.with(context)
                            .load(Utils.getFileFromPath(imagePath))
                            .error(R.drawable.default_album_art)
                            .into(playlistImages[i]);
                }else{
                    playlistImages[i].setImageResource(R.color.colorWhite);
                }
            }
        }catch (NullPointerException ex){
            ex.printStackTrace();
            Log.e(toString(),ex.getMessage());
        }

    }

    public void setMoreBtnOnClickListener(View.OnClickListener onClickListener){
        moreBtn.setOnClickListener(onClickListener);
    }
}

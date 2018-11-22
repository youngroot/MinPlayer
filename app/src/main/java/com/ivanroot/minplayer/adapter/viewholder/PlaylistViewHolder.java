package com.ivanroot.minplayer.adapter.viewholder;

import android.content.Context;
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

public class PlaylistViewHolder extends BaseItemViewHolder<PlaylistItem> {

    private ImageView[] playlistImages;
    private ImageView playlistImage;
    private TextView playlistName;
    private TextView playlistSize;


    public PlaylistViewHolder(View itemView) {
        super(itemView);
        playlistImages = new ImageView[]{
                (ImageView) itemView.findViewById(R.id.sub_playlist_image_1),
                (ImageView) itemView.findViewById(R.id.sub_playlist_image_2),
                (ImageView) itemView.findViewById(R.id.sub_playlist_image_3),
                (ImageView) itemView.findViewById(R.id.sub_playlist_image_4)

        };
        playlistName = (TextView) itemView.findViewById(R.id.playlist_name);
        playlistSize = (TextView) itemView.findViewById(R.id.playlist_size);
        moreBtn = (ImageButton) itemView.findViewById(R.id.more_btn);
    }

    public void representItem(Context context, PlaylistItem playlistItem){
        try {
            playlistName.setText(playlistItem.getName());
            playlistSize.setText(getPlaylistSizeQuantityString(context, playlistItem));

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

    private String getPlaylistSizeQuantityString(Context context, PlaylistItem playlistItem){
        int playlistSizeInt = playlistItem.getPlaylistSize();
        return context.getResources().getQuantityString(R.plurals.song_plurals, playlistSizeInt, playlistSizeInt);
    }

}

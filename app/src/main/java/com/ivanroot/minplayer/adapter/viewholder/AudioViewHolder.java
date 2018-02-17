package com.ivanroot.minplayer.adapter.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.audio.Audio;
import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by Ivan Root on 16.12.2017.
 */

public class AudioViewHolder extends RecyclerView.ViewHolder{

    private TextView title;
    private TextView album;
    private TextView artist;
    private ImageView albumArt;

    public AudioViewHolder(View itemView) {
        super(itemView);
        title = (TextView)itemView.findViewById(R.id.songTitle);
        album = (TextView)itemView.findViewById(R.id.songAlbum);
        artist = (TextView)itemView.findViewById(R.id.songArtist);
        albumArt = (ImageView)itemView.findViewById(R.id.songAlbumArt);

    }

    public void representAudioItem(Context context, Audio audio){
        title.setText(audio.getTitle());
        album.setText(audio.getAlbum());
        artist.setText(audio.getArtist());
        try {
            Picasso.with(context)
                    .load(new File(audio.getAlbumArt()))
                    .error(R.drawable.default_album_art)
                    .into(albumArt);

        }catch (NullPointerException ex){
            albumArt.setImageResource(R.drawable.default_album_art);
        }

    }
}

package com.ivanroot.minplayer.adapter.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.utils.Utils;
import com.squareup.picasso.Picasso;

/**
 * Created by Ivan Root on 16.12.2017.
 */

public class AudioViewHolder extends BaseItemViewHolder<Audio> {

    protected TextView title;
    protected TextView album;
    protected TextView artist;
    protected ImageView albumArt;

    public AudioViewHolder(View itemView) {
        super(itemView);
        title = (TextView) itemView.findViewById(R.id.song_title);
        album = (TextView) itemView.findViewById(R.id.song_album);
        artist = (TextView) itemView.findViewById(R.id.song_artist);
        albumArt = (ImageView) itemView.findViewById(R.id.album_art);
        moreBtn = (ImageButton) itemView.findViewById(R.id.more_btn);

    }

    @Override
    public void representItem(Context context, Audio audio) {
        title.setText(audio.getTitle());
        album.setText(audio.getAlbum());
        artist.setText(audio.getArtist());

        Picasso.with(context)
                .load(Utils.getFileFromPath(audio.getAlbumArtPath()))
                .error(R.drawable.default_album_art)
                .into(albumArt);
    }

}

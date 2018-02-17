package com.ivanroot.minplayer.playlist;

import android.graphics.Bitmap;

import com.ivanroot.minplayer.utils.Utils;

/**
 * Created by Ivan Root on 16.02.2018.
 */

public class PlaylistItem {

    public String name;

    public byte[] image;

    String date;

    String time;

    public PlaylistItem(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getBitmapImage(){
        return Utils.getBitmapFromByteArray(image);
    }

    public byte[] getImage(){
        return image;
    }

    public void setImage(byte[] image){
        this.image = image;
    }

    public void setBitmapImage(Bitmap bitmap){
        image = Utils.getByteArrayFromBitmap(bitmap);
    }


}

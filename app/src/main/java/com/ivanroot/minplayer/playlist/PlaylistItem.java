package com.ivanroot.minplayer.playlist;

/**
 * Created by Ivan Root on 16.02.2018.
 */

public class PlaylistItem {

    private String name;

    private String imagePath;

    private String date;

    private String time;

    public PlaylistItem(){}

    public PlaylistItem(String name, String imagePath, String date, String time) {
        this.name = name;
        this.imagePath = imagePath;
        this.date = date;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImagePath(){
        return imagePath;
    }

    public void setImagePath(String imagePath){
        this.imagePath = imagePath;
    }



}

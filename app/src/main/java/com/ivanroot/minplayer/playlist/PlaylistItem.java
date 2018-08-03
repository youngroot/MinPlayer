package com.ivanroot.minplayer.playlist;

import android.support.annotation.NonNull;

import java.util.Date;

/**
 * Created by Ivan Root on 16.02.2018.
 */

public class PlaylistItem {

    private String name;

    private int playlistSize;

    private String imagePath;

    private String[] imagePaths;

    private Date dateAdded;

    private Date dateModified;

    public PlaylistItem(){}

    public PlaylistItem(@NonNull String name, int playlistSize, @NonNull String imagePath, Date dateAdded, Date dateModified) {
        this.name = name;
        this.playlistSize = playlistSize;
        this.imagePath = imagePath;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
    }

    public PlaylistItem(@NonNull String name, int playlistSize, @NonNull String[] imagePaths, Date dateAdded, Date dateModified){
        this.name = name;
        this.playlistSize = playlistSize;
        this.imagePaths = imagePaths;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPlaylistSize(){
        return playlistSize;
    }

    public void setPlaylistSize(int playlistSize){
        this.playlistSize = playlistSize;
    }

    public String getImagePath(){
        return imagePath;
    }

    public void setImagePath(String imagePath){
        this.imagePath = imagePath;
    }

    public String[] getImagePaths(){
        return imagePaths;
    }

    public void setImagePaths(@NonNull String[] imagePaths){
        this.imagePaths = imagePaths;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

}
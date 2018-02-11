package com.ivanroot.minplayer.audio;

import java.io.Serializable;

/**
 * Created by Ivan Root on 02.06.2017.
 */

public class Audio implements Serializable {
    private String id;
    private String data;
    private String title;
    private String album;
    private String artist;
    private String genre;

    public  Audio(){}

    @Override
    public String toString() {
        return id + "\n" + data + "\n" + title + "\n" + album + "\n" + artist;
    }

    public Audio(String id, String data, String title, String album, String artist) {
        this.id = id;
        this.data = data;
        this.title = title;
        this.album = album;
        this.artist = artist;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    @Override
    public boolean equals(Object object){

        if(object != null && object instanceof Audio){

            Audio audio = (Audio) object;

            if(!data.equals(audio.data))  return false;
            if(!title.equals(audio.title))  return false;
            if(!album.equals(audio.album))  return false;
            if(!artist.equals(audio.artist))  return false;

            return true;
        }
        return false;
    }



}

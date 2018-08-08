package com.ivanroot.minplayer.audio;

import java.io.Serializable;

/**
 * Created by Ivan Root on 02.06.2017.
 */

public class Audio implements Serializable {
    private long id;
    private String localPath;
    private String cloudPath;
    private String md5Hash;
    private String title;
    private String album;
    private String artist;
    private String genre;
    private String albumArtPath;

    public  Audio(){}

    @Override
    public String toString() {
        return id + "\n" + localPath + "\n" + cloudPath + "\n" + md5Hash + "\n" + title + "\n" + album + "\n" + artist;
    }

    public Audio(long id, String localPath, String title, String album, String artist) {
        this.id = id;
        this.localPath = localPath;
        this.title = title;
        this.album = album;
        this.artist = artist;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getData() {
        return localPath;
    }

    public void setData(String data) {
        this.localPath = data;
    }

    public String getCloudPath() {
        return cloudPath;
    }

    public void setMd5Hash(String md5Hash){
        this.md5Hash = md5Hash;
    }

    public String getMd5Hash(){
        return md5Hash;
    }

    public void setCloudPath(String cloudPath) {
        this.cloudPath = cloudPath;
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

    public String getAlbumArtPath() {
        return albumArtPath;
    }

    public void setAlbumArtPath(String albumArtPath) {
        this.albumArtPath = albumArtPath;
    }



    @Override
    public boolean equals(Object object){

        if(object != null && object instanceof Audio){

            Audio audio = (Audio) object;
            if(md5Hash != null && audio.md5Hash != null && md5Hash.equals(audio.md5Hash)) return true;
            try {

                if (!localPath.equals(audio.localPath)) return false;
                if (!title.equals(audio.title)) return false;
                if (!album.equals(audio.album)) return false;
                if (!artist.equals(audio.artist)) return false;
            }catch (NullPointerException ex){
                return false;
            }

            return true;
        }
        return false;
    }

}

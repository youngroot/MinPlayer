package com.ivanroot.minplayer.audio;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by Ivan Root on 02.06.2017.
 */

public class Audio implements Serializable, Comparable<Audio> {
    private long id;
    private long size;
    private String localData;
    private String cloudData;
    private String md5Hash;
    private String title;
    private String album;
    private String artist;
    private String genre;
    private String albumArtPath;

    public Audio() {}

    public Audio(long id, long size, String localData, String cloudData, String md5Hash, String title, String album, String artist, String genre, String albumArtPath) {
        this.id = id;
        this.size = size;
        this.localData = localData;
        this.cloudData = cloudData;
        this.md5Hash = md5Hash;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.genre = genre;
        this.albumArtPath = albumArtPath;
    }

    @Override
    public String toString() {
        return id + " " + localData + " " + cloudData + " " + md5Hash + " " + title + " " + album + " " + artist;
    }

    public Audio(long id, String localData, String title, String album, String artist) {
        this.id = id;
        this.localData = localData;
        this.title = title;
        this.album = album;
        this.artist = artist;
    }

    public Audio(long size, String title, String cloudData, String md5Hash){
        this.size = size;
        this.title = title;
        this.cloudData = cloudData;
        this.md5Hash = md5Hash;
    }

    public long getId() {
        return id;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getSize() {
        return size;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLocalData() {
        return localData;
    }

    public void setLocalData(String localData) {
        this.localData = localData;
    }

    public String getCloudData() {
        return cloudData;
    }

    public void setMd5Hash(String md5Hash) {
        this.md5Hash = md5Hash;
    }

    public String getMd5Hash() {
        return md5Hash;
    }

    public void setCloudData(String cloudData) {
        this.cloudData = cloudData;
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
    public boolean equals(Object object) {

        if (object instanceof Audio) {
            Audio audio = (Audio) object;
            if (this.md5Hash != null && audio.md5Hash != null && Objects.equals(this.md5Hash, audio.md5Hash))
                return true;

            if (!Objects.equals(this.localData, audio.localData)) return false;
            if (!Objects.equals(this.title, audio.title)) return false;
            if (!Objects.equals(this.album, audio.album)) return false;
            if (!Objects.equals(this.artist, audio.artist)) return false;

            return true;
        }
        return false;
    }

    @Override
    public int compareTo(@NonNull Audio audio) {
        return Long.compare(getSize(), audio.getSize());
    }
}

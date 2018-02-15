package com.ivanroot.minplayer.playlist;

import com.ivanroot.minplayer.audio.Audio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by Ivan Root on 14.06.2017.
 */

public class Playlist implements Serializable, IPlaylist {

    protected String name = "";
    protected String date = "";
    protected String time = "";
    protected boolean isShuffled = false;
    protected int audioIndex = -1;
    protected Audio currAudio = null;
    protected List<Audio>playlist;
    protected List<Audio> non_shuffled_playlist;
    protected List<Audio>shuffled_playlist;
    protected int repeatMode  = IPlaylist.NOT_REPEAT;

    public Playlist(){
        playlist = new ArrayList<>();
    }

    public Playlist(String name){
        this.name = name;
        playlist = new ArrayList<>();
    }

    @Override
    public void addAudio(Audio audio){

        playlist.add(audio);
    }

    @Override
    public void deleteAudio(int audioIndex) {

        if(audioIndex > -1 && audioIndex < playlist.size()){

            Audio tempAudio = playlist.get(audioIndex);
            playlist.remove(audioIndex);

            if(isShuffled){
                int tempIndex = non_shuffled_playlist.lastIndexOf(tempAudio);
                non_shuffled_playlist.remove(tempIndex);
            }
        }
    }

    @Override
    public void deleteAudio(Audio audio) {
        if(playlist.contains(audio)){
            checkAndSetAudio(audio);
            setToPrevAudio();
            playlist.remove(audio);
            if(isShuffled){
                non_shuffled_playlist.remove(audio);
            }
        }
    }

    @Override
    public Audio getAudio(int audioIndex){
        if(audioIndex > -1 && audioIndex < playlist.size()){
            if(!isShuffled)
                return playlist.get(audioIndex);
            return non_shuffled_playlist.get(audioIndex);
        }
        else
            return new Audio();

    }


    public Playlist setAudioList(List<Audio> playlist){

        this.playlist = null;
        this.non_shuffled_playlist = null;
        this.shuffled_playlist = null;

        this.playlist = playlist;
        if(isShuffled) {
            isShuffled = false;
            shuffle();
        }
        return this;
    }


    public  List<Audio> getAudioList(){

        if(isShuffled){
            return non_shuffled_playlist;
        }
        return playlist;
    }

    @Override
    public boolean checkAndSetAudio(Audio audio){

        if(audio == null) return false;

        if(playlist.contains(audio)){
            audioIndex = playlist.indexOf(audio);
            currAudio = audio;
            return true;
        }
        return false;
    }

    @Override
    public Audio getCurrentAudio(){

        return currAudio;
    }

    @Override
    public void cleanCurrAudio() {

        currAudio = null;
    }

    @Override
    public boolean contains(Audio audio) {
        return playlist.contains(audio);
    }

    @Override
    public boolean setAudio(int audioIndex){

        if(audioIndex > -1 && audioIndex < playlist.size()){

            if(!isShuffled) {

                currAudio = playlist.get(audioIndex);
                this.audioIndex = audioIndex;

            }
            else{
                currAudio = non_shuffled_playlist.get(audioIndex);
                this.audioIndex = playlist.indexOf(currAudio);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean setToNextAudio(){

        if(audioIndex + 1 < playlist.size() && repeatMode != IPlaylist.REPEAT_ONE) {
            audioIndex++;
            currAudio = playlist.get(audioIndex);
            return true;

        }
        else
        if(audioIndex + 1 == playlist.size() && repeatMode == IPlaylist.REPEAT_ALL){
            setToFirstAudio();
            return true;
        }
        return false;

    }

    @Override
    public boolean setToPrevAudio(){


        if(audioIndex - 1 > -1 && repeatMode != IPlaylist.REPEAT_ONE){
            audioIndex--;
            currAudio = playlist.get(audioIndex);
            return true;
        }
        else
        if(audioIndex - 1 == -1 && repeatMode == IPlaylist.REPEAT_ALL){
            setToLastAudio();
            return true;
        }
        return false;
    }

    @Override
    public void setToFirstAudio(){
        audioIndex = 0;
        currAudio = playlist.get(audioIndex);
    }

    @Override
    public void setToLastAudio(){

        audioIndex = playlist.size() - 1;
        currAudio = playlist.get(audioIndex);
    }

    @Override
    public boolean shuffle (){

        if(!isShuffled) {
            shuffled_playlist = new ArrayList<>();
            for (int i = 0; i < playlist.size(); i++) {
                shuffled_playlist.add(playlist.get(i));
            }
            non_shuffled_playlist = playlist;
            Collections.shuffle(shuffled_playlist);
            playlist = shuffled_playlist;
            audioIndex = playlist.indexOf(currAudio);
            isShuffled = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean unShuffle(){
        if(isShuffled) {
            playlist = non_shuffled_playlist;
            audioIndex = playlist.indexOf(currAudio);
            shuffled_playlist = null;
            isShuffled = false;
            return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setRepeatMode(int repeatMode){

        if(repeatMode > -1 && repeatMode < 3){
            this.repeatMode = repeatMode;
        }
        else this.repeatMode = IPlaylist.NOT_REPEAT;
    }

    @Override
    public int getRepeatMode(){
        return repeatMode;
    }

    @Override
    public  int size(){
        return playlist.size();
    }

    @Override
    public boolean isShuffled(){
        return isShuffled;
    }

    @Override
    public void update(){

    }

    @Override
    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String getTime() {
        return time;
    }

    @Override
    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String getDate() {
        return date;
    }

    @Override
    public boolean equals(IPlaylist playlist){

        if(playlist == null) return false;
        if(!playlist.getName().equals(this.getName())) return false;
        if(playlist.size() != this.size()) return false;
        if(playlist.getRepeatMode() != this.getRepeatMode()) return false;
        if(playlist.isShuffled() != this.isShuffled()) return false;
        for(int i = 0; i < playlist.size(); i++)
        {
            if(!this.getAudio(i).equals(playlist.getAudio(i))) return false;
        }
        return true;
    }
}

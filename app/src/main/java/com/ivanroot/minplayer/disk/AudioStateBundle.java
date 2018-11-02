package com.ivanroot.minplayer.disk;

import android.support.annotation.NonNull;

import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.utils.Utils;

public class AudioStateBundle {
    private Audio taskAudio;
    private long loaded;
    private long total;

    public AudioStateBundle(){
        taskAudio = new Audio();
        loaded = 0;
    }

    @Override
    public String toString() {
        return String.valueOf(taskAudio.getTitle())
                + " loaded " +
                String.valueOf(loaded)
                + " of " +
                taskAudio.getSize();

    }

    public AudioStateBundle(@NonNull Audio taskAudio, long loaded) {
        this.taskAudio = taskAudio;
        this.loaded = loaded;
        this.total = taskAudio.getSize();
    }

    public AudioStateBundle(@NonNull Audio taskAudio, long loaded, long total){
        this.taskAudio = taskAudio;
        this.loaded = loaded;
        this.total = total;
    }

    public Audio getTaskAudio() {
        return taskAudio;
    }

    public void setTaskAudio(Audio taskAudio) {
        this.taskAudio = taskAudio;
    }

    public long getLoaded() {
        return loaded;
    }

    public int getLoadedPercentage(){
        return Utils.getLoadedPercentage(loaded, getTotal());
    }

    public long getTotal(){
        return total;
    }

    public void setTotal(long total){
        this.total = total;
    }

    public void setLoaded(long loaded) {
        this.loaded = loaded;
    }

    public boolean isLoaded(){
        return total == loaded;
    }
}

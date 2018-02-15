package com.ivanroot.minplayer.playlist;

import com.ivanroot.minplayer.audio.Audio;

import java.util.Date;

/**
 * Created by Ivan Root on 05.07.2017.
 */

public interface IPlaylist {

    int NOT_REPEAT = 0;
    int REPEAT_ALL = 1;
    int REPEAT_ONE = 2;

    void addAudio(Audio audio);

    void deleteAudio(int audioIndex);

    void deleteAudio(Audio audio);

    Audio getAudio(int audioIndex);

    boolean checkAndSetAudio(Audio audio);

    Audio getCurrentAudio();

    void cleanCurrAudio();

    boolean contains(Audio audio);

    boolean setAudio(int audioIndex);

    boolean setToNextAudio();

    boolean setToPrevAudio();

    void setToFirstAudio();

    void setToLastAudio();

    boolean shuffle();

    boolean unShuffle();

    String getName();

    void setName(String name);

    void setRepeatMode(int repeatMode);

    int getRepeatMode();

    int size();

    boolean isShuffled();

    void update();

    void setTime(String time);

    String getTime();

    void setDate(String date);

    String getDate();

    boolean equals(IPlaylist playlist);
}

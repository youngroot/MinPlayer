package com.ivanroot.minplayer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.hwangjr.rxbus.Bus;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.ivanroot.minplayer.player.constants.PlayerActions;
import com.ivanroot.minplayer.player.constants.PlayerEvents;

public abstract class VisualizationFragmentBase extends Fragment {
    protected int audioSessionId = -1;
    private Bus rxBus = RxBus.get();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rxBus.register(this);
        rxBus.post(PlayerActions.ACTION_GET_AUDIO_SESSION_ID, this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //onAudioSessionId(audioSessionId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        rxBus.unregister(this);
    }

    @Subscribe(tags = {@Tag(PlayerEvents.EVENT_ON_GET_AUDIO_SESSION_ID)})
    public void onAudioSessionEvent(Integer audioSessionId){
        this.audioSessionId = audioSessionId;
        onAudioSessionId(audioSessionId);
    }
    
    protected abstract void onAudioSessionId(int audioSessionId);
}

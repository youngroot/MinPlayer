package com.ivanroot.minplayer.playlist;

import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.TransitionSet;

public class PlaylistItemSharedElementTransition extends TransitionSet {
    public PlaylistItemSharedElementTransition(){
        addTransition(new ChangeBounds()).
                addTransition(new ChangeTransform());
    }
}

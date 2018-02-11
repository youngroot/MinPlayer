package com.ivanroot.minplayer.player;

import com.hwangjr.rxbus.Bus;

/**
 * Created by Ivan Root on 02.01.2018.
 */

public class RxBus {
    private static Bus sBus;

    public static synchronized Bus getInstance() {
        if (sBus == null) {
            sBus = new Bus();
        }
        return sBus;
    }
}

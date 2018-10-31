package com.ivanroot.minplayer.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class RxNetworkChangeReceiver extends BroadcastReceiver {

    private Context context;
    private OnReceiveListener onReceiveListener;
    private NetworkInfo lastState;
    private static final Map<Context, RxNetworkChangeReceiver> consumers = new HashMap<>();

    private RxNetworkChangeReceiver() {
    }

    public static synchronized RxNetworkChangeReceiver create(Context context) {
        if (consumers.get(context) != null)
            return consumers.get(context);

        RxNetworkChangeReceiver rxNetworkChangeReceiver = new RxNetworkChangeReceiver();
        rxNetworkChangeReceiver.context = context;

        return rxNetworkChangeReceiver;
    }

    public RxNetworkChangeReceiver register() {
        if (consumers.get(context) != this) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(this, intentFilter);
            consumers.put(context, this);
        }
        return this;
    }

    public void unregister() {
        if (consumers.get(context) == this) {
            context.unregisterReceiver(this);
            consumers.remove(context);
        }
    }

    public Observable<NetworkInfo> asObservable() {
        return Observable.<NetworkInfo>create(emitter -> {
            if (lastState != null)
                emitter.onNext(lastState);
            onReceiveListener = networkInfo -> emitter.onNext(networkInfo);
        })
                .doOnDispose(() -> {
                    if (context != null) unregister();
                }).doOnNext(networkInfo -> Log.i("RxNetworkChangeReceiver", String.valueOf(networkInfo.isConnected())));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Objects.equals(intent.getAction(), ConnectivityManager.CONNECTIVITY_ACTION)) return;

        NetworkInfo networkInfo = intent.getParcelableExtra("networkInfo");

        if (networkInfo == null) return;

        if (onReceiveListener != null)
            onReceiveListener.onReceive(networkInfo);
        lastState = networkInfo;
    }

    public interface OnReceiveListener {
        void onReceive(NetworkInfo networkInfo);
    }
}

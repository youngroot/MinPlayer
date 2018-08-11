package com.ivanroot.minplayer.disk;

import android.content.Context;
import android.net.NetworkInfo;

import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.facebook.stetho.okhttp.StethoInterceptor;
import com.ivanroot.minplayer.activity.TokenActivity;
import com.ivanroot.minplayer.utils.Pair;
import com.ivanroot.minplayer.utils.RxNetworkChangeReceiver;
import com.squareup.okhttp.OkHttpClient;
import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.OkHttpClientFactory;
import com.yandex.disk.rest.RestClient;


import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class RestClientUtil {

    private static Credentials credentials;
    private static OkHttpClient client;

    public static RestClient getInstance(final Credentials credentials) {
        RestClientUtil.credentials = credentials;

        if(client == null) {
            client = OkHttpClientFactory.makeClient();
            client.networkInterceptors().add(new StethoInterceptor());
        }
        return new RestClient(credentials, client);
    }

    public static Observable<Pair<RestClient, NetworkInfo>> asObservable(RxNetworkChangeReceiver changeReceiver, RxSharedPreferences rxPreferences){
        Observable<String> prefObservable = rxPreferences.getString(TokenActivity.PREF_ACCESS_TOKEN)
                .asObservable();

        Observable<NetworkInfo> connObservable = changeReceiver.asObservable();

        return Observable.combineLatest(prefObservable, connObservable, (token, networkInfo) -> {
                    if (token != null && networkInfo.isConnected())
                        return new Pair<>(getInstance(new Credentials("", token)), networkInfo);
                    else
                        return new Pair<>(null, networkInfo);
                });
    }

    public static OkHttpClient getClient(){
        return client;
    }

    public static Credentials getCredentials(){
        return credentials;
    }
}

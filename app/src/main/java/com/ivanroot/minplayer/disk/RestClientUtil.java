package com.ivanroot.minplayer.disk;

import android.content.Context;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.facebook.stetho.okhttp.StethoInterceptor;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.ivanroot.minplayer.activity.TokenActivity;
import com.ivanroot.minplayer.utils.Pair;
import com.ivanroot.minplayer.utils.RxNetworkChangeReceiver;
import com.squareup.okhttp.OkHttpClient;
import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.OkHttpClientFactory;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.http.UnauthorizedException;


import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RestClientUtil {

    private static Credentials credentials;
    private static OkHttpClient client;

    public static final String KEY_REST_CLIENT = "com.ivanroot.minplayer.key_rest_client";
    public static final String KEY_NETWORK_INFO = "com.ivanroot.minplayer.key_network_info";
    public static final String KEY_ERROR = "com.ivanroot.minplayer.key_error";



    public static RestClient getInstance(final Credentials credentials) {
        RestClientUtil.credentials = credentials;

        if (client == null) {
            client = OkHttpClientFactory.makeClient();
            client.networkInterceptors().add(new StethoInterceptor());
        }
        return new RestClient(credentials, client);
    }

    public static Observable<Map<String, Object>> asObservable(Observable<String> tokenObservable, Observable<NetworkInfo> networkInfoObservable) {
        return Observable.combineLatest(tokenObservable, networkInfoObservable, (token, networkInfo) -> {
            Map<String, Object> map = new HashMap<>();
            if(token != null && networkInfo.isConnected())
                map.put(KEY_REST_CLIENT, getInstance(new Credentials("", token)));
            map.put(KEY_NETWORK_INFO, networkInfo);
            return map;

        }).observeOn(Schedulers.io())
                .map(state -> {
                    RestClient restClient = (RestClient) state.get(KEY_REST_CLIENT);
                    try{
                        if(restClient != null) restClient.getDiskInfo();

                    } catch (UnauthorizedException ex){
                        state.put(KEY_ERROR, ex);
                        state.remove(KEY_REST_CLIENT);
                    }
                    return state;
                });
    }

    public static Observable<Map<String, Object>> asObservable(RxSharedPreferences rxPreferences, @NonNull String tokenKey){
        return ReactiveNetwork.observeInternetConnectivity()
                .observeOn(Schedulers.io())
                .filter(b -> b)
                .switchMap(b -> rxPreferences.getString(tokenKey).asObservable())
                .map(token -> new RestClient(new Credentials("", token)))
                .map(restClient -> {
                    Map<String, Object> state = new HashMap<>();
                    try{
                        restClient.getDiskInfo();
                        state.put(KEY_REST_CLIENT, restClient);
                    }catch (UnauthorizedException | UnknownHostException ex){
                        state.put(KEY_ERROR, ex);
                    }
                    return state;
                });
    }

    public static OkHttpClient getClient() {
        return client;
    }

    public static Credentials getCredentials() {
        return credentials;
    }
}

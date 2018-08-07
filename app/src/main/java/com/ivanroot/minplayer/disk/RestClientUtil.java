package com.ivanroot.minplayer.disk;

import com.facebook.stetho.okhttp.StethoInterceptor;
import com.squareup.okhttp.OkHttpClient;
import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.OkHttpClientFactory;
import com.yandex.disk.rest.RestClient;

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

    public static OkHttpClient getClient(){
        return client;
    }

    public static Credentials getCredentials(){
        return credentials;
    }
}

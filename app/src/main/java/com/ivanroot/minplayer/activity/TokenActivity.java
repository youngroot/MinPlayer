package com.ivanroot.minplayer.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.ivanroot.minplayer.R;

public class TokenActivity extends AppCompatActivity {

    private WebView webView;
    private String clientId = "4057d3127f6b42cd8813aa2048bf3a48";
    private static String uniqueID = null;
    public static final String PREF_CREDENTIALS = "PREF_CREDENTIALS";
    public static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
    public static final String PREF_ACCESS_TOKEN = "PREF_ACCESS_TOKEN";
    private String callbackUrl = "https://yx4057d3127f6b42cd8813aa2048bf3a48.oauth.yandex.ru/auth/finish?platform=android";
    private String yandexTokenUrl = "https://oauth.yandex.ru/authorize?response_type=token" +
            "&client_id={0}" +
            "&device_id={1}" +
            "&device_name={2}" +
            "&force_confirm=true" +
            "&state=login" +
            "&display=popup";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_token);

        yandexTokenUrl = MessageFormat.format(yandexTokenUrl, clientId, id(this), Build.MODEL);

        webView = (WebView) findViewById(R.id.web_view);
        setWebView(webView);
        webView.loadUrl(yandexTokenUrl);

    }

    public void setWebView(final WebView webView) {
        final Activity activity = this;
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                activity.setProgress(newProgress * 1000);
            }
        });

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(callbackUrl) && url.contains("access_token")) {
                    extractAndSaveToken(url);
                    finish();
                }
                return false;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }
        });

    }

    public synchronized static String id(Context context) {
        if (uniqueID == null) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(
                    PREF_CREDENTIALS, Context.MODE_PRIVATE);
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(PREF_UNIQUE_ID, uniqueID);
                editor.apply();
            }
        }
        return uniqueID;
    }

    public synchronized static Map<String, String> getUrlParams(@NonNull String url) {
        Map<String, String> params = new HashMap<>();
        String[] splittedUrl = url.split("[?#&]");

        for (String substr : splittedUrl) {
            if (substr.contains("=")) {
                String[] keyValue = substr.split("=");
                params.put(keyValue[0], keyValue[1]);
            }
        }

        return params;
    }

    private void extractAndSaveToken(String url) {
        Map<String, String> params = getUrlParams(url);
        String token = params.get("access_token");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_ACCESS_TOKEN, token);
        editor.apply();
        Intent intent = new Intent();
        intent.putExtra("access_token",token);
        setResult(RESULT_OK, intent);
    }
}

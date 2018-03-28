package com.ivanroot.minplayer.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.ivanroot.minplayer.R;

import java.util.ArrayList;

public class StartupActivity extends AppCompatActivity {

    private final int MIN_PLAYER_PERMISSIONS_REQUEST = 42;
    private final int permissionDenied = PackageManager.PERMISSION_DENIED;
    private final int permissionGranted = PackageManager.PERMISSION_GRANTED;
    private String[] permissions = new String[]{

            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO

    };
    private ArrayList<Boolean> permissionRationale = new ArrayList<>();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("StartupActivity","onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MIN_PLAYER_PERMISSIONS_REQUEST){
            if(checkPermissions()){
                startPlayer(0);
                finish();
            }
            else{
                System.exit(0);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("StartupActivity","onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        if(!checkPermissions()){
            requestPermissions();
        }else{

            startPlayer(0);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.i("StartupActivity","onRequestPermissionsResult");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length != permissions.length) {
            requestPermissions();
            return;
        }

        for(int i = 0; i < grantResults.length; i++) {

            if(grantResults[i] == permissionDenied){
                if(permissionRationale.get(i)){

                    requestPermissions();
                }
                else{

                    openSettings();
                }
                return;
            }
        }

        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }


    private boolean checkPermissions(){
        Log.i("StartupActivity","checkPermission");
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == permissionDenied) return false;
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == permissionDenied) return false;
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == permissionDenied) return false;
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO) == permissionDenied) return false;
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.MODIFY_AUDIO_SETTINGS) == permissionDenied) return false;

        return true;
    }

    private void requestPermissions(){
        Log.i("StartupActivity","requestPermission");
        permissionRationale.clear();
        for(String permission : permissions)
            permissionRationale.add(ActivityCompat.shouldShowRequestPermissionRationale(this,permission));

        ActivityCompat.requestPermissions(this,permissions,MIN_PLAYER_PERMISSIONS_REQUEST);
    }

    private void openSettings() {
        Log.i("StartupActivity","openSettings");
        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        startActivityForResult(appSettingsIntent, MIN_PLAYER_PERMISSIONS_REQUEST);
    }

    private void startPlayer(int waitMillis){
        Log.i("StartupActivity","startPlayer");
        new Handler()
                .postDelayed(() -> {
                    Intent intent = new Intent(StartupActivity.this,MainActivity.class);
                    startActivity(intent);
                },waitMillis);
    }
}

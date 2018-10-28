package com.ivanroot.minplayer.disk;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hwangjr.rxbus.Bus;
import com.hwangjr.rxbus.RxBus;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.activity.TokenActivity;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.utils.Pair;
import com.ivanroot.minplayer.utils.Utils;
import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.ProgressListener;
import com.yandex.disk.rest.RestClient;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AudioDownloadService extends Service {
    private static final int NOTIFICATION_ID = 102;
    public static final String EXTRA_AUDIO_SIZE = "extra_audio_size";
    public static final String EXTRA_AUDIO_PATH = "extra_audio_path";
    public static final String EXTRA_MD5_HASH = "extra_md5_hash";
    public static final String EXTRA_AUDIO_TITLE = "extra_audio_title";

    private SharedPreferences sharedPreferences;
    private RxSharedPreferences rxPreferences;
    private PriorityBlockingQueue<Audio> audioDownloadPq;
    private Disposable currTaskDisposable;
    private Disposable prefDisposable;
    private RestClient restClient;
    private Bus rxBus = RxBus.get();

    private NotificationManager notificationManager;
    private Notification.Builder notificationBuilder;

    @Override
    public void onCreate() {
        Log.i("AudioDownloadService", "onCreate");
        rxBus.register(this);
        sharedPreferences = getSharedPreferences(getPackageName() + ".AudioDownloadService", MODE_PRIVATE);
        rxPreferences = RxSharedPreferences.create(PreferenceManager.getDefaultSharedPreferences(this));
        prefDisposable = rxPreferences.getString(TokenActivity.PREF_ACCESS_TOKEN)
                .asObservable()
                .subscribe(token -> restClient = RestClientUtil.getInstance(new Credentials("", token)));

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        restoreTasks();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("AudioDownloadService", "onStartCommand");
        if (intent == null && !audioDownloadPq.isEmpty()) {
            startNewTask(startId);
            return START_STICKY;
        } else if (intent == null) {
            Log.i("AudioDownloadService", "intent == null");
            stopSelf();
            return START_NOT_STICKY;
        }

        Audio taskAudio = getAudioFromIntent(intent);

        if (isAudioValidForDownloading(taskAudio) && !audioDownloadPq.contains(taskAudio)) {
            audioDownloadPq.add(taskAudio);
            saveTasks();
            rxBus.post(AudioStatus.STATUS_AUDIO_PREPARING, taskAudio.getMd5Hash());

            if (currTaskDisposable == null || currTaskDisposable.isDisposed()) {
                startNewTask(startId);
            }
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i("AudioDownloadService", "onDestroy");
        rxBus.unregister(this);
        if (prefDisposable != null)
            prefDisposable.dispose();
        saveTasks();
        removeNotification();
    }

    private void saveTasks() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (!audioDownloadPq.isEmpty()) {
            String json = new Gson().toJson(audioDownloadPq);
            editor.putString("tasks_json", json).apply();
        } else {
            editor.putString("tasks_json", null).apply();
        }
    }

    private void restoreTasks() {
        String json = sharedPreferences.getString("tasks_json", null);
        Log.i("AudioDownloadService", "restored tasks json = " + String.valueOf(json));
        if (json != null) {
            Type typeToken = new TypeToken<PriorityBlockingQueue<Audio>>() {
            }.getType();
            audioDownloadPq = new Gson().fromJson(json, typeToken);
        } else {
            audioDownloadPq = new PriorityBlockingQueue<>();
        }
    }

    private void startNewTask(int startId) {
        Audio taskAudio = audioDownloadPq.peek();

        currTaskDisposable = getDownloadStatusObservable(taskAudio)
                .subscribeOn(Schedulers.newThread())
                .delay(1000, TimeUnit.MILLISECONDS)
                //.observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> {
                    removeNotification();
                    rxBus.post(AudioStatus.STATUS_AUDIO_DOWNLOADED, taskAudio.getMd5Hash());
                    audioDownloadPq.remove(taskAudio);
                    saveTasks();

                    if (!audioDownloadPq.isEmpty())
                        startNewTask(startId);

                    stopSelf(startId);

                })
                .subscribe(pair -> {
                    updateNotification(getLoadedPercentage(pair.first, pair.second));
                    HashMap<String, Pair<Long, Long>> state = new HashMap<>();
                    state.put(taskAudio.getMd5Hash(), pair);
                    rxBus.post(AudioStatus.STATUS_AUDIO_DOWNLOADING, state);
                });
    }

    private Observable<Pair<Long, Long>> getDownloadStatusObservable(Audio taskAudio) {
        return Observable.create(emitter -> {

            File saveTo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), taskAudio.getTitle());
            String saveToMd5Hash = Utils.getMd5Hash(Utils.getFileBytes(saveTo));

            if (!saveToMd5Hash.equals(taskAudio.getMd5Hash())) {
                buildNotification(taskAudio);
                restClient.downloadFile(taskAudio.getCloudData(), saveTo, new ProgressListener() {

                    @Override
                    public void updateProgress(long loaded, long total) {
                        emitter.onNext(new Pair<>(loaded, total));
                    }

                    @Override
                    public boolean hasCancelled() {
                        return false;
                    }

                });
            }

            emitter.onComplete();
        });
    }

    private boolean isAudioValidForDownloading(Audio candidate) {
        return candidate.getSize() != 0 && candidate.getTitle() != null && candidate.getCloudData() != null && candidate.getMd5Hash() != null;
    }

    private Audio getAudioFromIntent(@NonNull Intent intent) {
        long size = intent.getLongExtra(EXTRA_AUDIO_SIZE, 0);
        String title = intent.getStringExtra(EXTRA_AUDIO_TITLE);
        String path = intent.getStringExtra(EXTRA_AUDIO_PATH);
        String md5Hash = intent.getStringExtra(EXTRA_MD5_HASH);
        return new Audio(size, title, path, md5Hash);
    }

    private void buildNotification(Audio taskAudio) {
        notificationBuilder = new Notification.Builder(this);
        notificationBuilder.setContentTitle(getResources().getString(R.string.downloading_from_disk))
                .setOngoing(true)
                .setContentText(taskAudio.getTitle())
                .setSmallIcon(R.drawable.ic_music_rounded)
                .setPriority(Notification.PRIORITY_HIGH);

        notificationBuilder.setProgress(100, 0, true);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

    }

    private void updateNotification(int loadedPercentage) {
        notificationBuilder.setProgress(100, loadedPercentage, false);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void removeNotification() {
//        notificationBuilder.setContentText(getResources().getString(R.string.download_complete))
//                .setProgress(0,0,false);
//        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private int getLoadedPercentage(long loaded, long total) {
        return (int) (((double) loaded) / total * 100);
    }
}

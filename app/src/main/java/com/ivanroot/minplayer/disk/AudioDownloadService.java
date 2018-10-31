package com.ivanroot.minplayer.disk;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.github.pwittchen.reactivenetwork.library.rx2.ConnectivityPredicate;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
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
import com.yandex.disk.rest.exceptions.http.ServiceUnavailableException;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import io.reactivex.Observable;
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
    private Disposable downloadTasksDisposable;
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
        notificationBuilder = new Notification.Builder(this);
        readTasks();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("AudioDownloadService", "onStartCommand");
        if (intent == null && !audioDownloadPq.isEmpty()) {
            if (downloadTasksDisposable != null)
                downloadTasksDisposable.dispose();
            downloadTasksDisposable = getDownloadTasksObservable()
                    .subscribe();
            return START_STICKY;
        } else if (intent == null) {
            Log.i("AudioDownloadService", "intent == null");
            stopSelf();
            return START_NOT_STICKY;
        }

        Audio taskAudio = getAudioFromIntent(intent);

        if (isAudioValidForDownloading(taskAudio) && !audioDownloadPq.contains(taskAudio)) {
            audioDownloadPq.add(taskAudio);
            postAudioPreparingState(new DownloadingAudioBundle(taskAudio, 0));

            if (downloadTasksDisposable == null || downloadTasksDisposable.isDisposed()) {
                downloadTasksDisposable = getDownloadTasksObservable()
                        .subscribe();
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

        writeTasks();
        removeNotification();
    }

    private void writeTasks() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (!audioDownloadPq.isEmpty()) {
            String json = new Gson().toJson(audioDownloadPq);
            editor.putString("tasks_json", json).apply();
        } else {
            editor.putString("tasks_json", null).apply();
        }
    }

    private void readTasks() {
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

    private Observable getDownloadTasksObservable() {
        return Observable.<DownloadingAudioBundle>create(emitter -> {
            Audio taskAudio = audioDownloadPq.peek();
            File saveTo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), taskAudio.getTitle());
            String saveToMd5Hash = Utils.getMd5Hash(Utils.getFileBytes(saveTo));

            if (!Objects.equals(saveToMd5Hash, taskAudio.getMd5Hash())) {
                //if (saveTo.exists())
                //  saveTo.delete();

                buildCurrentStateNotification(taskAudio);
                restClient.downloadFile(taskAudio.getCloudData(), saveTo, new ProgressListener() {

                    @Override
                    public void updateProgress(long loaded, long total) {
                        emitter.onNext(new DownloadingAudioBundle(taskAudio, loaded));
                    }

                    @Override
                    public boolean hasCancelled() {
                        return false;
                    }

                });

            } else {
                audioDownloadPq.remove(taskAudio);
            }
            Log.i(toString(), "before onComplete");
            emitter.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(disposable -> Log.i(toString(), "onSubscribe"))
                .retry(throwable -> {
                    Log.i(toString(), throwable.toString());
                    removeNotification();
                    return throwable instanceof SSLException || throwable instanceof ServiceUnavailableException;
                })
                .doOnError(throwable -> {
                    audioDownloadPq.clear();
                    writeTasks();
                    removeNotification();
                    stopSelf();
                })
                .doOnNext(this::postAudioDownloadingState)
                .filter(DownloadingAudioBundle::isLoaded)
                .doOnNext(bundle -> {
                    Log.i(toString(), bundle.toString());
                    audioDownloadPq.remove(bundle.getTaskAudio());
                    postAudioDownloadedState(bundle);
                    Log.i(toString(), audioDownloadPq.toString());
                })
                .repeatUntil(audioDownloadPq::isEmpty)
                .doOnComplete(() -> {
                    Log.i(toString(), "onComplete");
                    buildDoneNotification();
                    stopSelf();
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


    private void postAudioPreparingState(DownloadingAudioBundle bundle) {
        rxBus.post(AudioStatus.STATUS_AUDIO_PREPARING, getStatePair(bundle));
    }

    private void postAudioDownloadingState(DownloadingAudioBundle bundle) {
        rxBus.post(AudioStatus.STATUS_AUDIO_DOWNLOADING, getStatePair(bundle));
    }

    private void postAudioDownloadedState(DownloadingAudioBundle bundle) {
        rxBus.post(AudioStatus.STATUS_AUDIO_DOWNLOADING, getStatePair(bundle));
    }

    private void buildCurrentStateNotification(Audio taskAudio) {
        notificationBuilder.setContentTitle(getResources().getString(R.string.downloading_from_disk))
                .setContentText(taskAudio.getTitle())
                .setSmallIcon(R.drawable.ic_music_rounded)
                .setOngoing(true)
                .setShowWhen(true)
                .setColor(getResources().getColor(R.color.colorDarkMagenta))
                .setPriority(Notification.PRIORITY_HIGH);

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void buildDoneNotification() {
        notificationManager.notify(NOTIFICATION_ID, new Notification.Builder(this)
                .setContentTitle(getResources().getString(R.string.all_downloads_complete))
                .setSmallIcon(R.drawable.ic_music_rounded)
                .setOngoing(false)
                .setShowWhen(true)
                .setColor(getResources().getColor(R.color.colorDarkMagenta))
                .setPriority(Notification.PRIORITY_HIGH)
                .build());
    }

    private void removeNotification() {
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private Pair<String, Pair<Long, Long>> getStatePair(String md5Hash, long loaded, long total) {
        return new Pair<>(md5Hash, new Pair<>(loaded, total));
    }

    private Pair<String, Pair<Long, Long>> getStatePair(String md5Hash, Pair<Long, Long> downloadPair) {
        return new Pair<>(md5Hash, downloadPair);
    }

    private Pair<String, Pair<Long, Long>> getStatePair(DownloadingAudioBundle bundle) {
        return new Pair<>(bundle.getTaskAudio().getMd5Hash(), new Pair<>(bundle.getLoaded(), bundle.getTaskAudio().getSize()));
    }

    public static class DownloadingAudioBundle {
        private Audio taskAudio;
        private long loaded;

        public DownloadingAudioBundle(){
            taskAudio = new Audio();
            loaded = 0;
        }

        @Override
        public String toString() {
            return String.valueOf(taskAudio.getTitle())
                    + " loaded " +
                    String.valueOf(loaded)
                    + " of " +
                    taskAudio.getSize();

        }

        public DownloadingAudioBundle(Audio taskAudio, long loaded) {
            this.taskAudio = taskAudio;
            this.loaded = loaded;
        }

        public Audio getTaskAudio() {
            return taskAudio;
        }

        public void setTaskAudio(Audio taskAudio) {
            this.taskAudio = taskAudio;
        }

        public long getLoaded() {
            return loaded;
        }

        public void setLoaded(long loaded) {
            this.loaded = loaded;
        }

        public boolean isLoaded(){
            return taskAudio.getSize() == loaded;
        }
    }
}

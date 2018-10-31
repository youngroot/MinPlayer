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

import javax.net.ssl.SSLException;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit.RestAdapter;

public class AudioDownloadService extends Service {
    private static final int NOTIFICATION_ID = 102;
    public static final String EXTRA_AUDIO_SIZE = "extra_audio_size";
    public static final String EXTRA_AUDIO_PATH = "extra_audio_path";
    public static final String EXTRA_MD5_HASH = "extra_md5_hash";
    public static final String EXTRA_AUDIO_TITLE = "extra_audio_title";

    private SharedPreferences sharedPreferences;
    private RxSharedPreferences rxPreferences;
    private PriorityBlockingQueue<Audio> audioDownloadPq;
    private Audio taskAudio;
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
        notificationBuilder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_music_rounded)
                .setOngoing(true)
                .setShowWhen(true)
                .setColor(getResources().getColor(R.color.colorDarkMagenta))
                .setPriority(Notification.PRIORITY_HIGH);

        readTasks();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("AudioDownloadService", "onStartCommand");
        if (intent == null && !audioDownloadPq.isEmpty()) {
            if (downloadTasksDisposable != null)
                downloadTasksDisposable.dispose();
            downloadTasksDisposable = getDownloadTasksDisposable();
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
                downloadTasksDisposable = getDownloadTasksDisposable();
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

    private Disposable getDownloadTasksDisposable() {
        return ReactiveNetwork.observeInternetConnectivity()
                .subscribeOn(Schedulers.io())
                .filter(b -> b)
                .take(1)
                .flatMap(connectivity -> Observable.<DownloadingAudioBundle>create(emitter -> {
                    taskAudio = audioDownloadPq.peek();

                    if(taskAudio != null) {
                        File saveTo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), taskAudio.getTitle());
                        String saveToMd5Hash = Utils.getMd5Hash(Utils.getFileBytes(saveTo));

                        if (!Objects.equals(saveToMd5Hash, taskAudio.getMd5Hash())) {
                            //buildCurrentStateNotification(taskAudio);
                            buildPreparingStateNotification(taskAudio);
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
                            postAudioDownloadedState(new DownloadingAudioBundle(taskAudio, taskAudio.getSize()));
                            audioDownloadPq.remove(taskAudio);
                        }
                    }
                    emitter.onComplete();
                }))
                .doOnSubscribe(disposable -> {
                    Log.i(toString(), "onSubscribe");
                    writeTasks();
                })
                .retry(throwable -> {
                    Log.e(toString() + "Error", throwable.getClass().getName() + " " + throwable.getMessage());

                    if(throwable instanceof IllegalArgumentException){
                        removeNotification();
                        postAudioCanceledState(new DownloadingAudioBundle(taskAudio, 0));
                        audioDownloadPq.remove(taskAudio);
                        return true;
                    }

                    if(throwable instanceof SSLException || throwable instanceof ServiceUnavailableException){
                        buildWaitingForInternetNotification();
                        return true;
                    }

                    return false;
                })
                .doOnNext(this::postAudioDownloadingState)
                .doOnNext(bundle -> {
                    int loadedPercentage = Utils.getLoadedPercentage(bundle.getLoaded(), bundle.getTaskAudio().getSize());
                    if(loadedPercentage % 10 == 0)
                        buildCurrentStateNotification(bundle.getTaskAudio(), loadedPercentage);
                })
                .filter(DownloadingAudioBundle::isLoaded)
                .doOnNext(bundle -> buildCurrentStateNotification(bundle.getTaskAudio(), 100))
                .doOnComplete(() -> Log.i(toString(),"onComplete " + String.valueOf(taskAudio)))
                .repeatUntil(audioDownloadPq::isEmpty)
                .doOnComplete(() -> {
                    Log.i(toString(), "onComplete final");
                    removeNotification();
                    buildDoneNotification();
                    stopSelf();
                })
                .subscribe(bundle ->{
                    Log.i(toString(), "subscribe " + bundle.toString());
                    audioDownloadPq.remove(bundle.getTaskAudio());
                    postAudioDownloadedState(bundle);
                    writeTasks();

                }, throwable -> {
                    removeNotification();
                    postAudioCanceledState(new DownloadingAudioBundle(taskAudio, 0));
                    audioDownloadPq.clear();
                    writeTasks();
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
        rxBus.post(AudioStatus.STATUS_AUDIO_PREPARING, bundle);
    }

    private void postAudioDownloadingState(DownloadingAudioBundle bundle) {
        rxBus.post(AudioStatus.STATUS_AUDIO_DOWNLOADING, bundle);
    }

    private void postAudioDownloadedState(DownloadingAudioBundle bundle) {
        rxBus.post(AudioStatus.STATUS_AUDIO_DOWNLOADED, bundle);
    }

    private void postAudioCanceledState(DownloadingAudioBundle bundle){
        rxBus.post(AudioStatus.STATUS_AUDIO_CANCELED, bundle);
    }

    private void buildCurrentStateNotification(Audio taskAudio) {
        notificationBuilder.setContentTitle(getResources().getString(R.string.downloading_from_disk))
                .setContentText(taskAudio.getTitle());

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void buildPreparingStateNotification(Audio taskAudio){
        notificationBuilder.setContentTitle(getResources().getString(R.string.downloading_from_disk))
                .setContentText(taskAudio.getTitle())
                .setProgress(100, 0, true);

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void buildWaitingForInternetNotification(){
        notificationManager.notify(NOTIFICATION_ID, new Notification.Builder(this)
                .setContentTitle(getResources().getString(R.string.waiting_for_internet_connection))
                .setSmallIcon(R.drawable.ic_music_rounded)
                .setOngoing(true)
                .setShowWhen(true)
                .setProgress(100,0,true)
                .setColor(getResources().getColor(R.color.colorDarkMagenta))
                .setPriority(Notification.PRIORITY_HIGH)
                .build());
    }

    private void buildCurrentStateNotification(Audio taskAudio, int loadedPercentage) {
        notificationBuilder.setContentTitle(getResources().getString(R.string.downloading_from_disk))
                .setContentText(taskAudio.getTitle())
                .setProgress(100, loadedPercentage, false);

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }


    private void buildDoneNotification() {
        notificationManager.notify(NOTIFICATION_ID, new Notification.Builder(this)
                .setContentTitle(getResources().getString(R.string.all_tracks_downloaded))
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

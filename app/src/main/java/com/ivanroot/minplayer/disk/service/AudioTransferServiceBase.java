package com.ivanroot.minplayer.disk.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.activity.TokenActivity;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.disk.AudioStateBundle;
import com.ivanroot.minplayer.disk.RestClientUtil;
import com.ivanroot.minplayer.disk.constants.IntentExtras;
import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.ProgressListener;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.ServerException;
import com.yandex.disk.rest.exceptions.http.ConflictException;
import com.yandex.disk.rest.exceptions.http.ServiceUnavailableException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.PriorityBlockingQueue;

import javax.net.ssl.SSLException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public abstract class AudioTransferServiceBase extends Service {

    protected String prefKeyTasks;
    protected int notificationId;
    protected int processingAudioStringId;
    protected int processedAudioStringId;
    protected int allAudioProcessedStringId;
    protected int canceledAudioStringId;

    protected SharedPreferences sharedPreferences;
    protected RxSharedPreferences rxPreferences;
    protected PriorityBlockingQueue<Audio> audioTasksPq;
    protected RestClient restClient;
    protected NotificationManager notificationManager;
    protected Notification.Builder notificationBuilder;
    protected Disposable tasksDisposable;
    protected Disposable prefDisposable;
    protected Audio taskAudio;

    public AudioTransferServiceBase(@NonNull String prefKeyTasks,
                                    int notificationId,
                                    int processingAudioStringId,
                                    int processedAudioStringId,
                                    int allAudioProcessedStringId,
                                    int canceledAudioStringId) {

        this.prefKeyTasks = prefKeyTasks;
        this.notificationId = notificationId;
        this.processingAudioStringId = processingAudioStringId;
        this.processedAudioStringId = processedAudioStringId;
        this.allAudioProcessedStringId = allAudioProcessedStringId;
        this.canceledAudioStringId = canceledAudioStringId;
    }


    @Override
    public void onCreate() {
        sharedPreferences = getSharedPreferences(getPackageName() + "disk_service", MODE_PRIVATE);
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null && !audioTasksPq.isEmpty()) {
            if (tasksDisposable != null)
                tasksDisposable.dispose();
            tasksDisposable = getTasksDisposable();
            return START_STICKY;
        } else if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        Audio taskAudio = getAudioFromIntent(intent);

        if (isAudioValid(taskAudio) && !audioTasksPq.contains(taskAudio)) {
            audioTasksPq.add(taskAudio);
            postAudioPreparingState(taskAudio);

            if (tasksDisposable == null || tasksDisposable.isDisposed()) {
                tasksDisposable = getTasksDisposable();
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (prefDisposable != null)
            prefDisposable.dispose();

        if (tasksDisposable != null)
            tasksDisposable.dispose();

        writeTasks();
    }

    protected void writeTasks() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (!audioTasksPq.isEmpty()) {
            String json = new Gson().toJson(audioTasksPq);
            editor.putString(prefKeyTasks, json).apply();
        } else {
            editor.putString(prefKeyTasks, null).apply();
        }
    }

    protected void readTasks() {
        String json = sharedPreferences.getString(prefKeyTasks, null);
        if (json != null) {
            Type typeToken = new TypeToken<PriorityBlockingQueue<Audio>>() {
            }.getType();
            audioTasksPq = new Gson().fromJson(json, typeToken);
        } else {
            audioTasksPq = new PriorityBlockingQueue<>();
        }
    }

    protected Disposable getTasksDisposable() {
        return getInternetIsAvailableObservable()
                .flatMap(connectivity -> Observable.<AudioStateBundle>create(emitter -> {
                    taskAudio = audioTasksPq.peek();
                    if (taskAudio != null) {
                        buildPreparingStateNotification(taskAudio);
                        if (!onStartTransferTask(getTaskProgressListener(emitter, taskAudio), taskAudio)) {
                            postAudioProcessedState(taskAudio);
                            audioTasksPq.remove(taskAudio);
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

                    if (throwable instanceof IllegalArgumentException ||
                            throwable instanceof ConflictException) {
                        removeNotification();
                        postAudioCanceledState(taskAudio);
                        audioTasksPq.remove(taskAudio);
                        return true;
                    }

                    if (throwable instanceof SSLException ||
                            throwable instanceof ServiceUnavailableException ||
                            throwable instanceof UnknownHostException ||
                            throwable instanceof SocketTimeoutException) {
                        buildWaitingForInternetNotification();
                        return true;
                    }

                    return false;
                })
                .doOnNext(this::postAudioProcessingState)
                .doOnNext(bundle -> {
                    int loadedPercentage = bundle.getLoadedPercentage();
                    if (loadedPercentage % 10 == 0)
                        buildCurrentStateNotification(bundle);
                })
                .filter(AudioStateBundle::isLoaded)
                .doOnNext(this::buildCurrentStateNotification)
                .doOnComplete(() -> Log.i(toString(), "onComplete " + String.valueOf(taskAudio)))
                .repeatUntil(audioTasksPq::isEmpty)
                .doOnComplete(() -> {
                    Log.i(toString(), "onComplete final");
                    removeNotification();
                    buildDoneNotification();
                    postAllAudiosProcessedState();
                    stopSelf();
                })
                .subscribe(bundle -> {
                    Log.i(toString(), "subscribe " + bundle.toString());
                    audioTasksPq.remove(bundle.getTaskAudio());
                    postAudioProcessedState(bundle.getTaskAudio());
                    writeTasks();

                }, throwable -> {
                    removeNotification();
                    postAudioCanceledState(taskAudio);
                    audioTasksPq.clear();
                    writeTasks();
                    stopSelf();
                });
    }

    protected abstract boolean onStartTransferTask(ProgressListener progressListener, Audio taskAudio) throws ServerException, IOException;

    @NonNull
    public Audio getAudioFromIntent(@NonNull Intent intent) {
        long id = intent.getLongExtra(IntentExtras.EXTRA_AUDIO_ID, 0);
        long size = intent.getLongExtra(IntentExtras.EXTRA_AUDIO_SIZE, 0);
        String localData = intent.getStringExtra(IntentExtras.EXTRA_AUDIO_LOCAL_DATA);
        String cloudData = intent.getStringExtra(IntentExtras.EXTRA_AUDIO_CLOUD_DATA);
        String md5Hash = intent.getStringExtra(IntentExtras.EXTRA_MD5_HASH);
        String title = intent.getStringExtra(IntentExtras.EXTRA_AUDIO_TITLE);
        String album = intent.getStringExtra(IntentExtras.EXTRA_AUDIO_ALBUM);
        String artist = intent.getStringExtra(IntentExtras.EXTRA_AUDIO_ARTIST);
        String genre = intent.getStringExtra(IntentExtras.EXTRA_AUDIO_GENRE);
        String albumArtPath = intent.getStringExtra(IntentExtras.EXTRA_AUDIO_ALBUM_ART_PATH);

        return new Audio(id, size, localData, cloudData, md5Hash, title, album, artist, genre, albumArtPath);
    }

    @NonNull
    public static <T extends AudioTransferServiceBase> Intent getIntentFromAudio(Context context, Audio audio, Class<T> serviceClass) {
        Intent intent = new Intent(context, serviceClass);
        intent.putExtra(IntentExtras.EXTRA_AUDIO_ID, audio.getId());
        intent.putExtra(IntentExtras.EXTRA_AUDIO_SIZE, audio.getSize());
        intent.putExtra(IntentExtras.EXTRA_AUDIO_LOCAL_DATA, audio.getLocalData());
        intent.putExtra(IntentExtras.EXTRA_AUDIO_CLOUD_DATA, audio.getCloudData());
        intent.putExtra(IntentExtras.EXTRA_MD5_HASH, audio.getMd5Hash());
        intent.putExtra(IntentExtras.EXTRA_AUDIO_TITLE, audio.getTitle());
        intent.putExtra(IntentExtras.EXTRA_AUDIO_ALBUM, audio.getAlbum());
        intent.putExtra(IntentExtras.EXTRA_AUDIO_ARTIST, audio.getArtist());
        intent.putExtra(IntentExtras.EXTRA_AUDIO_GENRE,audio.getGenre());
        intent.putExtra(IntentExtras.EXTRA_AUDIO_ALBUM_ART_PATH,audio.getAlbumArtPath());
        return intent;
    }

    protected abstract boolean isAudioValid(@NonNull Audio candidate);

    protected abstract void postAudioPreparingState(@NonNull Audio taskAudio);

    protected abstract void postAudioProcessingState(@NonNull AudioStateBundle bundle);

    protected abstract void postAudioProcessedState(@NonNull Audio taskAudio);

    protected abstract void postAllAudiosProcessedState();

    protected abstract void postAudioCanceledState(@NonNull Audio taskAudio);

    protected void removeNotification() {
        notificationManager.cancel(notificationId);
    }

    protected void buildPreparingStateNotification(Audio taskAudio) {
        notificationBuilder.setContentTitle(getResources().getString(processingAudioStringId))
                .setContentText(getNotificationTitle(taskAudio))
                .setProgress(100, 0, true);

        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    protected void buildWaitingForInternetNotification() {
        notificationManager.notify(notificationId, new Notification.Builder(this)
                .setContentTitle(getResources().getString(R.string.waiting_for_internet_connection))
                .setSmallIcon(R.drawable.ic_music_rounded)
                .setOngoing(true)
                .setShowWhen(true)
                .setProgress(100, 0, true)
                .setColor(getResources().getColor(R.color.colorDarkMagenta))
                .setPriority(Notification.PRIORITY_HIGH)
                .build());
    }

    protected void buildDoneNotification() {
        notificationManager.notify(notificationId, new Notification.Builder(this)
                .setContentTitle(getResources().getString(allAudioProcessedStringId))
                .setSmallIcon(R.drawable.ic_music_rounded)
                .setOngoing(false)
                .setShowWhen(true)
                .setColor(getResources().getColor(R.color.colorDarkMagenta))
                .setPriority(Notification.PRIORITY_HIGH)
                .build());
    }

    protected void buildCurrentStateNotification(@NonNull AudioStateBundle bundle) {
        notificationBuilder.setContentTitle(getResources().getString(processingAudioStringId))
                .setContentText(getNotificationTitle(bundle.getTaskAudio()))
                .setProgress(100, bundle.getLoadedPercentage(), false);

        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    protected Observable<Boolean> getInternetIsAvailableObservable() {
        return ReactiveNetwork.observeInternetConnectivity()
                .subscribeOn(Schedulers.io())
                .filter(b -> b)
                .take(1);
    }

    protected ProgressListener getTaskProgressListener(ObservableEmitter<AudioStateBundle> emitter, Audio taskAudio) {
        return new ProgressListener() {
            @Override
            public void updateProgress(long loaded, long total) {
                emitter.onNext(new AudioStateBundle(taskAudio, loaded, total));
            }

            @Override
            public boolean hasCancelled() {
                return false;
            }
        };
    }

    protected abstract String getNotificationTitle(@NonNull Audio taskAudio);
}

package com.ivanroot.minplayer.disk;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.hwangjr.rxbus.Bus;
import com.hwangjr.rxbus.RxBus;
import com.ivanroot.minplayer.activity.TokenActivity;
import com.ivanroot.minplayer.utils.Pair;
import com.ivanroot.minplayer.utils.Utils;
import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.ProgressListener;
import com.yandex.disk.rest.RestClient;

import java.io.File;
import java.util.HashMap;

import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AudioDownloadService extends Service {
    public static final String EXTRA_AUDIO_PATH = "extra_audio_path";
    public static final String EXTRA_MD5_HASH = "extra_md5_hash";
    public static final String EXTRA_AUDIO_TITLE = "extra_audio_title";

    private RxSharedPreferences rxPreferences;
    private CompositeDisposable tasksDisposable = new CompositeDisposable();
    private Disposable prefDisposable;
    private RestClient restClient;
    private Bus rxBus = RxBus.get();

    @Override
    public void onCreate() {
        rxBus.register(this);
        rxPreferences = RxSharedPreferences.create(PreferenceManager.getDefaultSharedPreferences(this));
        prefDisposable = rxPreferences.getString(TokenActivity.PREF_ACCESS_TOKEN)
                .asObservable()
                .subscribe(token -> restClient = RestClientUtil.getInstance(new Credentials("", token)));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        String path = intent.getStringExtra(EXTRA_AUDIO_PATH);
        String md5Hash = intent.getStringExtra(EXTRA_MD5_HASH);
        String title = intent.getStringExtra(EXTRA_AUDIO_TITLE);

        if (path != null && md5Hash != null && title != null) {
            Disposable currTask = Completable.create(emitter -> {
                rxBus.post(AudioStatus.STATUS_AUDIO_PREPARING, md5Hash);
                File saveTo =  new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), title);
                String saveToMd5Hash = Utils.getMd5Hash(Utils.getFileBytes(saveTo));
                Log.i("Hashes", md5Hash + " " + saveToMd5Hash);
                if(!saveToMd5Hash.equals(md5Hash)) {
                    restClient.downloadFile(path, saveTo, new ProgressListener() {

                        @Override
                        public void updateProgress(long loaded, long total) {
                            HashMap<String, Pair<Long, Long>> state = new HashMap<>();
                            state.put(md5Hash, new Pair<>(loaded, total));
                            rxBus.post(AudioStatus.STATUS_AUDIO_DOWNLOADING, state);
                        }

                        @Override
                        public boolean hasCancelled() {
                            return false;
                        }
                    });
                }
                rxBus.post(AudioStatus.STATUS_AUDIO_DOWNLOADED, md5Hash);
                emitter.onComplete();
            }).subscribeOn(Schedulers.io())
                    .subscribe(()-> stopSelf(startId));
            tasksDisposable.add(currTask);
        }

        return START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        rxBus.unregister(this);
        if (prefDisposable != null)
            prefDisposable.dispose();
        tasksDisposable.dispose();
    }


}

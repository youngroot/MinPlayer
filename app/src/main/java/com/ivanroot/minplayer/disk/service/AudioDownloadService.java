package com.ivanroot.minplayer.disk.service;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.hwangjr.rxbus.Bus;
import com.hwangjr.rxbus.RxBus;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.disk.constants.AudioStatus;
import com.ivanroot.minplayer.disk.AudioStateBundle;
import com.ivanroot.minplayer.utils.Utils;
import com.yandex.disk.rest.ProgressListener;
import com.yandex.disk.rest.exceptions.ServerException;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class AudioDownloadService extends AudioTransferServiceBase {
    protected Bus rxBus = RxBus.get();

    public AudioDownloadService() {
        super("downloading_tasks_json",
                102,
                R.string.downloading_from_disk,
                R.string.download_completed,
                R.string.all_tracks_downloaded,
                R.string.download_canceled);
    }

    @Override
    public void onCreate() {
        Log.i("AudioDownloadService", "onCreate");
        rxBus.register(this);
        super.onCreate();
    }


    @Override
    public void onDestroy() {
        Log.i("AudioDownloadService", "onDestroy");
        rxBus.unregister(this);
        super.onDestroy();
    }

    @Override
    protected boolean onStartTransferTask(ProgressListener progressListener, Audio taskAudio) throws ServerException, IOException {
        File saveTo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), taskAudio.getTitle());
        String saveToMd5Hash = Utils.getMd5Hash(Utils.getFileBytes(saveTo));

        if (!Objects.equals(saveToMd5Hash, taskAudio.getMd5Hash())) {
            restClient.downloadFile(taskAudio.getCloudData(), saveTo, progressListener);
            return true;
        }
        return false;
    }

    @Override
    protected boolean isAudioValid(@NonNull Audio candidate) {
        return candidate.getSize() != 0 && candidate.getTitle() != null && candidate.getCloudData() != null && candidate.getMd5Hash() != null;
    }

    @Override
    protected void postAudioPreparingState(@NonNull Audio taskAudio) {
        rxBus.post(AudioStatus.STATUS_AUDIO_DOWNLOAD_PREPARING, taskAudio);
    }

    @Override
    protected void postAudioProcessingState(@NonNull AudioStateBundle bundle) {
        rxBus.post(AudioStatus.STATUS_AUDIO_DOWNLOADING, bundle);
    }

    @Override
    protected void postAudioProcessedState(@NonNull Audio taskAudio) {
        rxBus.post(AudioStatus.STATUS_AUDIO_DOWNLOADED, taskAudio);
    }

    @Override
    protected void postAllAudiosProcessedState() {
        rxBus.post(AudioStatus.STATUS_ALL_AUDIOS_DOWNLOADED, this);
    }

    @Override
    protected void postAudioCanceledState(@NonNull Audio taskAudio){
        rxBus.post(AudioStatus.STATUS_AUDIO_DOWNLOAD_CANCELED, taskAudio);
    }

    @Override
    protected String getNotificationTitle(@NonNull Audio taskAudio) {
        return taskAudio.getTitle();
    }

}

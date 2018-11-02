package com.ivanroot.minplayer.disk.service;


import android.support.annotation.NonNull;
import android.util.Log;

import com.hwangjr.rxbus.Bus;
import com.hwangjr.rxbus.RxBus;
import com.ivanroot.minplayer.R;
import com.ivanroot.minplayer.audio.Audio;
import com.ivanroot.minplayer.disk.AudioStateBundle;
import com.ivanroot.minplayer.disk.constants.AudioStatus;
import com.yandex.disk.rest.ProgressListener;
import com.yandex.disk.rest.exceptions.ServerException;
import com.yandex.disk.rest.json.Link;

import java.io.File;
import java.io.IOException;

public class AudioUploadService extends AudioTransferServiceBase{

    private static final String SERVICE_PATH = "/Music";
    private Bus rxBus = RxBus.get();

    public AudioUploadService(){
        super("uploading_tasks_json",
                103,
                R.string.uploading_to_disk,
                R.string.upload_completed,
                R.string.all_tracks_uploaded,
                R.string.upload_canceled);
    }

    @Override
    public void onCreate() {
        rxBus.register(this);
        super.onCreate();
    }

    @Override
    protected boolean onStartTransferTask(ProgressListener progressListener, Audio taskAudio) throws ServerException, IOException {
        String filename = new File(taskAudio.getLocalData()).getName();
        Link link = restClient.getUploadLink(SERVICE_PATH + "/" + filename, false);
        Log.i(toString(), taskAudio.getLocalData());
        restClient.uploadFile(link, true, new File(taskAudio.getLocalData()), progressListener);
        return true;
    }

    @Override
    public void onDestroy() {
        rxBus.unregister(this);
        super.onDestroy();
    }

    @Override
    protected boolean isAudioValid(@NonNull Audio candidate) {
        return candidate.getLocalData() != null;
    }

    @Override
    protected void postAudioPreparingState(@NonNull Audio taskAudio) {
        rxBus.post(AudioStatus.STATUS_AUDIO_UPLOAD_PREPARING, taskAudio);
    }

    @Override
    protected void postAudioProcessingState(@NonNull AudioStateBundle bundle) {
        rxBus.post(AudioStatus.STATUS_AUDIO_UPLOADING, bundle);
    }

    @Override
    protected void postAudioProcessedState(@NonNull Audio taskAudio) {
        rxBus.post(AudioStatus.STATUS_AUDIO_UPLOADED, taskAudio);
    }

    @Override
    protected void postAllAudiosProcessedState() {
        rxBus.post(AudioStatus.STATUS_ALL_AUDIOS_UPLOADED, this);
    }

    @Override
    protected void postAudioCanceledState(@NonNull Audio taskAudio) {
        rxBus.post(AudioStatus.STATUS_AUDIO_UPLOAD_CANCELED, taskAudio);
    }

    @Override
    protected String getNotificationTitle(@NonNull Audio taskAudio) {
        return (taskAudio.getArtist() != null ? taskAudio.getArtist() + " - " + taskAudio.getTitle() : taskAudio.getTitle());
    }
}

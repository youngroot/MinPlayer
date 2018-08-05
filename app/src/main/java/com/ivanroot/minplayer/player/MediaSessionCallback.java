package com.ivanroot.minplayer.player;

import android.content.Intent;
import android.media.session.MediaSession;
import android.support.annotation.NonNull;
import android.view.KeyEvent;

import java.util.ArrayList;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MediaSessionCallback extends MediaSession.Callback {

    private PlayerService playerService;
    private final MediaSessionCallback callback = this;
    private final int delay = 500;
    private ArrayList<KeyEvent> keyEvents;
    private Disposable keyEventDisposable;

    public MediaSessionCallback(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Override
    public boolean onMediaButtonEvent(@NonNull Intent mediaButtonIntent) {
        KeyEvent keyEvent = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

        if (keyEvent == null ||
                keyEvent.getKeyCode() != KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE &&
                        keyEvent.getKeyCode() != KeyEvent.KEYCODE_HEADSETHOOK)
            return super.onMediaButtonEvent(mediaButtonIntent);

        if (keyEventDisposable == null || keyEventDisposable.isDisposed()) {
            keyEvents = new ArrayList<>();
            keyEvents.add(keyEvent);
            keyEventDisposable = Completable.create(emitter -> {
                Thread.sleep(delay);
                emitter.onComplete();
            }).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnComplete(() -> {
                        int taps = 0;
                        for (int i = 1; i < keyEvents.size(); i++)
                            if (keyEvents.get(i - 1).getAction() == KeyEvent.ACTION_DOWN &&
                                    keyEvents.get(i).getAction() == KeyEvent.ACTION_UP)
                                taps++;

                        switch (taps) {
                            case 1:
                                if (playerService.isPlaying()) callback.onPause();
                                else callback.onPlay();
                                break;

                            case 2:
                                callback.onSkipToNext();
                                break;

                            case 3:
                                callback.onSkipToPrevious();
                                break;
                        }
                    }).subscribe();

        } else keyEvents.add(keyEvent);

        return true;
    }

    @Override
    public void onPlay() {
        super.onPlay();
        playerService.play();
    }

    @Override
    public void onPause() {
        super.onPause();
        playerService.pause();
    }

    @Override
    public void onSkipToNext() {
        super.onSkipToNext();
        playerService.playNextTrack();
    }

    @Override
    public void onSkipToPrevious() {
        super.onSkipToPrevious();
        playerService.playPrevTrack();
    }

    @Override
    public void onFastForward() {
        super.onFastForward();
        playerService.fastForward(250);
    }

    @Override
    public void onRewind() {
        super.onRewind();
        playerService.fastRewind(250);
    }

    @Override
    public void onStop() {
        super.onStop();
        playerService.stop();
    }

    @Override
    public void onSeekTo(long position) {
        playerService.seekTo((int) position);
        super.onSeekTo(position);
    }
}

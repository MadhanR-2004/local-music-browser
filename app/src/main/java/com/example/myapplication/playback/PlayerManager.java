package com.example.myapplication.playback;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.util.Util;
import androidx.media3.exoplayer.ExoPlayer;

public final class PlayerManager {
    private static volatile ExoPlayer playerInstance;
    private static volatile CrossfadeController crossfadeController;

    private PlayerManager() {}

    @NonNull
    public static ExoPlayer getPlayer(@NonNull Context context) {
        if (playerInstance == null) {
            synchronized (PlayerManager.class) {
                if (playerInstance == null) {
                    ExoPlayer exoPlayer = new ExoPlayer.Builder(context.getApplicationContext())
                            .setHandleAudioBecomingNoisy(true)
                            .setAudioAttributes(
                                    new AudioAttributes.Builder()
                                            .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                                            .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC)
                                            .build(),
                                    true
                            )
                            .build();
                    playerInstance = exoPlayer;
                    crossfadeController = new CrossfadeController(exoPlayer, 0);
                    exoPlayer.addListener(crossfadeController);
                }
            }
        }
        return playerInstance;
    }

    public static void setCrossfadeDurationMs(long durationMs) {
        if (crossfadeController != null) crossfadeController.setDurationMs(durationMs);
    }
}



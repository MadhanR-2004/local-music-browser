package com.example.myapplication.playback;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

public final class CrossfadeController implements Player.Listener {
    private final ExoPlayer player;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long durationMs;
    private float targetVolume = 1f;

    public CrossfadeController(@NonNull ExoPlayer player, long durationMs) {
        this.player = player;
        this.durationMs = durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    @Override
    public void onMediaItemTransition(@androidx.annotation.Nullable androidx.media3.common.MediaItem mediaItem, int reason) {
        if (durationMs <= 0) return;
        rampVolume(0f, 1f, durationMs);
    }

    private void rampVolume(final float from, final float to, final long duration) {
        player.setVolume(from);
        final long start = System.currentTimeMillis();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - start;
                float t = Math.min(1f, elapsed / (float) duration);
                float v = from + (to - from) * t;
                player.setVolume(v);
                if (t < 1f) handler.post(this);
            }
        });
    }
}



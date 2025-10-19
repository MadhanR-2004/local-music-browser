package com.example.myapplication.ui.nowplaying;

import android.content.ComponentName;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;

import com.example.myapplication.service.MusicService;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.CompletableFuture;

public final class MediaControllerHolder {
    private MediaControllerHolder() {}

    public static CompletableFuture<MediaController> create(@NonNull Context context) {
        CompletableFuture<MediaController> future = new CompletableFuture<>();
        // Ensure the service is started before resolving the session token
        android.content.Intent serviceIntent = new android.content.Intent(context, MusicService.class);
        try {
            ContextCompat.startForegroundService(context, serviceIntent);
        } catch (Throwable ignored) {}

        // Build SessionToken with the service's ComponentName declared in the manifest
        SessionToken token = new SessionToken(context, new ComponentName(context, MusicService.class));
        MediaController.Builder builder = new MediaController.Builder(context, token);
        ListenableFuture<MediaController> controllerFuture = builder.buildAsync();
        controllerFuture.addListener(() -> {
            try {
                future.complete(controllerFuture.get());
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        }, ContextCompat.getMainExecutor(context));
        return future;
    }
}

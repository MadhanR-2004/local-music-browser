package com.example.myapplication.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;

import java.util.concurrent.ExecutionException;

public class NotificationActionReceiver extends BroadcastReceiver {
    public static final String ACTION_PREV = "com.example.myapplication.action.PREV";
    public static final String ACTION_PLAY_PAUSE = "com.example.myapplication.action.PLAY_PAUSE";
    public static final String ACTION_NEXT = "com.example.myapplication.action.NEXT";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        SessionToken token = new SessionToken(context, new android.content.ComponentName(context, MusicService.class));
        com.google.common.util.concurrent.ListenableFuture<MediaController> controllerFuture = new MediaController.Builder(context, token).buildAsync();
        controllerFuture.addListener(() -> {
            try {
                MediaController controller = controllerFuture.get();
                String a = intent.getAction();
                if (ACTION_PREV.equals(a)) {
                    controller.seekToPreviousMediaItem();
                } else if (ACTION_PLAY_PAUSE.equals(a)) {
                    if (controller.isPlaying()) controller.pause(); else controller.play();
                } else if (ACTION_NEXT.equals(a)) {
                    controller.seekToNextMediaItem();
                }
            } catch (ExecutionException | InterruptedException e) {
                Log.e("NotificationActionReceiver", "Error getting MediaController", e);
            }
        }, androidx.core.content.ContextCompat.getMainExecutor(context));
    }
}

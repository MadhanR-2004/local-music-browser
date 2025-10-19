package com.example.myapplication.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;
import androidx.media.app.NotificationCompat.MediaStyle;

import com.example.myapplication.R;
import com.example.myapplication.playback.PlayerManager;

public class MusicService extends MediaSessionService {
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "music_playback";

    private MediaSession mediaSession;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        ExoPlayer player = PlayerManager.getPlayer(getApplicationContext());
        mediaSession = new MediaSession.Builder(this, player).build();
        startForeground(NOTIFICATION_ID, buildPersistentNotification());
    }

    @Override
    public MediaSession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }

    @Override
    public void onDestroy() {
        if (mediaSession != null) {
            mediaSession.release();
            mediaSession = null;
        }
        super.onDestroy();
    }

    private Notification buildPersistentNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Playing music")
                .setOngoing(true)
                .setSilent(true)
                .setStyle(new MediaStyle().setMediaSession(mediaSession.getSessionCompatToken()).setShowActionsInCompactView(1))
                .setContentIntent(mediaSession.getSessionActivity());

        int flags = android.app.PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= android.app.PendingIntent.FLAG_MUTABLE;
        }

        android.app.PendingIntent prev = android.app.PendingIntent.getBroadcast(
                this, 1, new android.content.Intent(NotificationActionReceiver.ACTION_PREV).setPackage(getPackageName()), flags);
        android.app.PendingIntent play = android.app.PendingIntent.getBroadcast(
                this, 2, new android.content.Intent(NotificationActionReceiver.ACTION_PLAY_PAUSE).setPackage(getPackageName()), flags);
        android.app.PendingIntent next = android.app.PendingIntent.getBroadcast(
                this, 3, new android.content.Intent(NotificationActionReceiver.ACTION_NEXT).setPackage(getPackageName()), flags);
        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_previous, "Prev", prev));
        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", play));
        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_next, "Next", next));

        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Playback controls and status");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }
}



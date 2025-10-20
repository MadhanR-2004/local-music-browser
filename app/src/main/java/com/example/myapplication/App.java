package com.example.myapplication;

import android.app.Application;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.core.content.ContextCompat;

import com.google.android.material.color.DynamicColors;

public class App extends Application {
    private static final String PREFS = "app_prefs";
    private static final String KEY_LAST_SCAN_EPOCH_MS = "last_scan_epoch_ms";
    private static final long AUTO_SCAN_MIN_INTERVAL_MS = 10 * 60 * 1000L; // 10 minutes

    @Override
    public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            InitialLoadInitializer.runAsync(this);
            markScanTriggered();
        }

        // Re-trigger autoscan when app returns to foreground (e.g., opened from Recents)
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override public void onActivityCreated(android.app.Activity activity, Bundle bundle) {}
            @Override public void onActivityStarted(android.app.Activity activity) {
                maybeTriggerAutoScan();
            }
            @Override public void onActivityResumed(android.app.Activity activity) {}
            @Override public void onActivityPaused(android.app.Activity activity) {}
            @Override public void onActivityStopped(android.app.Activity activity) {}
            @Override public void onActivitySaveInstanceState(android.app.Activity activity, Bundle bundle) {}
            @Override public void onActivityDestroyed(android.app.Activity activity) {}
        });
    }

    private void maybeTriggerAutoScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        long last = prefs.getLong(KEY_LAST_SCAN_EPOCH_MS, 0L);
        long now = System.currentTimeMillis();
        if (now - last >= AUTO_SCAN_MIN_INTERVAL_MS) {
            InitialLoadInitializer.runAsync(this);
            markScanTriggered();
        }
    }

    private void markScanTriggered() {
        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putLong(KEY_LAST_SCAN_EPOCH_MS, System.currentTimeMillis())
                .apply();
    }
}



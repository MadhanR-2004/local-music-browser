package com.example.myapplication;

import android.app.Application;
import android.os.AsyncTask;

import com.example.myapplication.data.DatabaseProvider;
import com.example.myapplication.media.MediaStoreScanner;
import com.example.myapplication.media.FolderScanner;
import android.net.Uri;

public final class InitialLoadInitializer {
    private InitialLoadInitializer() {}

    public static void runAsync(Application app) {
        AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                if (DatabaseProvider.get(app).songDao().getAll().isEmpty()) {
                    java.util.Set<String> folderUris = app.getSharedPreferences("app_prefs", Application.MODE_PRIVATE)
                            .getStringSet("music_folder_uris", new java.util.HashSet<>());
                    if (!folderUris.isEmpty()) {
                        java.util.List<Uri> uris = new java.util.ArrayList<>();
                        for (String s : folderUris) {
                            try { uris.add(Uri.parse(s)); } catch (Exception ignored) {}
                        }
                        if (!uris.isEmpty()) {
                            FolderScanner.scan(app, uris);
                        } else {
                            MediaStoreScanner.persist(app, MediaStoreScanner.scan(app));
                        }
                    } else {
                        MediaStoreScanner.persist(app, MediaStoreScanner.scan(app));
                    }
                }
            }
        });
    }
}



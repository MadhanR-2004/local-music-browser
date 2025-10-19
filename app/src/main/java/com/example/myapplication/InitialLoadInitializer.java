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
        runAsync(app, false);
    }
    
    public static void runAsync(Application app, boolean forceRescan) {
        AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                android.util.Log.d("InitialLoadInitializer", "Starting library scan (forceRescan=" + forceRescan + ")");
                
                if (!forceRescan && !DatabaseProvider.get(app).songDao().getAll().isEmpty()) {
                    android.util.Log.d("InitialLoadInitializer", "Database not empty, skipping scan");
                    return;
                }
                
                // Try path-based folders first
                java.util.Set<String> folderPaths = app.getSharedPreferences("app_prefs", Application.MODE_PRIVATE)
                        .getStringSet("music_folder_paths", new java.util.HashSet<>());
                
                android.util.Log.d("InitialLoadInitializer", "Found " + folderPaths.size() + " path-based folders");
                
                if (!folderPaths.isEmpty()) {
                    com.example.myapplication.media.PathBasedScanner.scan(app, folderPaths);
                    return;
                }
                
                // Try URI-based folders
                java.util.Set<String> folderUris = app.getSharedPreferences("app_prefs", Application.MODE_PRIVATE)
                        .getStringSet("music_folder_uris", new java.util.HashSet<>());
                        
                android.util.Log.d("InitialLoadInitializer", "Found " + folderUris.size() + " URI-based folders");
                
                if (!folderUris.isEmpty()) {
                    java.util.List<Uri> uris = new java.util.ArrayList<>();
                    for (String s : folderUris) {
                        try { uris.add(Uri.parse(s)); } catch (Exception ignored) {}
                    }
                    if (!uris.isEmpty()) {
                        FolderScanner.scan(app, uris);
                    } else {
                        android.util.Log.w("InitialLoadInitializer", "No valid URIs, falling back to MediaStore");
                        MediaStoreScanner.persist(app, MediaStoreScanner.scan(app));
                    }
                } else {
                    android.util.Log.w("InitialLoadInitializer", "No folders configured, scanning entire MediaStore");
                    MediaStoreScanner.persist(app, MediaStoreScanner.scan(app));
                }
            }
        });
    }
}



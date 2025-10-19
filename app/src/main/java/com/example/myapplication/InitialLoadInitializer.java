package com.example.myapplication;

import android.app.Application;
import android.os.AsyncTask;

import com.example.myapplication.data.DatabaseProvider;
import com.example.myapplication.data.entity.Song;
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
                    android.util.Log.d("InitialLoadInitializer", "Database is empty, starting music scan...");
                    
                    // Check for selected folder paths (from onboarding)
                    java.util.Set<String> folderPaths = app.getSharedPreferences("app_prefs", Application.MODE_PRIVATE)
                            .getStringSet("music_folder_paths", new java.util.HashSet<>());
                    
                    android.util.Log.d("InitialLoadInitializer", "Found folder paths: " + folderPaths);
                    
                    if (!folderPaths.isEmpty()) {
                        // Use MediaStoreScanner with folder filtering
                        java.util.List<Song> songs = MediaStoreScanner.scan(app);
                        android.util.Log.d("InitialLoadInitializer", "MediaStoreScanner found " + songs.size() + " songs");
                        MediaStoreScanner.persist(app, songs);
                        android.util.Log.d("InitialLoadInitializer", "Persisted " + songs.size() + " songs to database");
                    } else {
                        // Scan all music files
                        android.util.Log.d("InitialLoadInitializer", "No folder paths found, scanning all music files");
                        java.util.List<Song> songs = MediaStoreScanner.scan(app);
                        android.util.Log.d("InitialLoadInitializer", "MediaStoreScanner found " + songs.size() + " songs");
                        MediaStoreScanner.persist(app, songs);
                        android.util.Log.d("InitialLoadInitializer", "Persisted " + songs.size() + " songs to database");
                    }
                } else {
                    android.util.Log.d("InitialLoadInitializer", "Database already has songs, skipping scan");
                }
            }
        });
    }
}



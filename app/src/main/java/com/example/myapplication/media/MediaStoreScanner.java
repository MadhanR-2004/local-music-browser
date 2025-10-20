package com.example.myapplication.media;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import com.example.myapplication.data.AppDatabase;
import com.example.myapplication.data.DatabaseProvider;
import com.example.myapplication.data.entity.Song;

import java.util.ArrayList;
import java.util.List;

public final class MediaStoreScanner {
    private MediaStoreScanner() {}

        /**
         * Rescan selected folders for new and removed songs.
         * Adds new songs, removes deleted/moved songs.
         * @param context Application context
         * @param existingSongs List of songs already in DB
         * @return List of new songs found
         */
        public static List<Song> rescan(@NonNull Context context, @NonNull List<Song> existingSongs) {
            List<Song> scannedSongs = scan(context);
            java.util.Set<String> existingPaths = new java.util.HashSet<>();
            for (Song s : existingSongs) {
                existingPaths.add(s.path);
            }
            List<Song> newSongs = new ArrayList<>();
            for (Song s : scannedSongs) {
                if (!existingPaths.contains(s.path)) {
                    newSongs.add(s);
                }
            }
            // Remove deleted/moved songs
            List<Song> removedSongs = new ArrayList<>();
            java.util.Set<String> scannedPaths = new java.util.HashSet<>();
            for (Song s : scannedSongs) scannedPaths.add(s.path);
            for (Song s : existingSongs) {
                if (!scannedPaths.contains(s.path)) {
                    removedSongs.add(s);
                }
            }
            // Persist new songs
            persist(context, newSongs);
            // Remove deleted/moved songs from DB
            if (!removedSongs.isEmpty()) {
                try {
                    AppDatabase db = DatabaseProvider.get(context);
                    db.songDao().deleteAll(removedSongs);
                    android.util.Log.d("MediaStoreScanner", "Removed " + removedSongs.size() + " deleted/moved songs from database");
                } catch (Exception e) {
                    android.util.Log.e("MediaStoreScanner", "Error removing deleted/moved songs", e);
                }
            }
            return newSongs;
        }

    public static List<Song> scan(@NonNull Context context) {
        List<Song> songs = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String[] projection = new String[] {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.DATE_ADDED
        };

        // Get selected folder paths from SharedPreferences
        java.util.Set<String> folderPaths = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .getStringSet("music_folder_paths", new java.util.HashSet<>());
        
        android.util.Log.d("MediaStoreScanner", "Folder paths filter: " + folderPaths);

        try (Cursor cursor = resolver.query(uri, projection, selection, null, null)) {
            if (cursor == null) return songs;
            int titleIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            int artistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            int albumIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
            int durationIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int dataIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            int trackIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK);
            int yearIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR);
            int dateAddedIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED);
            int totalFiles = cursor.getCount();
            android.util.Log.d("MediaStoreScanner", "Found " + totalFiles + " music files in MediaStore");
            
            int processedCount = 0;
            int filteredCount = 0;
            
            while (cursor.moveToNext()) {
                processedCount++;
                String songPath = safe(cursor.getString(dataIdx));
                
                // If no folders selected, include all songs. Otherwise, filter by selected folders.
                if (folderPaths.isEmpty() || isInSelectedFolders(songPath, new ArrayList<>(folderPaths))) {
                    Song s = new Song();
                    s.title = safe(cursor.getString(titleIdx));
                    s.artist = safe(cursor.getString(artistIdx));
                    s.album = safe(cursor.getString(albumIdx));
                    s.durationMs = cursor.getLong(durationIdx);
                    s.path = songPath;
                    s.trackNumber = cursor.getInt(trackIdx);
                    s.year = cursor.getInt(yearIdx);
                    s.dateAddedEpochMs = cursor.getLong(dateAddedIdx) * 1000L;
                    songs.add(s);
                    filteredCount++;
                }
            }
            
            android.util.Log.d("MediaStoreScanner", "Processed " + processedCount + " files, filtered to " + filteredCount + " songs");
        }
        return songs;
    }

    // Helper to check if a song path is in any selected folder (including subfolders)
    private static boolean isInSelectedFolders(String songPath, List<String> folderPaths) {
        if (songPath == null) return false;
        for (String folder : folderPaths) {
            if (songPath.startsWith(folder)) return true;
        }
        return false;
    }

    // Helper to get folder path from URI
    private static String getFolderPath(Context context, Uri folderUri) {
        // Try to resolve the real path from the URI
        // This may need to be improved for SAF URIs
        if ("file".equals(folderUri.getScheme())) {
            return folderUri.getPath();
        }
        // For content:// URIs, try to query
        try {
            Cursor cursor = context.getContentResolver().query(folderUri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex("_data");
                if (idx != -1) {
                    String path = cursor.getString(idx);
                    cursor.close();
                    return path;
                }
                cursor.close();
            }
        } catch (Exception ignored) {}
        return null;
    }

    public static void persist(@NonNull Context context, @NonNull List<Song> songs) {
        if (songs.isEmpty()) {
            android.util.Log.w("MediaStoreScanner", "No songs to persist");
            return;
        }
        
        android.util.Log.d("MediaStoreScanner", "Persisting " + songs.size() + " songs to database...");
        
        try {
            AppDatabase db = DatabaseProvider.get(context);
            db.songDao().insertAll(songs);
            android.util.Log.d("MediaStoreScanner", "Successfully persisted " + songs.size() + " songs to database");
        } catch (Exception e) {
            android.util.Log.e("MediaStoreScanner", "Error persisting songs to database", e);
        }
    }

    private static String safe(String v) {
        return v == null ? "" : v;
    }
}



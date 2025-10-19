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

        // Get selected folder URIs from SharedPreferences
        java.util.Set<String> folderUris = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .getStringSet("music_folder_uris", new java.util.HashSet<>());
        List<String> folderPaths = new ArrayList<>();
        for (String uriStr : folderUris) {
            try {
                Uri folderUri = Uri.parse(uriStr);
                String path = getFolderPath(context, folderUri);
                if (path != null) folderPaths.add(path);
            } catch (Exception ignored) {}
        }

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
            while (cursor.moveToNext()) {
                String songPath = safe(cursor.getString(dataIdx));
                if (isInSelectedFolders(songPath, folderPaths)) {
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
                }
            }
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
        if (songs.isEmpty()) return;
        AppDatabase db = DatabaseProvider.get(context);
        db.songDao().insertAll(songs);
        // FTS content table auto-updates via contentEntity, but to be safe we can reinsert or rebuild if needed.
    }

    private static String safe(String v) {
        return v == null ? "" : v;
    }
}



package com.example.myapplication.media;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import com.example.myapplication.data.DatabaseProvider;
import com.example.myapplication.data.entity.Song;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Recursively scans SAF DocumentFile trees and extracts song metadata to persist in Room DB.
 */
public final class FolderScanner {
    private static final String TAG = "FolderScanner";
    private FolderScanner() {}

    public static List<Song> scan(@NonNull Context context, @NonNull List<Uri> roots) {
        List<Song> out = new ArrayList<>();
        for (Uri u : roots) {
            try {
                DocumentFile root = DocumentFile.fromTreeUri(context, u);
                if (root != null && root.exists() && root.isDirectory()) {
                    traverse(context, root, out);
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to scan uri: " + u + " -> " + e.getMessage());
            }
        }
        return out;
    }

    private static void traverse(Context context, DocumentFile dir, List<Song> out) {
        try {
            for (DocumentFile child : dir.listFiles()) {
                if (child.isDirectory()) {
                    traverse(context, child, out);
                    continue;
                }
                if (!child.isFile()) continue;
                String name = child.getName();
                if (name == null) continue;
                String lower = name.toLowerCase();
                if (!(lower.endsWith(".mp3") || lower.endsWith(".flac") || lower.endsWith(".m4a") || lower.endsWith(".wav") || lower.endsWith(".ogg") || lower.endsWith(".opus"))) continue;

                // Extract metadata
                Song s = extractMetadata(context, child);
                if (s != null) {
                    out.add(s);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "traverse error: " + e.getMessage());
        }
    }

    private static Song extractMetadata(Context context, DocumentFile file) {

        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(context, file.getUri());
            Song s = new Song();
            String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            s.title = title == null ? stripExtension(file.getName()) : title;
            s.artist = artist == null ? "Unknown Artist" : artist;
            s.album = album == null ? "Unknown Album" : album;
            s.path = file.getUri().toString();
            try { s.durationMs = duration == null ? 0 : Long.parseLong(duration); } catch (Throwable t) { s.durationMs = 0; }

            // Persist immediately to DB to avoid huge memory usage
            DatabaseProvider.get(context).songDao().insert(s);
            mmr.release();
            return s;
        } catch (Exception e) {
            Log.w(TAG, "extractMetadata failed for " + file.getUri() + ": " + e.getMessage());
            return null;
        }
    }

    private static String stripExtension(String name) {
        if (name == null) return "";
        int i = name.lastIndexOf('.');
        if (i <= 0) return name;
        return name.substring(0, i);
    }
}

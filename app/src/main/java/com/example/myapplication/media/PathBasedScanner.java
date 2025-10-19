package com.example.myapplication.media;

import android.content.Context;

import com.example.myapplication.data.entity.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class PathBasedScanner {
    private static final Set<String> AUDIO_EXT = new HashSet<>();
    static {
        AUDIO_EXT.add("mp3");
        AUDIO_EXT.add("flac");
        AUDIO_EXT.add("wav");
        AUDIO_EXT.add("m4a");
        AUDIO_EXT.add("aac");
        AUDIO_EXT.add("ogg");
        AUDIO_EXT.add("opus");
    }

    private PathBasedScanner() {}

    public static void scan(Context context, Set<String> folderPaths) {
        android.util.Log.d("PathBasedScanner", "Starting scan of " + folderPaths.size() + " folders");
        
        // Broadcast scan started
        ScanProgressBroadcaster.broadcastScanStarted(context, folderPaths.size());
        
        List<Song> collected = new ArrayList<>();
        int foldersScanned = 0;
        int foldersSkipped = 0;
        int currentFolderIndex = 0;
        
        for (String path : folderPaths) {
            currentFolderIndex++;
            android.util.Log.d("PathBasedScanner", "Scanning folder " + currentFolderIndex + "/" + folderPaths.size() + ": " + path);
            File dir = new File(path);
            
            if (!dir.exists()) {
                android.util.Log.w("PathBasedScanner", "Folder does not exist: " + path);
                foldersSkipped++;
                continue;
            }
            
            if (!dir.isDirectory()) {
                android.util.Log.w("PathBasedScanner", "Not a directory: " + path);
                foldersSkipped++;
                continue;
            }
            
            if (!dir.canRead()) {
                android.util.Log.w("PathBasedScanner", "Cannot read directory: " + path);
                foldersSkipped++;
                continue;
            }
            
            int beforeCount = collected.size();
            walk(context, dir, collected);
            int foundInFolder = collected.size() - beforeCount;
            android.util.Log.d("PathBasedScanner", "Found " + foundInFolder + " songs in " + path);
            foldersScanned++;
            
            // Broadcast progress
            ScanProgressBroadcaster.broadcastScanProgress(context, currentFolderIndex, folderPaths.size(), 
                                                         dir.getName(), foundInFolder, collected.size());
        }
        
        android.util.Log.i("PathBasedScanner", "Scan complete: " + collected.size() + " songs from " + 
                          foldersScanned + " folders (" + foldersSkipped + " skipped)");
        
        if (!collected.isEmpty()) {
            MediaStoreScanner.persist(context, collected);
        } else {
            android.util.Log.w("PathBasedScanner", "No songs found in any of the selected folders!");
        }
        
        // Broadcast scan complete
        ScanProgressBroadcaster.broadcastScanComplete(context, collected.size());
    }

    private static void walk(Context context, File dir, List<Song> out) {
        if (dir == null || !dir.canRead()) return;
        if (dir.isFile()) {
            if (isAudio(dir)) {
                Song s = extractSong(context, dir);
                if (s != null) out.add(s);
            }
            return;
        }
        File[] children = dir.listFiles();
        if (children == null) return;
        for (File f : children) {
            if (f.isDirectory()) {
                walk(context, f, out);
            } else if (isAudio(f)) {
                Song s = extractSong(context, f);
                if (s != null) out.add(s);
            }
        }
    }

    private static boolean isAudio(File file) {
        if (file == null || file.isDirectory()) return false;
        String name = file.getName();
        if (name == null) return false;
        int dot = name.lastIndexOf('.');
        if (dot < 0) return false;
        String ext = name.substring(dot + 1).toLowerCase(Locale.US);
        return AUDIO_EXT.contains(ext);
    }

    private static Song extractSong(Context context, File file) {
        android.media.MediaMetadataRetriever mmr = new android.media.MediaMetadataRetriever();
        try {
            mmr.setDataSource(file.getAbsolutePath());
            Song s = new Song();
            String title = safe(mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_TITLE));
            String artist = safe(mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST));
            String album = safe(mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ALBUM));
            String genre = safe(mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_GENRE));
            String durationStr = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
            String trackStr = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);
            String yearStr = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_YEAR);

            s.title = title.isEmpty() ? file.getName().replaceFirst("[.][^.]+$", "") : title;
            s.artist = artist.isEmpty() ? "Unknown Artist" : artist;
            s.album = album.isEmpty() ? "Unknown Album" : album;
            s.genre = genre.isEmpty() ? null : genre;
            s.durationMs = parseLong(durationStr);
            s.trackNumber = (int) parseLong(trackStr);
            s.year = (int) parseLong(yearStr);
            s.path = file.getAbsolutePath();
            s.dateAddedEpochMs = System.currentTimeMillis();
            
            android.util.Log.v("PathBasedScanner", "Extracted: " + s.title + " by " + s.artist);
            return s;
        } catch (Throwable e) {
            android.util.Log.e("PathBasedScanner", "Failed to extract metadata from: " + file.getAbsolutePath() + " - " + e.getMessage());
            return null;
        } finally {
            try { mmr.release(); } catch (Throwable ignored) {}
        }
    }

    private static String safe(String v) {
        return v == null ? "" : v.trim();
    }

    private static long parseLong(String v) {
        try { return Long.parseLong(v); } catch (Throwable t) { return 0L; }
    }
}


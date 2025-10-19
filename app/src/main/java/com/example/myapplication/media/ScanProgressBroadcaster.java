package com.example.myapplication.media;

import android.content.Context;
import android.content.Intent;

/**
 * Broadcasts scan progress updates to listeners using system broadcasts.
 */
public class ScanProgressBroadcaster {
    
    public static final String ACTION_SCAN_STARTED = "com.example.myapplication.SCAN_STARTED";
    public static final String ACTION_SCAN_PROGRESS = "com.example.myapplication.SCAN_PROGRESS";
    public static final String ACTION_SCAN_COMPLETE = "com.example.myapplication.SCAN_COMPLETE";
    
    public static final String EXTRA_TOTAL_FOLDERS = "total_folders";
    public static final String EXTRA_CURRENT_FOLDER = "current_folder";
    public static final String EXTRA_FOLDER_NAME = "folder_name";
    public static final String EXTRA_SONGS_FOUND = "songs_found";
    public static final String EXTRA_TOTAL_SONGS = "total_songs";
    
    private static ScanProgressListener listener = null;
    
    private ScanProgressBroadcaster() {}
    
    public static void setListener(ScanProgressListener listener) {
        ScanProgressBroadcaster.listener = listener;
    }
    
    public static void broadcastScanStarted(Context context, int totalFolders) {
        if (listener != null) {
            listener.onScanStarted(totalFolders);
        }
        android.util.Log.d("ScanProgress", "Scan started: " + totalFolders + " folders");
    }
    
    public static void broadcastScanProgress(Context context, int currentFolder, int totalFolders, 
                                            String folderName, int songsInFolder, int totalSongs) {
        if (listener != null) {
            listener.onScanProgress(currentFolder, totalFolders, folderName, songsInFolder, totalSongs);
        }
        android.util.Log.d("ScanProgress", "Scanning folder " + currentFolder + "/" + totalFolders + 
                          ": " + folderName + " (" + songsInFolder + " songs, " + totalSongs + " total)");
    }
    
    public static void broadcastScanComplete(Context context, int totalSongs) {
        if (listener != null) {
            listener.onScanComplete(totalSongs);
            listener = null; // Clear listener after completion
        }
        android.util.Log.i("ScanProgress", "Scan complete: " + totalSongs + " total songs");
    }
    
    public interface ScanProgressListener {
        void onScanStarted(int totalFolders);
        void onScanProgress(int currentFolder, int totalFolders, String folderName, int songsInFolder, int totalSongs);
        void onScanComplete(int totalSongs);
    }
}


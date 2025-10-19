package com.example.myapplication.ui.screens.onboarding

import com.example.myapplication.ui.screens.onboarding.FolderItem
import java.io.File

/**
 * Scans the device for folders that actually exist and might contain music
 */
fun scanForMusicFolders(): List<FolderItem> {
    val basePath = "/storage/emulated/0"
    val potentialFolders = listOf(
        "Music",
        "Download",
        "Downloads", 
        "Documents",
        "MyAudioEditor",
        "WhatsApp/Media/WhatsApp Audio",
        "WhatsApp/Media/WhatsApp Voice Notes",
        "files (online-audio-converter.com)",
        "records",
        "Recordings",
        "Audio",
        "Sounds",
        "Ringtones",
        "Notifications",
        "Alarms",
        "Podcasts",
        "Audiobooks",
        "DCIM",
        "Movies"
    )
    
    val foundFolders = mutableListOf<FolderItem>()
    
    // Check each potential folder
    for (folderName in potentialFolders) {
        val fullPath = "$basePath/$folderName"
        val folder = File(fullPath)
        
        if (folder.exists() && folder.isDirectory && folder.canRead()) {
            // Check if folder has any audio files (at least check if it's not empty)
            val hasContent = folder.listFiles()?.isNotEmpty() == true
            
            if (hasContent) {
                android.util.Log.d("FolderScanner", "Found folder: $fullPath")
                foundFolders.add(
                    FolderItem(
                        name = folderName,
                        path = fullPath,
                        isSelected = true // Select by default
                    )
                )
            }
        }
    }
    
    // Also scan root directory for any other folders with audio files
    try {
        val rootDir = File(basePath)
        rootDir.listFiles()?.forEach { file ->
            if (file.isDirectory && file.canRead()) {
                val folderName = file.name
                val fullPath = file.absolutePath
                
                // Skip already-added folders and system folders
                if (!foundFolders.any { it.path == fullPath } && 
                    !folderName.startsWith(".") && 
                    !folderName.equals("Android", ignoreCase = true)) {
                    
                    // Quick check: does it have any audio files?
                    if (hasAudioFiles(file)) {
                        android.util.Log.d("FolderScanner", "Found additional folder with audio: $fullPath")
                        foundFolders.add(
                            FolderItem(
                                name = folderName,
                                path = fullPath,
                                isSelected = true
                            )
                        )
                    }
                }
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("FolderScanner", "Error scanning root directory", e)
    }
    
    android.util.Log.d("FolderScanner", "Total folders found: ${foundFolders.size}")
    return foundFolders.sortedBy { it.name }
}

/**
 * Quick check if a folder contains audio files (checks up to 20 files)
 */
private fun hasAudioFiles(folder: File): Boolean {
    try {
        val audioExtensions = setOf("mp3", "flac", "m4a", "wav", "aac", "ogg", "opus", "wma")
        var checkedCount = 0
        val maxCheck = 20 // Don't scan too deeply
        
        folder.listFiles()?.forEach { file ->
            if (checkedCount >= maxCheck) return@forEach
            
            if (file.isFile) {
                val extension = file.extension.lowercase()
                if (extension in audioExtensions) {
                    return true
                }
                checkedCount++
            } else if (file.isDirectory && !file.name.startsWith(".")) {
                // Check one level deep
                file.listFiles()?.take(10)?.forEach { subFile ->
                    if (subFile.isFile) {
                        val extension = subFile.extension.lowercase()
                        if (extension in audioExtensions) {
                            return true
                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("FolderScanner", "Error checking for audio files in ${folder.path}", e)
    }
    return false
}




package com.example.myapplication.ui.viewmodel
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for Settings Screen
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    // Callback for launching folder picker (to be set by UI)
    var onLaunchFolderPicker: (() -> Unit)? = null

    fun launchFolderPicker() {
        onLaunchFolderPicker?.invoke()
    }

    fun startBackgroundRescan() {
        // Use WorkManager to start MusicScanWorker
        val workManager = androidx.work.WorkManager.getInstance(getApplication())
        val request = androidx.work.OneTimeWorkRequestBuilder<com.example.myapplication.worker.MusicScanWorker>()
            .build()
        workManager.enqueue(request)
        android.util.Log.d("SettingsViewModel", "Background rescan triggered via WorkManager")
    }
    
    private val sharedPrefs = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    
    // Theme settings
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
    
    private val _useDynamicColors = MutableStateFlow(true)
    val useDynamicColors: StateFlow<Boolean> = _useDynamicColors.asStateFlow()
    
    // Library settings
    private val _selectedFolderCount = MutableStateFlow(0)
    val selectedFolderCount: StateFlow<Int> = _selectedFolderCount.asStateFlow()
    
    // Playback settings
    private val _gaplessPlayback = MutableStateFlow(true)
    val gaplessPlayback: StateFlow<Boolean> = _gaplessPlayback.asStateFlow()
    
    private val _crossfadeDuration = MutableStateFlow(0)
    val crossfadeDuration: StateFlow<Int> = _crossfadeDuration.asStateFlow()
    
    init {
        loadSettings()
    }
    
    fun loadSettings() {
        _useDynamicColors.value = sharedPrefs.getBoolean("dynamic_colors", true)
        _gaplessPlayback.value = sharedPrefs.getBoolean("gapless_playback", true)
        _crossfadeDuration.value = sharedPrefs.getInt("crossfade_duration", 0)
        
        // Count selected folders
        val folders = sharedPrefs.getStringSet("music_folder_paths", emptySet())
        _selectedFolderCount.value = folders?.size ?: 0
    }
    
    fun toggleDynamicColors() {
        val newValue = !_useDynamicColors.value
        _useDynamicColors.value = newValue
        sharedPrefs.edit().putBoolean("dynamic_colors", newValue).apply()
    }
    
    fun toggleGaplessPlayback() {
        val newValue = !_gaplessPlayback.value
        _gaplessPlayback.value = newValue
        sharedPrefs.edit().putBoolean("gapless_playback", newValue).apply()
    }
    
    fun setCrossfadeDuration(seconds: Int) {
        _crossfadeDuration.value = seconds
        sharedPrefs.edit().putInt("crossfade_duration", seconds).apply()
    }
    
    fun clearCache() {
        // TODO: Implement cache clearing
        android.util.Log.d("SettingsViewModel", "Clear cache requested")
    }
    
    fun rescanLibrary() {
    startBackgroundRescan()
    }
}



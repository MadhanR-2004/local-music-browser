package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.DatabaseProvider
import com.example.myapplication.data.entity.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Now Playing Screen
 * Delegates to MusicPlayerViewModel for actual playback control
 */
class NowPlayingViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = DatabaseProvider.get(application)
    private val songDao = database.songDao()
    
    // Expose current song from music player
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()
    
    // Playback state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    // Progress (0.0 to 1.0)
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()
    
    // Current time in ms
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    // Shuffle state
    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()
    
    // Repeat mode (0 = off, 1 = all, 2 = one)
    private val _repeatMode = MutableStateFlow(0)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()
    
    // Liked state
    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked.asStateFlow()
    
    // Reference to music player view model (will be set from screen)
    private var musicPlayerViewModel: MusicPlayerViewModel? = null
    
    fun setMusicPlayerViewModel(viewModel: MusicPlayerViewModel) {
        musicPlayerViewModel = viewModel
        syncState()
    }
    
    private fun syncState() {
        musicPlayerViewModel?.let { player ->
            _currentSong.value = player.currentSong
            _isPlaying.value = player.isPlaying
            _progress.value = player.progress
            _currentPosition.value = player.currentPosition
        }
    }
    
    fun playPause() {
        musicPlayerViewModel?.togglePlayPause()
        _isPlaying.value = musicPlayerViewModel?.isPlaying ?: false
    }
    
    fun seekTo(position: Float) {
        val duration = _currentSong.value?.durationMs ?: 0
        val targetMs = (duration * position).toLong()
        musicPlayerViewModel?.seekTo(targetMs)
        _progress.value = position
        _currentPosition.value = targetMs
    }
    
    fun skipNext() {
        musicPlayerViewModel?.seekToNext()
    }
    
    fun skipPrevious() {
        musicPlayerViewModel?.seekToPrevious()
    }
    
    fun toggleShuffle() {
        _isShuffleEnabled.value = !_isShuffleEnabled.value
        // TODO: Implement shuffle in music service
        android.util.Log.d("NowPlayingViewModel", "Shuffle: ${_isShuffleEnabled.value}")
    }
    
    fun toggleRepeat() {
        _repeatMode.value = (_repeatMode.value + 1) % 3
        // TODO: Implement repeat in music service
        android.util.Log.d("NowPlayingViewModel", "Repeat mode: ${_repeatMode.value}")
    }
    
    fun toggleLike() {
        _isLiked.value = !_isLiked.value
        // TODO: Save to favorites in database
        viewModelScope.launch {
            _currentSong.value?.let { song ->
                android.util.Log.d("NowPlayingViewModel", "Toggle like for: ${song.title}, isLiked: ${_isLiked.value}")
                // TODO: Implement FavoriteSong entity insertion/deletion
            }
        }
    }
    
    // Call this periodically to sync state from music player
    fun updateState() {
        syncState()
    }
}


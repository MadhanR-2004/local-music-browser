package com.example.myapplication.ui.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.IBinder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.DatabaseProvider
import com.example.myapplication.data.entity.Song
import com.example.myapplication.service.MusicPlayerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Shared ViewModel for music playback across the app
 */
class MusicPlayerViewModel(application: Application) : AndroidViewModel(application) {
    
    private val context = application.applicationContext
    private val songDao = DatabaseProvider.get(application).songDao()
    
    // Service connection
    private var musicService: MusicPlayerService? = null
    private var isBound = false
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicPlayerService.MusicBinder
            musicService = binder.getService()
            isBound = true
            
            // Add listener
            musicService?.addListener(playbackListener)
            
            // Sync state from service when reconnecting
            musicService?.getCurrentSong()?.let { song ->
                _currentSong = song
                loadAlbumArt(song.path)
                android.util.Log.d(TAG, "Restored current song from service: ${song.title}")
            }
            
            // Sync playback state
            isPlaying = musicService?.isPlaying() ?: false
            
            // Sync shuffle and repeat state
            isShuffleEnabled = musicService?.getShuffleEnabled() ?: false
            repeatMode = musicService?.getRepeatMode() ?: 0
            
            // Start progress updates
            startProgressUpdates()
            
            android.util.Log.d(TAG, "Service connected and state synced (shuffle: $isShuffleEnabled, repeat: $repeatMode)")
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            musicService?.removeListener(playbackListener)
            isBound = false
            musicService = null
            android.util.Log.d(TAG, "Service disconnected")
        }
    }
    
    private val playbackListener = object : MusicPlayerService.PlaybackListener {
        override fun onSongChanged(song: Song) {
            _currentSong = song
            loadAlbumArt(song.path)
        }
        
        override fun onPlaybackStateChanged(isPlaying: Boolean) {
            this@MusicPlayerViewModel.isPlaying = isPlaying
        }
        
        override fun onError(message: String) {
            android.util.Log.e(TAG, "Playback error: $message")
        }
    }
    
    private var _currentSong by mutableStateOf<Song?>(null)
    val currentSong: Song? get() = _currentSong
    
    var isPlaying by mutableStateOf(false)
        private set
    
    var progress by mutableStateOf(0f)
        private set
    
    var currentPosition by mutableStateOf(0L)
        private set
    
    var albumArt by mutableStateOf<Bitmap?>(null)
        private set
    
    var isShuffleEnabled by mutableStateOf(false)
        private set
    
    var repeatMode by mutableStateOf(0) // 0 = off, 1 = all, 2 = one
        private set
    
    private var allSongs = listOf<Song>()
    private var currentSortedList = listOf<Song>()
    
    companion object {
        private const val TAG = "MusicPlayerViewModel"
    }
    
    init {
        bindService()
        loadFirstSong()
    }
    
    fun refreshSongs() {
        android.util.Log.d(TAG, "Refreshing songs...")
        loadFirstSong()
    }
    
    private fun loadFirstSong() {
        viewModelScope.launch {
            val songs = withContext(Dispatchers.IO) {
                // Load all songs without pre-sorting - let UI handle sorting
                songDao.getAll()
            }
            allSongs = songs
            android.util.Log.d(TAG, "Loaded ${songs.size} songs from database (unsorted)")
            if (songs.isNotEmpty()) {
                // Only set current song if we don't have one yet (e.g., fresh start)
                // If service already has a current song, don't override it or its playlist
                val serviceCurrentSong = musicService?.getCurrentSong()
                if (serviceCurrentSong != null) {
                    android.util.Log.d(TAG, "Service already has current song: ${serviceCurrentSong.title}, not overriding playlist")
                    // Service will restore it in onServiceConnected
                } else if (_currentSong == null) {
                    // Fresh start - set default playlist to all songs
                    android.util.Log.d(TAG, "No current song, setting default playlist and first song")
                    musicService?.setPlaylist(songs, 0)
                    setCurrentSong(songs[0])
                }
            } else {
                android.util.Log.w(TAG, "No songs in database")
            }
        }
    }
    
    fun setCurrentSong(song: Song) {
        _currentSong = song
        loadAlbumArt(song.path)
    }
    
    fun togglePlayPause() {
        if (isPlaying) {
            musicService?.pause()
        } else {
            if (_currentSong == null && allSongs.isNotEmpty()) {
                playSong(allSongs[0])
            } else {
                musicService?.play()
            }
        }
    }
    
    fun playSong(song: Song, customPlaylist: List<Song>? = null) {
        // Use custom playlist if provided (e.g., from album/artist), otherwise use all songs
        val playlist = customPlaylist ?: allSongs
        val songIndex = playlist.indexOfFirst { it.id == song.id }
        
        android.util.Log.d(TAG, "Playing song: ${song.title} at index $songIndex of ${playlist.size} songs (custom playlist: ${customPlaylist != null})")
        
        if (!isBound) {
            android.util.Log.w(TAG, "Service not bound, binding now...")
            bindService()
        }
        
        // Always set playlist with correct index to ensure proper sequence
        if (playlist.isNotEmpty() && songIndex >= 0) {
            musicService?.setPlaylist(playlist, songIndex)
            android.util.Log.d(TAG, "Set playlist with ${playlist.size} songs, starting at index $songIndex")
            currentSortedList = playlist
            
            // Only set original context if this is from a custom playlist (album/artist)
            if (customPlaylist != null) {
                musicService?.setOriginalContext(customPlaylist, song)
                android.util.Log.d(TAG, "Set original context to custom playlist with ${customPlaylist.size} songs")
            }
        } else {
            android.util.Log.w(TAG, "Cannot set playlist: playlist.size=${playlist.size}, songIndex=$songIndex")
        }
        
        musicService?.playSong(song)
        _currentSong = song
        loadAlbumArt(song.path)
    }
    
    fun seekToNext() {
        musicService?.playNext()
    }
    
    fun seekToPrevious() {
        // If more than 1 second into the song, restart it
        // Otherwise, go to previous song
        if (currentPosition > 2000) {
            seekTo(0)
        } else {
            musicService?.playPrevious()
        }
    }
    
    /**
     * Updates the queue with a new sorted list while maintaining the current song position
     * This is called when the user changes sort order while music is playing
     */
    fun updateQueueWithSortedList(sortedList: List<Song>) {
        val currentSong = _currentSong
        if (currentSong != null && sortedList.isNotEmpty()) {
            val newIndex = sortedList.indexOfFirst { it.id == currentSong.id }
            if (newIndex >= 0) {
                android.util.Log.d(TAG, "Updating queue with ${sortedList.size} songs, current song at index $newIndex")
                musicService?.setPlaylist(sortedList, newIndex)
                currentSortedList = sortedList
            } else {
                android.util.Log.w(TAG, "Current song not found in new sorted list, cannot update queue")
            }
        }
    }
    
    fun seekTo(positionMs: Long) {
        val wasPlaying = musicService?.isPlaying() ?: false
        musicService?.seekTo(positionMs)
        
        // If music was paused, resume playback after seeking
        if (!wasPlaying) {
            musicService?.play()
            isPlaying = true
            android.util.Log.d(TAG, "Resumed playback after seeking while paused")
        }
        
        // Force immediate progress update
        musicService?.let { service ->
            val duration = service.getDuration()
            if (duration > 0) {
                progress = (positionMs.toFloat() / duration).coerceIn(0f, 1f)
                currentPosition = positionMs
            }
        }
        android.util.Log.d(TAG, "Seeked to: $positionMs ms, wasPlaying: $wasPlaying")
        
        // Also update after a short delay to ensure ExoPlayer has processed the seek
        viewModelScope.launch {
            kotlinx.coroutines.delay(50)
            musicService?.let { service ->
                val actualPosition = service.getCurrentPosition()
                val duration = service.getDuration()
                if (duration > 0) {
                    progress = (actualPosition.toFloat() / duration).coerceIn(0f, 1f)
                    currentPosition = actualPosition
                }
            }
        }
    }
    
    fun toggleShuffle() {
        isShuffleEnabled = !isShuffleEnabled
        musicService?.setShuffleEnabled(isShuffleEnabled)
        android.util.Log.d(TAG, "Shuffle toggled: $isShuffleEnabled")
    }
    
    fun toggleRepeat() {
        repeatMode = (repeatMode + 1) % 3
        musicService?.setRepeatMode(repeatMode)
        android.util.Log.d(TAG, "Repeat mode: $repeatMode")
    }
    
    fun getQueue(): List<Song> {
        return musicService?.getQueue() ?: emptyList()
    }
    
    fun getCurrentQueueIndex(): Int {
        return musicService?.getCurrentIndex() ?: 0
    }
    
    fun getPlayNextQueue(): List<Song> {
        return musicService?.getPlayNextQueue() ?: emptyList()
    }
    
    fun getRegularQueue(): List<Song> {
        return musicService?.getRegularQueue() ?: emptyList()
    }
    
    fun getOriginalContext(): List<Song> {
        return musicService?.getOriginalContext() ?: emptyList()
    }
    
    // New custom shuffle methods that work with service
    fun setCustomShuffleEnabled(enabled: Boolean) {
        musicService?.setCustomShuffleEnabled(enabled)
        android.util.Log.d(TAG, "Custom shuffle set to: $enabled")
    }
    
    fun getCustomShuffleEnabled(): Boolean {
        return musicService?.getCustomShuffleEnabled() ?: false
    }
    
    /**
     * Gets the original context (album/playlist) in the correct sorted order
     * This should be called from UI components that need the sorted context
     */
    fun getSortedOriginalContext(sortedList: List<Song>): List<Song> {
        val originalContext = getOriginalContext()
        if (originalContext.isEmpty()) return emptyList()
        
        // Filter the sorted list to only include songs that are in the original context
        return sortedList.filter { song -> 
            originalContext.any { originalSong -> originalSong.id == song.id }
        }
    }
    
    /**
     * Gets the current sorted list from the service if available
     * This is used by the QueueScreen to display the correct order
     */
    fun getCurrentSortedList(): List<Song> {
        return currentSortedList
    }
    
    fun addToPlayNext(song: Song) {
        musicService?.addToPlayNext(song)
        android.util.Log.d(TAG, "Added to Play Next: ${song.title}")
    }
    
    fun addToQueue(song: Song) {
        musicService?.addToQueue(song)
        android.util.Log.d(TAG, "Added to Queue: ${song.title}")
    }
    
    fun movePlayNextItem(fromIndex: Int, toIndex: Int) {
        musicService?.movePlayNextItem(fromIndex, toIndex)
        android.util.Log.d(TAG, "Play Next item moved from $fromIndex to $toIndex")
    }
    
    fun moveRegularQueueItem(fromIndex: Int, toIndex: Int) {
        musicService?.moveRegularQueueItem(fromIndex, toIndex)
        android.util.Log.d(TAG, "Regular Queue item moved from $fromIndex to $toIndex")
    }
    
    fun removeFromPlayNext(index: Int) {
        musicService?.removeFromPlayNext(index)
        android.util.Log.d(TAG, "Removed from Play Next at index $index")
    }
    
    fun removeFromRegularQueue(index: Int) {
        musicService?.removeFromRegularQueue(index)
        android.util.Log.d(TAG, "Removed from Regular Queue at index $index")
    }
    
    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        musicService?.moveQueueItem(fromIndex, toIndex)
        android.util.Log.d(TAG, "Queue item moved from $fromIndex to $toIndex")
    }
    
    fun removeFromQueue(index: Int) {
        musicService?.removeFromQueue(index)
        android.util.Log.d(TAG, "Removed item at index $index from queue")
    }
    
    fun clearQueue() {
        musicService?.clearQueue()
        android.util.Log.d(TAG, "Queue cleared")
    }
    
    /**
     * Plays a specific song from the original context (album/playlist)
     * This will set the entire sorted context as the new queue
     */
    fun playSongFromContext(song: Song, sortedContext: List<Song>) {
        val songIndex = sortedContext.indexOfFirst { it.id == song.id }
        if (songIndex >= 0) {
            android.util.Log.d(TAG, "Playing song from context: ${song.title} at index $songIndex")
            musicService?.setPlaylist(sortedContext, songIndex)
            // Set the original context to preserve the album/playlist context
            musicService?.setOriginalContext(sortedContext, song)
            // Ensure playback actually starts and UI updates
            musicService?.playSong(song)
            _currentSong = song
            loadAlbumArt(song.path)
        } else {
            android.util.Log.w(TAG, "Song not found in sorted context: ${song.title}")
        }
    }
    
    fun updateProgress(position: Long, duration: Long) {
        currentPosition = position
        progress = if (duration > 0) position.toFloat() / duration else 0f
    }
    
    private fun bindService() {
        val intent = Intent(context, MusicPlayerService::class.java)
        context.startService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    
    private fun startProgressUpdates() {
        viewModelScope.launch(Dispatchers.Main) {
            while (isActive && isBound) {
                val service = musicService
                if (service != null) {
                    val duration = service.getDuration()
                    val position = service.getCurrentPosition()
                    if (duration > 0) {
                        progress = (position.toFloat() / duration).coerceIn(0f, 1f)
                        currentPosition = position
                    }
                }
                // Reduce UI churn to avoid jank; 250ms feels smooth enough for music progress
                delay(200)
            }
        }
    }
    
    private fun loadAlbumArt(filePath: String) {
        // Clear old album art immediately to prevent showing wrong art
        albumArt = null
        
        viewModelScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                extractAlbumArt(filePath)
            }
            albumArt = bitmap
            android.util.Log.d(TAG, "Album art loaded: ${bitmap != null}, size: ${bitmap?.width}x${bitmap?.height}")
        }
    }
    
    private fun extractAlbumArt(filePath: String): Bitmap? {
        val mmr = MediaMetadataRetriever()
        try {
            android.util.Log.d(TAG, "Loading album art from: $filePath")
            
            // Try to set data source
            mmr.setDataSource(filePath)
            
            // Extract embedded picture
            val art = mmr.embeddedPicture
            if (art != null) {
                android.util.Log.d(TAG, "Found embedded art, size: ${art.size} bytes")
                val decoded = BitmapFactory.decodeByteArray(art, 0, art.size)
                if (decoded != null) {
                    android.util.Log.d(TAG, "Album art decoded: ${decoded.width}x${decoded.height}")
                    return Bitmap.createScaledBitmap(decoded, 256, 256, true)
                }
            } else {
                android.util.Log.w(TAG, "No embedded art found in file")
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to load album art from $filePath: ${e.message}", e)
        } finally {
            try {
                mmr.release()
            } catch (ignored: Exception) {
            }
        }
        return null
    }
    
    fun formatDuration(ms: Long): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / (1000 * 60)) % 60
        val hours = (ms / (1000 * 60 * 60))
        
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        if (isBound) {
            musicService?.removeListener(playbackListener)
            context.unbindService(serviceConnection)
            isBound = false
        }
    }
}

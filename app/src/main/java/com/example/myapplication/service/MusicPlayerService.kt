package com.example.myapplication.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.myapplication.MainActivityCompose
import com.example.myapplication.R
import com.example.myapplication.data.entity.Song
import java.io.File

/**
 * Foreground service for music playback using ExoPlayer
 */
class MusicPlayerService : Service() {
    
    private val binder = MusicBinder()
    private var exoPlayer: ExoPlayer? = null
    private var currentSong: Song? = null
    private var playlist = mutableListOf<Song>()
    private var originalPlaylist = mutableListOf<Song>() // Store original order for shuffle toggle
    private var currentIndex = 0
    private var mediaSession: MediaSessionCompat? = null
    
    // Queue hierarchy: Play Next (high priority) + Regular Queue (low priority)
    private var playNextQueue = mutableListOf<Song>() // Songs to play immediately after current
    private var regularQueue = mutableListOf<Song>() // Songs added to end of queue
    private var originalContextPlaylist = mutableListOf<Song>() // Original album/playlist context
    private var lastPlayedContextIndex = 0 // Track where we left off in original context
    
    // Playback modes
    private var isShuffleEnabled = false
    private var repeatMode = 0 // 0 = off, 1 = all, 2 = one
    
    private val listeners = mutableListOf<PlaybackListener>()
    
    companion object {
        private const val TAG = "MusicPlayerService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "music_playback_channel"
        
        const val ACTION_PLAY = "com.example.myapplication.PLAY"
        const val ACTION_PAUSE = "com.example.myapplication.PAUSE"
        const val ACTION_NEXT = "com.example.myapplication.NEXT"
        const val ACTION_PREVIOUS = "com.example.myapplication.PREVIOUS"
        const val ACTION_STOP = "com.example.myapplication.STOP"
    }
    
    inner class MusicBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
        initializeMediaSession()
        initializePlayer()
    }
    
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> play()
            ACTION_PAUSE -> pause()
            ACTION_NEXT -> playNext()
            ACTION_PREVIOUS -> playPrevious()
            ACTION_STOP -> stopSelf()
        }
        return START_STICKY
    }
    
    private fun initializeMediaSession() {
        mediaSession = MediaSessionCompat(this, TAG).apply {
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
            setCallback(mediaSessionCallback)
            isActive = true
        }
        Log.d(TAG, "MediaSession initialized")
    }
    
    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            play()
        }
        
        override fun onPause() {
            pause()
        }
        
        override fun onSkipToNext() {
            playNext()
        }
        
        override fun onSkipToPrevious() {
            playPrevious()
        }
        
        override fun onSeekTo(pos: Long) {
            seekTo(pos)
        }
        
        override fun onStop() {
            stopSelf()
        }
    }
    
    private fun initializePlayer() {
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> {
                            notifyListeners { onPlaybackStateChanged(true) }
                            updateMediaSessionPlaybackState()
                        }
                        Player.STATE_ENDED -> {
                            handleSongEnded()
                        }
                    }
                }
                
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    notifyListeners { onPlaybackStateChanged(isPlaying) }
                    updateMediaSessionPlaybackState()
                    updateNotification()
                }
            })
        }
        Log.d(TAG, "ExoPlayer initialized")
    }
    
    private fun handleSongEnded() {
        when (repeatMode) {
            2 -> {
                // Repeat one - replay current song
                currentSong?.let { playSong(it) }
            }
            1 -> {
                // Repeat all - play next (will loop to start)
                playNext()
            }
            else -> {
                // No repeat - play next if not at end
                if (currentIndex < playlist.size - 1) {
                    playNext()
                } else {
                    // Check if we need to loop back to context
                    if (shouldLoopToContext()) {
                        loopToContextStart()
                        playNext()
                    } else {
                        // Reached end of everything, stop
                        pause()
                    }
                }
            }
        }
    }
    
    private fun shouldLoopToContext(): Boolean {
        // Loop if we have context songs and we're not at the very beginning
        return originalContextPlaylist.isNotEmpty() && lastPlayedContextIndex > 0
    }
    
    private fun loopToContextStart() {
        // Reset context index to start from beginning
        lastPlayedContextIndex = 0
        Log.d(TAG, "Looped back to context start - currentIndex: $currentIndex, playlist size: ${playlist.size}")
    }
    
    fun setPlaylist(songs: List<Song>, startIndex: Int = 0) {
        originalPlaylist.clear()
        originalPlaylist.addAll(songs)
        // Don't automatically set original context - it should be set explicitly
        playlist.clear()
        playlist.addAll(songs)
        currentIndex = startIndex.coerceIn(0, playlist.size - 1)
        lastPlayedContextIndex = startIndex.coerceIn(0, songs.size - 1) // Track where we started
        Log.d(TAG, "Playlist set: ${songs.size} songs, starting at index $currentIndex, context index: $lastPlayedContextIndex")
        
        // If shuffle is enabled, apply it to the new playlist
        if (isShuffleEnabled) {
            applyShuffle()
        }
    }
    
    /**
     * Sets the original context (album/playlist) separately from the main playlist
     * This preserves the original context even when playing songs from other sources
     */
    fun setOriginalContext(songs: List<Song>, currentSong: Song) {
        originalContextPlaylist.clear()
        originalContextPlaylist.addAll(songs)
        val contextIndex = songs.indexOfFirst { it.id == currentSong.id }
        lastPlayedContextIndex = contextIndex.coerceIn(0, songs.size - 1)
        Log.d(TAG, "Original context set: ${songs.size} songs, current song at context index: $lastPlayedContextIndex")
    }
    
    fun setShuffleEnabled(enabled: Boolean) {
        if (isShuffleEnabled == enabled) return
        
        isShuffleEnabled = enabled
        Log.d(TAG, "Shuffle ${if (enabled) "enabled" else "disabled"}")
        
        if (enabled) {
            applyShuffle()
        } else {
            // Restore original order but maintain current song
            val currentSongId = currentSong?.id
            playlist.clear()
            playlist.addAll(originalPlaylist)
            currentIndex = if (currentSongId != null) {
                playlist.indexOfFirst { it.id == currentSongId }.coerceAtLeast(0)
            } else {
                0
            }
        }
    }
    
    private fun applyShuffle() {
        val currentSongId = currentSong?.id
        val shuffled = playlist.shuffled().toMutableList()
        
        // Move current song to front if it exists
        if (currentSongId != null) {
            val currentSongIndex = shuffled.indexOfFirst { it.id == currentSongId }
            if (currentSongIndex > 0) {
                val song = shuffled.removeAt(currentSongIndex)
                shuffled.add(0, song)
            }
        }
        
        playlist.clear()
        playlist.addAll(shuffled)
        currentIndex = 0
        Log.d(TAG, "Playlist shuffled, ${playlist.size} songs")
    }
    
    fun setRepeatMode(mode: Int) {
        repeatMode = mode.coerceIn(0, 2)
        Log.d(TAG, "Repeat mode set to $repeatMode")
    }
    
    fun getShuffleEnabled(): Boolean = isShuffleEnabled
    fun getRepeatMode(): Int = repeatMode
    
    fun playSong(song: Song, updateIndex: Boolean = true) {
        val oldIndex = currentIndex
        currentSong = song
        
        if (updateIndex) {
            // Find index by song ID (not object reference)
            val foundIndex = playlist.indexOfFirst { it.id == song.id }
            if (foundIndex >= 0) {
                currentIndex = foundIndex
                Log.d(TAG, "playSong: Found song at index $foundIndex (was $oldIndex): ${song.title} (id=${song.id})")
            } else {
                Log.w(TAG, "playSong: Song not found in playlist: ${song.title} (id=${song.id}), keeping current index: $currentIndex")
                Log.w(TAG, "playSong: Available song IDs: ${playlist.map { it.id }}")
            }
        } else {
            Log.d(TAG, "playSong: Playing song at current index $currentIndex: ${song.title} (id=${song.id})")
        }
        
        Log.d(TAG, "Playing song: ${song.title} from ${song.path}")
        
        try {
            val file = File(song.path)
            if (!file.exists()) {
                Log.e(TAG, "Song file does not exist: ${song.path}")
                notifyListeners { onError("File not found: ${song.title}") }
                return
            }
            
            val mediaItem = MediaItem.fromUri(Uri.fromFile(file))
            exoPlayer?.apply {
                setMediaItem(mediaItem)
                prepare()
                play()
            }
            
            // Update MediaSession metadata
            updateMediaSessionMetadata(song)
            updateMediaSessionPlaybackState()
            
            notifyListeners { onSongChanged(song) }
            startForeground(NOTIFICATION_ID, createNotification())
            
        } catch (e: Exception) {
            Log.e(TAG, "Error playing song", e)
            notifyListeners { onError("Failed to play: ${song.title}") }
        }
    }
    
    fun play() {
        exoPlayer?.play()
        updateNotification()
    }
    
    fun pause() {
        exoPlayer?.pause()
        updateNotification()
    }
    
    fun playNext() {
        // Check if we have Play Next songs first
        if (playNextQueue.isNotEmpty()) {
            val song = playNextQueue.removeAt(0)
            Log.d(TAG, "playNext: Playing from Play Next queue: ${song.title}")
            playSong(song, updateIndex = false)
            return
        }
        
        // Check if we have Regular Queue songs
        if (regularQueue.isNotEmpty()) {
            val song = regularQueue.removeAt(0)
            Log.d(TAG, "playNext: Playing from Regular Queue: ${song.title}")
            playSong(song, updateIndex = false)
            return
        }
        
        // No queues left, follow circular navigation in original context
        if (originalContextPlaylist.isEmpty()) return
        
        lastPlayedContextIndex = (lastPlayedContextIndex + 1) % originalContextPlaylist.size
        val song = originalContextPlaylist[lastPlayedContextIndex]
        
        Log.d(TAG, "playNext: Following circular navigation - context index: $lastPlayedContextIndex")
        playSong(song, updateIndex = false)
    }
    
    fun playPrevious() {
        if (originalContextPlaylist.isEmpty()) {
            Log.w(TAG, "playPrevious: Original context is empty!")
            return
        }
        
        val oldIndex = lastPlayedContextIndex
        val oldSong = currentSong?.title ?: "null"
        
        // Simple circular navigation in original context only
        lastPlayedContextIndex = if (lastPlayedContextIndex - 1 < 0) {
            originalContextPlaylist.size - 1
        } else {
            lastPlayedContextIndex - 1
        }
        
        val newSong = originalContextPlaylist[lastPlayedContextIndex].title
        
        Log.d(TAG, "playPrevious: context index $oldIndex -> $lastPlayedContextIndex")
        Log.d(TAG, "playPrevious: '$oldSong' -> '$newSong'")
        
        // Play the song from original context
        playSong(originalContextPlaylist[lastPlayedContextIndex], updateIndex = false)
    }
    
    private fun shouldLoopToContextEnd(): Boolean {
        // Loop to context end if we have context songs and we're at the very beginning
        return originalContextPlaylist.isNotEmpty() && lastPlayedContextIndex > 0
    }
    
    private fun loopToContextEnd() {
        // Reset context index to the last song in the original context
        lastPlayedContextIndex = originalContextPlaylist.size - 1
        Log.d(TAG, "Looped back to context end")
    }
    
    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        // Force update listeners with new position
        notifyListeners { 
            // This will trigger progress update in ViewModel
        }
        Log.d(TAG, "Seeked to position: $positionMs")
    }
    
    fun getCurrentPosition(): Long {
        return exoPlayer?.currentPosition ?: 0
    }
    
    fun getDuration(): Long {
        return exoPlayer?.duration ?: 0
    }
    
    fun isPlaying(): Boolean {
        return exoPlayer?.isPlaying ?: false
    }
    
    fun getCurrentSong(): Song? = currentSong
    
    fun getQueue(): List<Song> = playlist.toList()
    
    fun getCurrentIndex(): Int = currentIndex
    
    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        if (fromIndex < 0 || fromIndex >= playlist.size || toIndex < 0 || toIndex >= playlist.size) {
            return
        }
        
        val song = playlist.removeAt(fromIndex)
        playlist.add(toIndex, song)
        
        // Update current index if affected
        currentIndex = when {
            currentIndex == fromIndex -> toIndex
            fromIndex < currentIndex && toIndex >= currentIndex -> currentIndex - 1
            fromIndex > currentIndex && toIndex <= currentIndex -> currentIndex + 1
            else -> currentIndex
        }
        
        Log.d(TAG, "Moved queue item from $fromIndex to $toIndex, new currentIndex: $currentIndex")
    }
    
    fun removeFromQueue(index: Int) {
        if (index < 0 || index >= playlist.size) return
        
        // Don't allow removing currently playing song
        if (index == currentIndex) return
        
        playlist.removeAt(index)
        
        // Adjust current index if necessary
        if (index < currentIndex) {
            currentIndex--
        }
        
        Log.d(TAG, "Removed item at index $index, new size: ${playlist.size}, currentIndex: $currentIndex")
    }
    
    fun clearQueue() {
        val current = currentSong
        playlist.clear()
        playNextQueue.clear()
        regularQueue.clear()
        if (current != null) {
            playlist.add(current)
            currentIndex = 0
        }
        Log.d(TAG, "Queue cleared, kept current song")
    }
    
    // Queue hierarchy management
    fun addToPlayNext(song: Song) {
        playNextQueue.add(song)
        rebuildPlaylist()
        Log.d(TAG, "Added to Play Next: ${song.title}, queue size: ${playNextQueue.size}")
    }
    
    fun addToQueue(song: Song) {
        regularQueue.add(song)
        rebuildPlaylist()
        Log.d(TAG, "Added to Queue: ${song.title}, queue size: ${regularQueue.size}")
    }
    
    fun removeFromPlayNext(index: Int) {
        if (index >= 0 && index < playNextQueue.size) {
            playNextQueue.removeAt(index)
            rebuildPlaylist()
            Log.d(TAG, "Removed from Play Next at index $index")
        }
    }
    
    fun removeFromRegularQueue(index: Int) {
        if (index >= 0 && index < regularQueue.size) {
            regularQueue.removeAt(index)
            rebuildPlaylist()
            Log.d(TAG, "Removed from Regular Queue at index $index")
        }
    }
    
    fun movePlayNextItem(fromIndex: Int, toIndex: Int) {
        if (fromIndex >= 0 && fromIndex < playNextQueue.size && 
            toIndex >= 0 && toIndex < playNextQueue.size) {
            val song = playNextQueue.removeAt(fromIndex)
            playNextQueue.add(toIndex, song)
            rebuildPlaylist()
            Log.d(TAG, "Moved Play Next item from $fromIndex to $toIndex")
        }
    }
    
    fun moveRegularQueueItem(fromIndex: Int, toIndex: Int) {
        if (fromIndex >= 0 && fromIndex < regularQueue.size && 
            toIndex >= 0 && toIndex < regularQueue.size) {
            val song = regularQueue.removeAt(fromIndex)
            regularQueue.add(toIndex, song)
            rebuildPlaylist()
            Log.d(TAG, "Moved Regular Queue item from $fromIndex to $toIndex")
        }
    }
    
    private fun rebuildPlaylist() {
        val current = currentSong
        val oldIndex = currentIndex
        playlist.clear()
        
        // Build playlist in the correct order for complex queue management
        // 1. Add all songs from original context in order
        playlist.addAll(originalContextPlaylist)
        
        // 2. Insert Play Next queue songs after current song position
        if (current != null) {
            val currentSongIndex = playlist.indexOfFirst { it.id == current.id }
            if (currentSongIndex >= 0) {
                // Insert Play Next songs after current song
                playlist.addAll(currentSongIndex + 1, playNextQueue)
            }
        }
        
        // 3. Insert Regular Queue songs after Play Next songs
        // Find where Play Next songs end and insert Regular Queue there
        val playNextEndIndex = if (current != null) {
            val currentSongIndex = playlist.indexOfFirst { it.id == current.id }
            if (currentSongIndex >= 0) {
                currentSongIndex + 1 + playNextQueue.size
            } else {
                playlist.size
            }
        } else {
            playlist.size
        }
        playlist.addAll(playNextEndIndex, regularQueue)
        
        // 4. Update currentIndex to the actual position of current song
        if (current != null) {
            currentIndex = playlist.indexOfFirst { it.id == current.id }.coerceAtLeast(0)
        } else {
            currentIndex = 0
        }
        
        Log.d(TAG, "Playlist rebuilt: ${playlist.size} total songs, current at index $currentIndex (was $oldIndex)")
        Log.d(TAG, "Playlist order: ${playlist.mapIndexed { index, song -> "$index: ${song.title}" }}")
        Log.d(TAG, "Play Next queue: ${playNextQueue.map { it.title }}")
        Log.d(TAG, "Regular queue: ${regularQueue.map { it.title }}")
    }
    
    fun getPlayNextQueue(): List<Song> = playNextQueue.toList()
    fun getRegularQueue(): List<Song> = regularQueue.toList()
    fun getOriginalContext(): List<Song> = originalContextPlaylist.toList()
    
    private fun cleanupPlayedSong() {
        val current = currentSong
        if (current == null) return
        
        // Remove from Play Next queue if it exists there
        val playNextIndex = playNextQueue.indexOfFirst { it.id == current.id }
        if (playNextIndex >= 0) {
            playNextQueue.removeAt(playNextIndex)
            Log.d(TAG, "Removed played song from Play Next: ${current.title}")
        }
        
        // Remove from Regular Queue if it exists there
        val regularIndex = regularQueue.indexOfFirst { it.id == current.id }
        if (regularIndex >= 0) {
            regularQueue.removeAt(regularIndex)
            Log.d(TAG, "Removed played song from Regular Queue: ${current.title}")
        }
        
        // Only update context position if we played directly from the original context
        // Queue songs (Play Next/Add to Queue) should not affect the original context progression
        val contextIndex = originalContextPlaylist.indexOfFirst { it.id == current.id }
        val wasPlayedFromQueue = playNextIndex >= 0 || regularIndex >= 0
        
        if (contextIndex >= 0 && !wasPlayedFromQueue) {
            // This song was played directly from the original context (not from queue)
            lastPlayedContextIndex = contextIndex + 1
            Log.d(TAG, "Played song directly from context at index: $contextIndex, updated context index to: $lastPlayedContextIndex")
        } else {
            Log.d(TAG, "Played song from queue or external source, context position unchanged: $lastPlayedContextIndex")
        }
        
        // Rebuild the playlist to reflect the changes
        rebuildPlaylist()
    }
    
    private fun advanceContextToNext() {
        // Simply advance to the next song in the original context sequence
        if (lastPlayedContextIndex < originalContextPlaylist.size) {
            lastPlayedContextIndex++
            Log.d(TAG, "Advanced context to next song at index: $lastPlayedContextIndex")
        } else {
            Log.d(TAG, "Reached end of context")
        }
    }
    
    private fun advanceContextToNextUnplayed() {
        // Find the next unplayed song in the original context
        // Skip any songs that are in Play Next or Regular Queue
        val queuedSongIds = (playNextQueue + regularQueue).map { it.id }.toSet()
        
        for (i in lastPlayedContextIndex until originalContextPlaylist.size) {
            val song = originalContextPlaylist[i]
            if (song.id !in queuedSongIds) {
                lastPlayedContextIndex = i
                Log.d(TAG, "Advanced context to next unplayed song at index: $i (${song.title})")
                return
            }
        }
        
        // If no unplayed songs found, we've reached the end
        Log.d(TAG, "No more unplayed songs in context, reached end")
    }
    
    fun addListener(listener: PlaybackListener) {
        listeners.add(listener)
    }
    
    fun removeListener(listener: PlaybackListener) {
        listeners.remove(listener)
    }
    
    private fun notifyListeners(action: PlaybackListener.() -> Unit) {
        listeners.forEach { it.action() }
    }
    
    private fun updateMediaSessionMetadata(song: Song) {
        val albumArt = try {
            extractAlbumArt(song.path)
        } catch (e: Exception) {
            null
        }
        
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.album)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.durationMs)
            .apply {
                if (albumArt != null) {
                    putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                }
            }
            .build()
        
        mediaSession?.setMetadata(metadata)
    }
    
    private fun updateMediaSessionPlaybackState() {
        val isPlaying = exoPlayer?.isPlaying ?: false
        val position = exoPlayer?.currentPosition ?: 0
        
        val stateBuilder = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_SEEK_TO
            )
            .setState(
                if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                position,
                1.0f
            )
        
        mediaSession?.setPlaybackState(stateBuilder.build())
    }
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Music Playback",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows currently playing music"
            setShowBadge(false)
        }
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    
    private fun createNotification(): Notification {
        val song = currentSong
        val isPlaying = exoPlayer?.isPlaying ?: false
        
        val contentIntent = Intent(this, MainActivityCompose::class.java).let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }
        
        val playPauseIntent = PendingIntent.getService(
            this, 0,
            Intent(this, MusicPlayerService::class.java).apply {
                action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
            },
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val nextIntent = PendingIntent.getService(
            this, 0,
            Intent(this, MusicPlayerService::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val previousIntent = PendingIntent.getService(
            this, 0,
            Intent(this, MusicPlayerService::class.java).apply { action = ACTION_PREVIOUS },
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(song?.title ?: "No song playing")
            .setContentText(song?.artist ?: "Unknown artist")
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentIntent(contentIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.ic_skip_previous, "Previous", previousIntent)
            .addAction(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                if (isPlaying) "Pause" else "Play",
                playPauseIntent
            )
            .addAction(R.drawable.ic_skip_next, "Next", nextIntent)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession?.sessionToken)
                .setShowActionsInCompactView(0, 1, 2))
        
        // Try to load album art
        song?.let {
            try {
                val albumArt = extractAlbumArt(it.path)
                if (albumArt != null) {
                    builder.setLargeIcon(albumArt)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading album art for notification", e)
            }
        }
        
        return builder.build()
    }
    
    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }
    
    private fun extractAlbumArt(path: String): Bitmap? {
        var retriever: MediaMetadataRetriever? = null
        try {
            retriever = MediaMetadataRetriever()
            retriever.setDataSource(path)
            val art = retriever.embeddedPicture
            if (art != null) {
                return android.graphics.BitmapFactory.decodeByteArray(art, 0, art.size)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting album art from $path", e)
        } finally {
            try {
                retriever?.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing MediaMetadataRetriever", e)
            }
        }
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mediaSession?.release()
        mediaSession = null
        exoPlayer?.release()
        exoPlayer = null
        Log.d(TAG, "Service destroyed")
    }
    
    interface PlaybackListener {
        fun onSongChanged(song: Song) {}
        fun onPlaybackStateChanged(isPlaying: Boolean) {}
        fun onError(message: String) {}
    }
}


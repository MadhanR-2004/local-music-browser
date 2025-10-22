package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.DatabaseProvider
import com.example.myapplication.data.entity.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Favorites/Liked Songs functionality
 * Manages liked songs state and operations
 */
class FavoritesViewModel(application: Application) : AndroidViewModel(application) {
    
    private val songDao = DatabaseProvider.get(application).songDao()
    private val favoriteDao = DatabaseProvider.get(application).favoriteDao()
    
    // State for liked songs
    private val _likedSongs = MutableStateFlow<List<Song>>(emptyList())
    val likedSongs: StateFlow<List<Song>> = _likedSongs.asStateFlow()
    
    // State for favorite status of songs
    private val _favoriteStatus = MutableStateFlow<Map<Long, Boolean>>(emptyMap())
    val favoriteStatus: StateFlow<Map<Long, Boolean>> = _favoriteStatus.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Sort order
    private val _sortOrder = MutableStateFlow(FavoritesSortOrder.RECENTLY_LIKED)
    val sortOrder: StateFlow<FavoritesSortOrder> = _sortOrder.asStateFlow()
    
    init {
        loadLikedSongs()
    }
    
    private fun loadLikedSongs() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val favoriteSongIds = favoriteDao.getFavoriteSongIds()
                val songs = songDao.getAll().filter { song -> 
                    song.id in favoriteSongIds 
                }
                
                _likedSongs.value = sortSongs(songs, _sortOrder.value)
                _isLoading.value = false
                
                // Update favorite status map
                val statusMap = mutableMapOf<Long, Boolean>()
                songs.forEach { song ->
                    statusMap[song.id] = true
                }
                _favoriteStatus.value = statusMap
                
                android.util.Log.d("FavoritesViewModel", "Loaded ${songs.size} liked songs")
            } catch (e: Exception) {
                android.util.Log.e("FavoritesViewModel", "Error loading liked songs", e)
                _isLoading.value = false
            }
        }
    }
    
    fun toggleFavorite(song: Song) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val isCurrentlyFavorite = favoriteDao.isFavorite(song.id) > 0
                
                if (isCurrentlyFavorite) {
                    // Unlike the song
                    favoriteDao.unlike(song.id)
                    android.util.Log.d("FavoritesViewModel", "Unliked song: ${song.title}")
                } else {
                    // Like the song
                    val favoriteSong = com.example.myapplication.data.entity.FavoriteSong().apply {
                        songId = song.id
                        likedAtEpochMs = System.currentTimeMillis()
                    }
                    favoriteDao.like(favoriteSong)
                    android.util.Log.d("FavoritesViewModel", "Liked song: ${song.title}")
                }
                
                // Update local state
                val currentStatus = _favoriteStatus.value.toMutableMap()
                currentStatus[song.id] = !isCurrentlyFavorite
                _favoriteStatus.value = currentStatus
                
                // Reload liked songs list
                loadLikedSongs()
                
            } catch (e: Exception) {
                android.util.Log.e("FavoritesViewModel", "Error toggling favorite", e)
            }
        }
    }
    
    fun isFavorite(songId: Long): Boolean {
        return _favoriteStatus.value[songId] ?: false
    }
    
    fun setSortOrder(order: FavoritesSortOrder) {
        _sortOrder.value = order
        _likedSongs.value = sortSongs(_likedSongs.value, order)
    }
    
    private fun sortSongs(songs: List<Song>, order: FavoritesSortOrder): List<Song> {
        return when (order) {
            FavoritesSortOrder.RECENTLY_LIKED -> {
                // Sort by liked timestamp (most recent first)
                songs.sortedByDescending { song ->
                    _favoriteStatus.value[song.id]?.let { isLiked ->
                        if (isLiked) {
                            // Get the liked timestamp from the database
                            // For now, use a simple approach
                            song.dateAddedEpochMs
                        } else {
                            0L
                        }
                    } ?: 0L
                }
            }
            FavoritesSortOrder.TITLE -> songs.sortedBy { it.title.lowercase() }
            FavoritesSortOrder.ARTIST -> songs.sortedBy { it.artist.lowercase() }
            FavoritesSortOrder.ALBUM -> songs.sortedBy { it.album.lowercase() }
            FavoritesSortOrder.DATE_ADDED -> songs.sortedByDescending { it.dateAddedEpochMs }
        }
    }
    
    fun refresh() {
        _isLoading.value = true
        loadLikedSongs()
    }
    
    fun clearAllFavorites() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Get all favorite song IDs
                val favoriteIds = favoriteDao.getFavoriteSongIds()
                
                // Remove all favorites
                favoriteIds.forEach { songId ->
                    favoriteDao.unlike(songId)
                }
                
                // Clear local state
                _favoriteStatus.value = emptyMap()
                _likedSongs.value = emptyList()
                
                android.util.Log.d("FavoritesViewModel", "Cleared all favorites")
            } catch (e: Exception) {
                android.util.Log.e("FavoritesViewModel", "Error clearing favorites", e)
            }
        }
    }
}

enum class FavoritesSortOrder {
    RECENTLY_LIKED, TITLE, ARTIST, ALBUM, DATE_ADDED
}

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
 * ViewModel for Library Screen
 * Manages Songs, Albums, Artists, Playlists, and Liked tabs
 */
class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val songDao = DatabaseProvider.get(application).songDao()
    
    // All songs
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()
    
    // Albums grouped data
    private val _albums = MutableStateFlow<List<AlbumGroup>>(emptyList())
    val albums: StateFlow<List<AlbumGroup>> = _albums.asStateFlow()
    
    // Artists grouped data
    private val _artists = MutableStateFlow<List<ArtistGroup>>(emptyList())
    val artists: StateFlow<List<ArtistGroup>> = _artists.asStateFlow()
    
    // Liked songs (TODO: Implement favorites system)
    private val _likedSongs = MutableStateFlow<List<Song>>(emptyList())
    val likedSongs: StateFlow<List<Song>> = _likedSongs.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Sort order
    private val _sortOrder = MutableStateFlow(SortOrder.ALPHABETICAL)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                
                val allSongs = songDao.getAll()
                _songs.value = sortSongs(allSongs, _sortOrder.value)
                
                // Group by album
                _albums.value = allSongs
                    .groupBy { it.album }
                    .map { (album, songs) ->
                        AlbumGroup(
                            name = album,
                            artist = songs.firstOrNull()?.artist ?: "Unknown",
                            songCount = songs.size,
                            songs = songs.sortedBy { it.trackNumber }
                        )
                    }
                    .sortedBy { it.name }
                
                // Group by artist
                _artists.value = allSongs
                    .groupBy { it.artist }
                    .map { (artist, songs) ->
                        ArtistGroup(
                            name = artist,
                            songCount = songs.size,
                            albumCount = songs.map { it.album }.distinct().size,
                            songs = songs
                        )
                    }
                    .sortedBy { it.name }
                
            } catch (e: Exception) {
                android.util.Log.e("LibraryViewModel", "Error loading library", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
        _songs.value = sortSongs(_songs.value, order)
    }
    
    private fun sortSongs(songs: List<Song>, order: SortOrder): List<Song> {
        return when (order) {
            SortOrder.ALPHABETICAL -> songs.sortedBy { it.title.lowercase() }
            SortOrder.ARTIST -> songs.sortedBy { it.artist.lowercase() }
            SortOrder.RECENTLY_ADDED -> songs.sortedByDescending { it.dateAddedEpochMs }
        }
    }
    
    fun refresh() {
        loadData()
    }
}

enum class SortOrder {
    ALPHABETICAL, ARTIST, RECENTLY_ADDED
}

data class AlbumGroup(
    val name: String,
    val artist: String,
    val songCount: Int,
    val songs: List<Song>,
    val albumArtPath: String? = songs.firstOrNull()?.path
)

data class ArtistGroup(
    val name: String,
    val songCount: Int,
    val albumCount: Int,
    val songs: List<Song>
)


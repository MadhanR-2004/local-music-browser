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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
    private val _sortOrder = MutableStateFlow(SortOrder.TITLE)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()
    
    init {
        // Reactive songs stream from Room
        viewModelScope.launch(Dispatchers.IO) {
            songDao.getAllFlow()
                .distinctUntilChanged()
                .collect { list ->
                    _songs.value = sortSongs(list, _sortOrder.value)
                    // Update derived groups when base changes
                    _albums.value = list
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
                    _artists.value = list
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
                    _isLoading.value = false
                }
        }
        // React to sort order changes
        viewModelScope.launch(Dispatchers.Default) {
            _sortOrder.collect { order ->
                _songs.value = sortSongs(_songs.value, order)
            }
        }
    }
    
    private fun loadData() { /* no-op: flows keep data live */ }
    
    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
        _songs.value = sortSongs(_songs.value, order)
    }
    
    private fun sortSongs(songs: List<Song>, order: SortOrder): List<Song> {
        return when (order) {
            SortOrder.TITLE -> songs.sortedBy { it.title.lowercase() }
            SortOrder.ARTIST -> songs.sortedBy { it.artist.lowercase() }
            SortOrder.ALBUM -> songs.sortedBy { it.album.lowercase() }
            SortOrder.DATE_ADDED -> songs.sortedByDescending { it.dateAddedEpochMs }
            SortOrder.DURATION -> songs.sortedByDescending { it.durationMs }
            SortOrder.PLAY_COUNT -> songs.sortedByDescending { it.playCount }
            SortOrder.RECENTLY_ADDED -> songs.sortedByDescending { it.dateAddedEpochMs }
        }
    }
    
    fun refresh() { /* no-op: flows update automatically */ }
}

enum class SortOrder {
    TITLE, ARTIST, ALBUM, DATE_ADDED, DURATION, PLAY_COUNT, RECENTLY_ADDED
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


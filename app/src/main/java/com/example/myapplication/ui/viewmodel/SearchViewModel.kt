package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.DatabaseProvider
import com.example.myapplication.data.entity.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for Search Screen
 * Implements real-time search with debouncing and filtering
 */
class SearchViewModel(application: Application) : AndroidViewModel(application) {
    
    private val songDao = DatabaseProvider.get(application).songDao()
    
    // Search query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // Selected filter
    private val _selectedFilter = MutableStateFlow(SearchFilter.ALL)
    val selectedFilter: StateFlow<SearchFilter> = _selectedFilter.asStateFlow()
    
    // Search results
    private val _searchResults = MutableStateFlow<SearchResults>(SearchResults.Empty)
    val searchResults: StateFlow<SearchResults> = _searchResults.asStateFlow()
    
    // Loading state
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    
    init {
        // Set up debounced search
        setupSearch()
    }
    
    @OptIn(FlowPreview::class)
    private fun setupSearch() {
        viewModelScope.launch {
            searchQuery
                .debounce(300) // Wait 300ms after user stops typing
                .distinctUntilChanged()
                .collect { query ->
                    performSearch(query)
                }
        }
    }
    
    fun updateQuery(query: String) {
        _searchQuery.value = query
        if (query.isEmpty()) {
            _searchResults.value = SearchResults.Empty
            _isSearching.value = false
        } else {
            _isSearching.value = true
        }
    }
    
    fun selectFilter(filter: SearchFilter) {
        _selectedFilter.value = filter
        if (searchQuery.value.isNotEmpty()) {
            performSearch(searchQuery.value)
        }
    }
    
    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            _searchResults.value = SearchResults.Empty
            _isSearching.value = false
            return
        }
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isSearching.value = true
                
                val allSongs = songDao.getAll()
                val filteredSongs = searchSongs(allSongs, query)
                
                when (selectedFilter.value) {
                    SearchFilter.ALL -> {
                        _searchResults.value = SearchResults.Success(
                            songs = filteredSongs.take(50),
                            albums = getAlbums(filteredSongs, query),
                            artists = getArtists(filteredSongs, query)
                        )
                    }
                    SearchFilter.SONGS -> {
                        _searchResults.value = SearchResults.Success(
                            songs = filteredSongs,
                            albums = emptyList(),
                            artists = emptyList()
                        )
                    }
                    SearchFilter.ALBUMS -> {
                        _searchResults.value = SearchResults.Success(
                            songs = emptyList(),
                            albums = getAlbums(filteredSongs, query),
                            artists = emptyList()
                        )
                    }
                    SearchFilter.ARTISTS -> {
                        _searchResults.value = SearchResults.Success(
                            songs = emptyList(),
                            albums = emptyList(),
                            artists = getArtists(filteredSongs, query)
                        )
                    }
                }
                
            } catch (e: Exception) {
                android.util.Log.e("SearchViewModel", "Search error", e)
                _searchResults.value = SearchResults.Error(e.message ?: "Search failed")
            } finally {
                _isSearching.value = false
            }
        }
    }
    
    private fun searchSongs(songs: List<Song>, query: String): List<Song> {
        val lowerQuery = query.lowercase()
        return songs.filter { song ->
            song.title.lowercase().contains(lowerQuery) ||
            song.artist.lowercase().contains(lowerQuery) ||
            song.album.lowercase().contains(lowerQuery) ||
            (song.genre?.lowercase()?.contains(lowerQuery) == true)
        }.sortedByDescending { song ->
            // Prioritize exact matches in title
            when {
                song.title.lowercase().startsWith(lowerQuery) -> 3
                song.artist.lowercase().startsWith(lowerQuery) -> 2
                song.album.lowercase().startsWith(lowerQuery) -> 1
                else -> 0
            }
        }
    }
    
    private fun getAlbums(songs: List<Song>, query: String): List<AlbumResult> {
        return songs
            .groupBy { it.album }
            .filter { it.key.lowercase().contains(query.lowercase()) }
            .map { (album, albumSongs) ->
                AlbumResult(
                    name = album,
                    artist = albumSongs.firstOrNull()?.artist ?: "Unknown",
                    songCount = albumSongs.size,
                    songs = albumSongs
                )
            }
            .sortedBy { it.name }
            .take(20)
    }
    
    private fun getArtists(songs: List<Song>, query: String): List<ArtistResult> {
        return songs
            .groupBy { it.artist }
            .filter { it.key.lowercase().contains(query.lowercase()) }
            .map { (artist, artistSongs) ->
                ArtistResult(
                    name = artist,
                    songCount = artistSongs.size,
                    albumCount = artistSongs.map { it.album }.distinct().size,
                    songs = artistSongs
                )
            }
            .sortedBy { it.name }
            .take(20)
    }
    
    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = SearchResults.Empty
        _selectedFilter.value = SearchFilter.ALL
    }
}

enum class SearchFilter {
    ALL, SONGS, ALBUMS, ARTISTS
}

sealed class SearchResults {
    object Empty : SearchResults()
    object Loading : SearchResults()
    data class Success(
        val songs: List<Song>,
        val albums: List<AlbumResult>,
        val artists: List<ArtistResult>
    ) : SearchResults()
    data class Error(val message: String) : SearchResults()
}

data class AlbumResult(
    val name: String,
    val artist: String,
    val songCount: Int,
    val songs: List<Song>
)

data class ArtistResult(
    val name: String,
    val songCount: Int,
    val albumCount: Int,
    val songs: List<Song>
)












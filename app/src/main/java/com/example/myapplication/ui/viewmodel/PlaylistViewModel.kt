package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.DatabaseProvider
import com.example.myapplication.data.entity.Playlist
import com.example.myapplication.data.entity.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Playlist management functionality
 * Manages playlists and their songs
 */
class PlaylistViewModel(application: Application) : AndroidViewModel(application) {
    
    private val playlistDao = DatabaseProvider.get(application).playlistDao()
    private val songDao = DatabaseProvider.get(application).songDao()
    
    // State for playlists
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()
    
    // State for current playlist songs
    private val _currentPlaylistSongs = MutableStateFlow<List<Song>>(emptyList())
    val currentPlaylistSongs: StateFlow<List<Song>> = _currentPlaylistSongs.asStateFlow()
    
    // State for current playlist
    private val _currentPlaylist = MutableStateFlow<Playlist?>(null)
    val currentPlaylist: StateFlow<Playlist?> = _currentPlaylist.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadPlaylists()
    }
    
    private fun loadPlaylists() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val playlists = playlistDao.getAll()
                _playlists.value = playlists
                _isLoading.value = false
                
                android.util.Log.d("PlaylistViewModel", "Loaded ${playlists.size} playlists")
            } catch (e: Exception) {
                android.util.Log.e("PlaylistViewModel", "Error loading playlists", e)
                _isLoading.value = false
            }
        }
    }
    
    fun createPlaylist(name: String, description: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val playlist = Playlist().apply {
                    this.name = name
                    this.description = description
                    this.createdAtEpochMs = System.currentTimeMillis()
                }
                
                val playlistId = playlistDao.insert(playlist)
                android.util.Log.d("PlaylistViewModel", "Created playlist: $name with ID: $playlistId")
                
                // Reload playlists
                loadPlaylists()
            } catch (e: Exception) {
                android.util.Log.e("PlaylistViewModel", "Error creating playlist", e)
            }
        }
    }
    
    
    fun renamePlaylist(playlist: Playlist, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Note: This would need to be implemented in PlaylistDao
                android.util.Log.d("PlaylistViewModel", "Renaming playlist: ${playlist.name} to $newName")
                
                // Reload playlists
                loadPlaylists()
            } catch (e: Exception) {
                android.util.Log.e("PlaylistViewModel", "Error renaming playlist", e)
            }
        }
    }
    
    fun loadPlaylistSongs(playlist: Playlist) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                android.util.Log.d("PlaylistViewModel", "Loading songs for playlist: ${playlist.name} (ID: ${playlist.id})")
                _currentPlaylist.value = playlist
                
                // Load songs from the playlist using PlaylistSongCrossRef
                val songsInPlaylist = playlistDao.getSongsInPlaylist(playlist.id)
                _currentPlaylistSongs.value = songsInPlaylist
                
                android.util.Log.d("PlaylistViewModel", "Loaded ${songsInPlaylist.size} songs for playlist: ${playlist.name}")
                songsInPlaylist.forEach { song ->
                    android.util.Log.d("PlaylistViewModel", "  - ${song.title} by ${song.artist}")
                }
            } catch (e: Exception) {
                android.util.Log.e("PlaylistViewModel", "Error loading playlist songs", e)
                e.printStackTrace()
            }
        }
    }
    
    fun addSongToPlaylist(playlist: Playlist, song: Song) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                android.util.Log.d("PlaylistViewModel", "Starting to add song ${song.title} to playlist ${playlist.name}")
                
                // Check if song is already in playlist
                val existingSongs = playlistDao.getSongsInPlaylist(playlist.id)
                val isAlreadyInPlaylist = existingSongs.any { it.id == song.id }
                
                if (isAlreadyInPlaylist) {
                    android.util.Log.d("PlaylistViewModel", "Song ${song.title} is already in playlist ${playlist.name}")
                    return@launch
                }
                
                // Get the next position in the playlist
                val maxPosition = playlistDao.getMaxPosition(playlist.id) ?: -1
                val nextPosition = maxPosition + 1
                
                android.util.Log.d("PlaylistViewModel", "Adding song at position $nextPosition")
                
                // Create the cross-reference
                val crossRef = com.example.myapplication.data.entity.PlaylistSongCrossRef(
                    playlist.id,
                    song.id,
                    nextPosition
                )
                
                // Insert the cross-reference
                playlistDao.insertPlaylistSong(crossRef)
                
                android.util.Log.d("PlaylistViewModel", "Successfully added song ${song.title} to playlist ${playlist.name} at position $nextPosition")
                
                // Reload current playlist songs if it's the active playlist
                if (_currentPlaylist.value?.id == playlist.id) {
                    android.util.Log.d("PlaylistViewModel", "Reloading playlist songs for ${playlist.name}")
                    loadPlaylistSongs(playlist)
                }
            } catch (e: Exception) {
                android.util.Log.e("PlaylistViewModel", "Error adding song to playlist", e)
                e.printStackTrace()
            }
        }
    }
    
    fun removeSongFromPlaylist(playlist: Playlist, song: Song) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Remove the cross-reference
                playlistDao.removeSongFromPlaylist(playlist.id, song.id)
                
                android.util.Log.d("PlaylistViewModel", "Removed song ${song.title} from playlist ${playlist.name}")
                
                // Reload current playlist songs if it's the active playlist
                if (_currentPlaylist.value?.id == playlist.id) {
                    loadPlaylistSongs(playlist)
                }
            } catch (e: Exception) {
                android.util.Log.e("PlaylistViewModel", "Error removing song from playlist", e)
            }
        }
    }
    
    fun loadPlaylistById(playlistId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                android.util.Log.d("PlaylistViewModel", "Loading playlist by ID: $playlistId")
                val playlist = playlistDao.getById(playlistId)
                if (playlist != null) {
                    android.util.Log.d("PlaylistViewModel", "Found playlist: ${playlist.name}")
                    loadPlaylistSongs(playlist)
                } else {
                    android.util.Log.e("PlaylistViewModel", "Playlist not found with ID: $playlistId")
                }
            } catch (e: Exception) {
                android.util.Log.e("PlaylistViewModel", "Error loading playlist by ID", e)
                e.printStackTrace()
            }
        }
    }
    
    fun updatePlaylist(playlist: Playlist, newName: String, newDescription: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                android.util.Log.d("PlaylistViewModel", "Updating playlist: ${playlist.name}")
                playlistDao.updateName(playlist.id, newName)
                
                // Update description if provided
                if (newDescription != null) {
                    // Note: This would require adding an updateDescription method to PlaylistDao
                    android.util.Log.d("PlaylistViewModel", "Updated playlist name to: $newName")
                }
                
                // Reload playlists
                loadPlaylists()
                
                // Reload current playlist if it's the active one
                if (_currentPlaylist.value?.id == playlist.id) {
                    loadPlaylistSongs(playlist)
                }
            } catch (e: Exception) {
                android.util.Log.e("PlaylistViewModel", "Error updating playlist", e)
                e.printStackTrace()
            }
        }
    }
    
    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                android.util.Log.d("PlaylistViewModel", "Deleting playlist: ${playlist.name}")
                
                // Clear all songs from playlist first
                playlistDao.clearPlaylist(playlist.id)
                
                // Delete the playlist
                playlistDao.deleteById(playlist.id)
                
                android.util.Log.d("PlaylistViewModel", "Successfully deleted playlist: ${playlist.name}")
                
                // Reload playlists
                loadPlaylists()
            } catch (e: Exception) {
                android.util.Log.e("PlaylistViewModel", "Error deleting playlist", e)
                e.printStackTrace()
            }
        }
    }
    
    fun moveSongUp(playlist: Playlist, song: Song) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                android.util.Log.d("PlaylistViewModel", "Moving song up: ${song.title}")
                // TODO: Implement move up logic
                // This would require updating the position in PlaylistSongCrossRef
            } catch (e: Exception) {
                android.util.Log.e("PlaylistViewModel", "Error moving song up", e)
                e.printStackTrace()
            }
        }
    }
    
    fun moveSongDown(playlist: Playlist, song: Song) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                android.util.Log.d("PlaylistViewModel", "Moving song down: ${song.title}")
                // TODO: Implement move down logic
                // This would require updating the position in PlaylistSongCrossRef
            } catch (e: Exception) {
                android.util.Log.e("PlaylistViewModel", "Error moving song down", e)
                e.printStackTrace()
            }
        }
    }
    
    fun refresh() {
        _isLoading.value = true
        loadPlaylists()
    }
}

/**
 * Data class representing a playlist with additional UI state
 */
data class PlaylistWithSongs(
    val playlist: Playlist,
    val songs: List<Song>,
    val songCount: Int = songs.size,
    val totalDuration: Long = songs.sumOf { it.durationMs }
)

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
 * ViewModel for Home Screen
 * Manages Daily Mixes, Recently Played, and Quick Actions
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val songDao = DatabaseProvider.get(application).songDao()
    
    // State for all songs
    private val _allSongs = MutableStateFlow<List<Song>>(emptyList())
    val allSongs: StateFlow<List<Song>> = _allSongs.asStateFlow()
    
    // State for recently played songs
    private val _recentlyPlayed = MutableStateFlow<List<Song>>(emptyList())
    val recentlyPlayed: StateFlow<List<Song>> = _recentlyPlayed.asStateFlow()
    
    // State for daily mixes (generated from library)
    private val _dailyMixes = MutableStateFlow<List<Mix>>(emptyList())
    val dailyMixes: StateFlow<List<Mix>> = _dailyMixes.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        // Live stream of songs
        viewModelScope.launch(Dispatchers.IO) {
            songDao.getAllFlow()
                .distinctUntilChanged()
                .collect { songs ->
                    _allSongs.value = songs
                    _recentlyPlayed.value = songs.takeLast(20).reversed()
                    _dailyMixes.value = generateDailyMixes(songs)
                    _isLoading.value = false
                }
        }
    }
    
    private fun generateDailyMixes(songs: List<Song>): List<Mix> {
        if (songs.isEmpty()) return emptyList()
        
        val mixes = mutableListOf<Mix>()
        
        // Group songs by genre
        val songsByGenre = songs.groupBy { it.genre ?: "Unknown" }
        
        // Create genre-based mixes for top 3 genres
        songsByGenre.entries
            .sortedByDescending { it.value.size }
            .take(3)
            .forEachIndexed { index, entry ->
                val genre = entry.key
                val genreSongs = entry.value.shuffled().take(50)
                
                mixes.add(
                    Mix(
                        id = index.toLong(),
                        name = "$genre Mix",
                        description = "Based on your $genre collection",
                        songIds = genreSongs.map { it.id },
                        coverArtUrl = null // TODO: Get album art from first song
                    )
                )
            }
        
        // Add "Recently Added" mix
        val recentSongs = songs.sortedByDescending { it.dateAddedEpochMs }.take(50)
        if (recentSongs.isNotEmpty()) {
            mixes.add(
                Mix(
                    id = 100L,
                    name = "Recently Added",
                    description = "Your newest additions",
                    songIds = recentSongs.map { it.id },
                    coverArtUrl = null
                )
            )
        }
        
        // Add "Shuffle All" mix
        mixes.add(
            Mix(
                id = 200L,
                name = "All Songs",
                description = "${songs.size} songs",
                songIds = songs.map { it.id },
                coverArtUrl = null
            )
        )
        
        return mixes
    }
    
    fun refresh() { /* flows update automatically */ }
    
    fun playMix(mix: Mix, musicPlayerViewModel: MusicPlayerViewModel) {
        viewModelScope.launch(Dispatchers.IO) {
            // Get songs for this mix
            val mixSongs = songDao.getAll().filter { it.id in mix.songIds }
            if (mixSongs.isNotEmpty()) {
                android.util.Log.d("HomeViewModel", "Playing mix: ${mix.name} with ${mixSongs.size} songs")
                // Play first song from mix with the mix as the playlist
                musicPlayerViewModel.playSong(mixSongs[0], customPlaylist = mixSongs)
            }
        }
    }
    
    fun playSong(song: Song, musicPlayerViewModel: MusicPlayerViewModel) {
        android.util.Log.d("HomeViewModel", "Playing song: ${song.title}")
        // When playing from recently played, use the recently played list as playlist
        musicPlayerViewModel.playSong(song, customPlaylist = _recentlyPlayed.value)
    }
    
    fun shuffleAll(musicPlayerViewModel: MusicPlayerViewModel) {
        viewModelScope.launch(Dispatchers.IO) {
            val songs = _allSongs.value.shuffled()
            if (songs.isNotEmpty()) {
                android.util.Log.d("HomeViewModel", "Shuffle all: ${songs.size} songs")
                // Play with the shuffled list as the playlist
                musicPlayerViewModel.playSong(songs[0], customPlaylist = songs)
            }
        }
    }
}

/**
 * Data class representing a Daily Mix
 */
data class Mix(
    val id: Long,
    val name: String,
    val description: String,
    val songIds: List<Long>,
    val coverArtUrl: String?
)


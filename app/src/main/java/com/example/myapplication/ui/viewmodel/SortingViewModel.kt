package com.example.myapplication.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages sorting preferences for different library sections
 * Persists sorting options across app restarts
 */
class SortingViewModel : ViewModel() {
    
    private var prefs: SharedPreferences? = null
    
    // Sorting states for different sections
    private val _songsSortOrder = MutableStateFlow(SortOrder.TITLE)
    val songsSortOrder: StateFlow<SortOrder> = _songsSortOrder.asStateFlow()
    
    private val _albumsSortOrder = MutableStateFlow(SortOrder.TITLE)
    val albumsSortOrder: StateFlow<SortOrder> = _albumsSortOrder.asStateFlow()
    
    private val _artistsSortOrder = MutableStateFlow(SortOrder.TITLE)
    val artistsSortOrder: StateFlow<SortOrder> = _artistsSortOrder.asStateFlow()
    
    private val _playlistsSortOrder = MutableStateFlow(SortOrder.TITLE)
    val playlistsSortOrder: StateFlow<SortOrder> = _playlistsSortOrder.asStateFlow()
    
    private val _likedSortOrder = MutableStateFlow(SortOrder.TITLE)
    val likedSortOrder: StateFlow<SortOrder> = _likedSortOrder.asStateFlow()
    
    fun initialize(context: Context) {
        prefs = context.getSharedPreferences("sorting_prefs", Context.MODE_PRIVATE)
        loadSortingPreferences()
    }
    
    private fun loadSortingPreferences() {
        prefs?.let { prefs ->
            _songsSortOrder.value = SortOrder.valueOf(
                prefs.getString("songs_sort", SortOrder.TITLE.name) ?: SortOrder.TITLE.name
            )
            _albumsSortOrder.value = SortOrder.valueOf(
                prefs.getString("albums_sort", SortOrder.TITLE.name) ?: SortOrder.TITLE.name
            )
            _artistsSortOrder.value = SortOrder.valueOf(
                prefs.getString("artists_sort", SortOrder.TITLE.name) ?: SortOrder.TITLE.name
            )
            _playlistsSortOrder.value = SortOrder.valueOf(
                prefs.getString("playlists_sort", SortOrder.TITLE.name) ?: SortOrder.TITLE.name
            )
            _likedSortOrder.value = SortOrder.valueOf(
                prefs.getString("liked_sort", SortOrder.DATE_ADDED.name) ?: SortOrder.DATE_ADDED.name
            )
        }
    }
    
    fun setSongsSortOrder(order: SortOrder) {
        _songsSortOrder.value = order
        saveSortingPreference("songs_sort", order)
    }
    
    fun setAlbumsSortOrder(order: SortOrder) {
        _albumsSortOrder.value = order
        saveSortingPreference("albums_sort", order)
    }
    
    fun setArtistsSortOrder(order: SortOrder) {
        _artistsSortOrder.value = order
        saveSortingPreference("artists_sort", order)
    }
    
    fun setPlaylistsSortOrder(order: SortOrder) {
        _playlistsSortOrder.value = order
        saveSortingPreference("playlists_sort", order)
    }
    
    fun setLikedSortOrder(order: SortOrder) {
        _likedSortOrder.value = order
        saveSortingPreference("liked_sort", order)
    }
    
    private fun saveSortingPreference(key: String, order: SortOrder) {
        viewModelScope.launch {
            prefs?.edit()?.putString(key, order.name)?.apply()
        }
    }
}

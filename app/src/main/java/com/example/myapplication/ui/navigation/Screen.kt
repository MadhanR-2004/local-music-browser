package com.example.myapplication.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed class representing all navigation destinations in the app
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : Screen(
        route = "home",
        title = "Home",
        icon = Icons.Default.Home
    )
    
    object Search : Screen(
        route = "search",
        title = "Search",
        icon = Icons.Default.Search
    )
    
    object Library : Screen(
        route = "library?tab={tab}",
        title = "Library",
        icon = Icons.Default.LibraryMusic
    ) {
        fun createRoute(tab: Int = 0) = "library?tab=$tab"
    }
    
    object Settings : Screen(
        route = "settings",
        title = "Settings",
        icon = Icons.Default.Settings
    )
    
    // Detail screens (not in bottom nav)
    object NowPlaying : Screen(
        route = "now_playing",
        title = "Now Playing",
        icon = Icons.Default.MusicNote
    )
    
    object AlbumDetail : Screen(
        route = "album/{albumName}",
        title = "Album",
        icon = Icons.Default.Album
    ) {
        fun createRoute(albumName: String) = "album/${java.net.URLEncoder.encode(albumName, "UTF-8")}"
    }
    
    object ArtistDetail : Screen(
        route = "artist/{artistName}",
        title = "Artist",
        icon = Icons.Default.Person
    ) {
        fun createRoute(artistName: String) = "artist/${java.net.URLEncoder.encode(artistName, "UTF-8")}"
    }
    
    object PlaylistDetail : Screen(
        route = "playlist/{playlistId}",
        title = "Playlist",
        icon = Icons.Default.PlaylistPlay
    ) {
        fun createRoute(playlistId: Long) = "playlist/$playlistId"
    }
    
    object Lyrics : Screen(
        route = "lyrics",
        title = "Lyrics",
        icon = Icons.Default.Lyrics
    )
    
    object Queue : Screen(
        route = "queue",
        title = "Queue",
        icon = Icons.Default.QueueMusic
    )
    
    object FolderSelection : Screen(
        route = "folder_selection",
        title = "Select Music Folders",
        icon = Icons.Default.Folder
    )
    
    object Onboarding : Screen(
        route = "onboarding",
        title = "Welcome",
        icon = Icons.Default.Home
    )
    
    object Favorites : Screen(
        route = "favorites",
        title = "Liked Songs",
        icon = Icons.Default.Favorite
    )
    
    object Playlists : Screen(
        route = "playlists",
        title = "Playlists",
        icon = Icons.Default.PlaylistPlay
    )
}

// Bottom navigation items
val bottomNavItems = listOf(
    Screen.Home,
    Screen.Search,
    Screen.Library,
    Screen.Settings
)



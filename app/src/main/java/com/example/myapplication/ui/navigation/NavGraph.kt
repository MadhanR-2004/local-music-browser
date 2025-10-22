package com.example.myapplication.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.ui.screens.home.HomeScreen
import com.example.myapplication.ui.screens.search.SearchScreen
import com.example.myapplication.ui.screens.library.LibraryScreen
import com.example.myapplication.ui.screens.settings.SettingsScreen
import com.example.myapplication.ui.screens.nowplaying.NowPlayingScreen
import com.example.myapplication.ui.screens.album.AlbumDetailScreen
import com.example.myapplication.ui.screens.artist.ArtistDetailScreen
import com.example.myapplication.ui.screens.playlist.PlaylistDetailScreen
import com.example.myapplication.ui.screens.playlist.PlaylistScreen
import com.example.myapplication.ui.screens.favorites.FavoritesScreen
import com.example.myapplication.ui.screens.lyrics.LyricsScreen
import com.example.myapplication.ui.screens.queue.QueueScreen
import com.example.myapplication.ui.screens.onboarding.FolderSelectionScreen
import com.example.myapplication.ui.screens.onboarding.OnboardingScreen
import com.example.myapplication.ui.viewmodel.MusicPlayerViewModel

/**
 * Main navigation graph for the app
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavGraph(
    navController: NavHostController,
    musicPlayerViewModel: MusicPlayerViewModel,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { 1000 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -300 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -300 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { 1000 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        // Bottom navigation screens
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        
        composable(Screen.Search.route) {
            SearchScreen(navController = navController)
        }
        
        composable(
            route = Screen.Library.route,
            arguments = listOf(navArgument("tab") { 
                type = NavType.IntType
                defaultValue = 0
            })
        ) { backStackEntry ->
            val initialTab = backStackEntry.arguments?.getInt("tab") ?: 0
            LibraryScreen(
                navController = navController,
                initialTab = initialTab
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        
        // Detail screens
        composable(
            route = Screen.NowPlaying.route,
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(400)
                ) + fadeIn(animationSpec = tween(400))
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(400)
                ) + fadeOut(animationSpec = tween(400))
            }
        ) {
            NowPlayingScreen(
                navController = navController,
                musicPlayerViewModel = musicPlayerViewModel
            )
        }
        
        composable(
            route = Screen.AlbumDetail.route,
            arguments = listOf(navArgument("albumName") { type = NavType.StringType })
        ) { backStackEntry ->
            val albumName = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("albumName") ?: "",
                "UTF-8"
            )
            AlbumDetailScreen(
                albumName = albumName,
                navController = navController
            )
        }
        
        composable(
            route = Screen.ArtistDetail.route,
            arguments = listOf(navArgument("artistName") { type = NavType.StringType })
        ) { backStackEntry ->
            val artistName = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("artistName") ?: "",
                "UTF-8"
            )
            ArtistDetailScreen(
                artistName = artistName,
                navController = navController
            )
        }
        
        composable(
            route = Screen.PlaylistDetail.route,
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: 0L
            PlaylistDetailScreen(
                playlistId = playlistId,
                navController = navController
            )
        }
        
        composable(Screen.Lyrics.route) {
            LyricsScreen(navController = navController)
        }
        
        composable(Screen.Queue.route) {
            QueueScreen(
                navController = navController,
                musicPlayerViewModel = musicPlayerViewModel
            )
        }
        
        composable(Screen.FolderSelection.route) {
            FolderSelectionScreen(navController = navController)
        }
        
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }
        
        // Favorites and Playlists screens
        composable(Screen.Favorites.route) {
            FavoritesScreen(navController = navController)
        }
        
        composable(Screen.Playlists.route) {
            PlaylistScreen(navController = navController)
        }
    }
}



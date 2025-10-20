package com.example.myapplication
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.components.BottomNavigationBar
import com.example.myapplication.ui.components.MiniPlayer
import com.example.myapplication.ui.navigation.NavGraph
import com.example.myapplication.ui.navigation.Screen
import com.example.myapplication.ui.theme.MusicPlayerTheme
import com.example.myapplication.ui.viewmodel.MusicPlayerViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController

/**
 * Main Activity using Jetpack Compose with Material 3 Expressive design
 */
class MainActivityCompose : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Trigger auto-scan on app start
        val workManager = androidx.work.WorkManager.getInstance(this)
        val request = androidx.work.OneTimeWorkRequestBuilder<com.example.myapplication.worker.MusicScanWorker>()
            .build()
        workManager.enqueue(request)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        setContent {
            MusicPlayerTheme {
                MusicPlayerApp()
            }
        }
    }
}

@Composable
fun MusicPlayerApp() {
    val navController = rememberNavController()
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Shared MusicPlayerViewModel - single instance for entire app
    val musicPlayerViewModel: MusicPlayerViewModel = viewModel()
    
    // Make system bars transparent
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = androidx.compose.ui.graphics.Color.Transparent,
            darkIcons = useDarkIcons
        )
    }
    
    // Check if onboarding is complete
    val prefs = remember {
        context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
    }
    val onboardingDone = remember {
        prefs.getBoolean("onboarding_done", false)
    }
    
    // Determine start destination
    val startDestination = if (onboardingDone) Screen.Home.route else Screen.Onboarding.route
    
    // Track current route to hide bottom nav on certain screens
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Hide bottom nav and mini player on these screens
    val hideBottomBar = currentRoute in listOf(
        Screen.NowPlaying.route,
        Screen.FolderSelection.route,
        Screen.Onboarding.route
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Content
        NavGraph(
            navController = navController,
            musicPlayerViewModel = musicPlayerViewModel,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (hideBottomBar) 0.dp else 128.dp)
        )
        
        // Bottom UI
        if (!hideBottomBar) {
            BottomUI(navController, musicPlayerViewModel)
        }
    }
}

@Composable
private fun BoxScope.BottomUI(
    navController: androidx.navigation.NavHostController,
    musicPlayerViewModel: MusicPlayerViewModel
) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
    ) {
        MiniPlayer(
            musicPlayerViewModel = musicPlayerViewModel,
            onExpand = {
                navController.navigate(Screen.NowPlaying.route)
            }
        )
        BottomNavigationBar(
            navController = navController,
            visible = true
        )
    }
}


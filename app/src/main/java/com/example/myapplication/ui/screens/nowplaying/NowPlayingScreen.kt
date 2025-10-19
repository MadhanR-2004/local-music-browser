package com.example.myapplication.ui.screens.nowplaying

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.palette.graphics.Palette
import com.example.myapplication.ui.components.AlbumArtImage
import com.example.myapplication.ui.viewmodel.MusicPlayerViewModel
import com.example.myapplication.ui.viewmodel.NowPlayingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Now Playing Screen - Full-screen immersive playback experience
 * Material 3 Expressive Design with animations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    navController: NavController,
    musicPlayerViewModel: MusicPlayerViewModel,
    modifier: Modifier = Modifier,
    viewModel: NowPlayingViewModel = viewModel()
) {
    // Use shared MusicPlayerViewModel for playback state
    val currentSong = musicPlayerViewModel.currentSong
    val isPlaying = musicPlayerViewModel.isPlaying
    val progress = musicPlayerViewModel.progress
    val currentPosition = musicPlayerViewModel.currentPosition
    val albumArt = musicPlayerViewModel.albumArt
    val isShuffleEnabled = musicPlayerViewModel.isShuffleEnabled
    val repeatMode = musicPlayerViewModel.repeatMode
    
    // Local state for like
    val isLiked by viewModel.isLiked.collectAsState()
    
    // Extract dominant colors from album art
    var dominantColor by remember { mutableStateOf<Color?>(null) }
    var vibrantColor by remember { mutableStateOf<Color?>(null) }
    
    LaunchedEffect(albumArt) {
        if (albumArt != null) {
            withContext(Dispatchers.Default) {
                try {
                    val palette = Palette.from(albumArt).generate()
                    dominantColor = palette.getDominantColor(0xFF1DB954.toInt()).let { Color(it) }
                    vibrantColor = palette.getVibrantColor(0xFF1DB954.toInt()).let { Color(it) }
                } catch (e: Exception) {
                    // Fallback to default colors
                    dominantColor = null
                    vibrantColor = null
                }
            }
        } else {
            dominantColor = null
            vibrantColor = null
        }
    }
    
    // Connect ViewModels
    LaunchedEffect(Unit) {
        viewModel.setMusicPlayerViewModel(musicPlayerViewModel)
    }
    
    // Animated background colors - use extracted colors or fallback to theme
    val targetColor = dominantColor ?: if (isPlaying) 
        MaterialTheme.colorScheme.primaryContainer 
    else 
        MaterialTheme.colorScheme.surfaceVariant
    
    val animatedColors by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(800), 
        label = "bg_animation"
    )
    
    // Swipe-to-dismiss state
    var offsetY by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val dismissThreshold = with(density) { 200.dp.toPx() }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        animatedColors,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, offsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (offsetY > dismissThreshold) {
                                // Dismiss the screen
                                navController.popBackStack()
                            } else {
                                // Snap back
                                offsetY = 0f
                            }
                        },
                        onVerticalDrag = { _, dragAmount ->
                            // Only allow downward drags
                            if (dragAmount > 0 || offsetY > 0) {
                                offsetY = (offsetY + dragAmount).coerceAtLeast(0f)
                            }
                        }
                    )
                }
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar
            TopAppBar(
                title = { Text("Now Playing") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.KeyboardArrowDown, "Close")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: More options */ }) {
                        Icon(Icons.Default.MoreVert, "More")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                
                // Album Art with animation
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(horizontal = 32.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    AlbumArtImage(
                        bitmap = albumArt,
                        filePath = currentSong?.path,
                        contentDescription = "Album Art for ${currentSong?.title}"
                    )
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Song Info
                if (currentSong != null) {
                    Text(
                        text = currentSong.title,
                        style = MaterialTheme.typography.headlineLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentSong.artist,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "No song playing",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Progress Bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Local state for slider to prevent freeze on drag
                    var sliderPosition by remember { mutableStateOf(0f) }
                    var isSliderDragging by remember { mutableStateOf(false) }
                    
                    // Update slider position from progress only when not dragging
                    LaunchedEffect(progress) {
                        if (!isSliderDragging) {
                            sliderPosition = progress
                        }
                    }
                    
                    Slider(
                        value = sliderPosition,
                        onValueChange = { newValue ->
                            isSliderDragging = true
                            sliderPosition = newValue
                        },
                        onValueChangeFinished = {
                            isSliderDragging = false
                            val duration = currentSong?.durationMs ?: 0L
                            val targetMs = (duration * sliderPosition).toLong()
                            musicPlayerViewModel.seekTo(targetMs)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(currentPosition),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatTime(currentSong?.durationMs ?: 0L),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Main Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle
                    com.example.myapplication.ui.components.InteractiveIconButton(
                        onClick = { musicPlayerViewModel.toggleShuffle() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (isShuffleEnabled) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Previous
                    com.example.myapplication.ui.components.InteractiveFilledIconButton(
                        onClick = { musicPlayerViewModel.seekToPrevious() },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    // Play/Pause
                    com.example.myapplication.ui.components.InteractiveFAB(
                        onClick = { musicPlayerViewModel.togglePlayPause() },
                        modifier = Modifier.size(80.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    // Next
                    com.example.myapplication.ui.components.InteractiveFilledIconButton(
                        onClick = { musicPlayerViewModel.seekToNext() },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    // Repeat
                    com.example.myapplication.ui.components.InteractiveIconButton(
                        onClick = { musicPlayerViewModel.toggleRepeat() }
                    ) {
                        Icon(
                            imageVector = when (repeatMode) {
                                1 -> Icons.Default.Repeat
                                2 -> Icons.Default.RepeatOne
                                else -> Icons.Default.Repeat
                            },
                            contentDescription = "Repeat",
                            tint = if (repeatMode > 0) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Secondary Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    com.example.myapplication.ui.components.InteractiveIconButton(
                        onClick = { viewModel.toggleLike() }
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) 
                                MaterialTheme.colorScheme.error 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    com.example.myapplication.ui.components.InteractiveIconButton(
                        onClick = { /* TODO: Share */ }
                    ) {
                        Icon(Icons.Default.Share, "Share")
                    }
                    
                    com.example.myapplication.ui.components.InteractiveIconButton(
                        onClick = {
                            navController.navigate(com.example.myapplication.ui.navigation.Screen.Queue.route)
                        }
                    ) {
                        Icon(Icons.Default.QueueMusic, "Queue")
                    }
                    
                    com.example.myapplication.ui.components.InteractiveIconButton(
                        onClick = { /* TODO: Lyrics */ }
                    ) {
                        Icon(Icons.Default.Lyrics, "Lyrics")
                    }
                }
            }
        }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}



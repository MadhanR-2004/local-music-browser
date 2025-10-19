package com.example.myapplication.ui.screens.album

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.data.entity.Song

/**
 * Album Detail Screen - Shows album art, info, and track listing
 * Material 3 Expressive Design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    navController: NavController,
    albumName: String,
    modifier: Modifier = Modifier
) {
    val viewModel: com.example.myapplication.ui.viewmodel.MusicPlayerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Load album songs from database
    val songs by produceState<List<Song>>(initialValue = emptyList(), albumName) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val db = com.example.myapplication.data.DatabaseProvider.get(
                context.applicationContext
            )
            val allSongs = db.songDao().getAll()
            // Sort by track number for albums
            value = allSongs
                .filter { it.album == albumName }
                .sortedBy { it.trackNumber }
        }
    }
    
    val artistName = songs.firstOrNull()?.artist ?: "Unknown Artist"
    val trackCount = songs.size
    val totalDuration = songs.sumOf { it.durationMs }
    
    // Format duration
    val duration = formatDuration(totalDuration)
    val year = songs.firstOrNull()?.year?.toString() ?: "Unknown"
    
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // Hero Section with Full-Width Album Art
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                ) {
                    // Full-width album art background
                    com.example.myapplication.ui.components.AlbumArtImage(
                        filePath = songs.firstOrNull()?.path,
                        contentDescription = "Album Art",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Gradient overlay for text readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.3f),
                                        Color.Black.copy(alpha = 0.7f)
                                    ),
                                    startY = 0f,
                                    endY = 1000f
                                )
                            )
                    )
                    
                    // Album Info overlay
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = albumName,
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = artistName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$year • $trackCount songs • $duration",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // Action Buttons Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    com.example.myapplication.ui.components.InteractiveButton(
                        onClick = { 
                            if (songs.isNotEmpty()) {
                                viewModel.playSong(songs.first(), customPlaylist = songs)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        isPrimary = true
                    ) {
                        Icon(Icons.Default.PlayArrow, "Play", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Play")
                    }
                    
                    com.example.myapplication.ui.components.InteractiveButton(
                        onClick = { 
                            if (songs.isNotEmpty()) {
                                val shuffled = songs.shuffled()
                                viewModel.playSong(shuffled.first(), customPlaylist = shuffled)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        isPrimary = false
                    ) {
                        Icon(Icons.Default.Shuffle, "Shuffle", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Shuffle")
                    }
                }
            }
            
            // Track List Header
            item {
                ListItem(
                    headlineContent = {
                        Text(
                            "Songs",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )
            }
            
            // Track List
            if (songs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No songs in this album",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(songs) { song ->
                    AlbumTrackItem(
                        song = song,
                        onClick = { viewModel.playSong(song, customPlaylist = songs) }
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
        
        // Floating navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            com.example.myapplication.ui.components.InteractiveFilledIconButton(
                onClick = { 
                    // Navigate back to Library Albums tab (tab index 1)
                    navController.navigate(com.example.myapplication.ui.navigation.Screen.Library.createRoute(tab = 1)) {
                        popUpTo(com.example.myapplication.ui.navigation.Screen.Library.route) {
                            inclusive = true
                        }
                    }
                },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.3f),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.ArrowBack, "Back")
            }
            
            com.example.myapplication.ui.components.InteractiveFilledIconButton(
                onClick = { /* TODO: More options */ },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.3f),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.MoreVert, "More")
            }
        }
    }
}

@Composable
fun AlbumTrackItem(
    song: Song,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = song.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = song.artist,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            Text(
                text = song.trackNumber.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            IconButton(onClick = { /* TODO: More options */ }) {
                Icon(Icons.Default.MoreVert, "More")
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

/**
 * Format duration in milliseconds to readable format (e.g., "45 min")
 */
private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    
    return when {
        hours > 0 -> "$hours hr ${minutes} min"
        else -> "$minutes min"
    }
}

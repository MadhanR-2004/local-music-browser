package com.example.myapplication.ui.screens.artist

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
 * Artist Detail Screen - Shows artist info, albums, and songs
 * Material 3 Expressive Design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    navController: NavController,
    artistName: String,
    modifier: Modifier = Modifier
) {
    val viewModel: com.example.myapplication.ui.viewmodel.MusicPlayerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Load artist songs from database
    val songs by produceState<List<Song>>(initialValue = emptyList(), artistName) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val db = com.example.myapplication.data.DatabaseProvider.get(
                context.applicationContext
            )
            val allSongs = db.songDao().getAll()
            // Sort by album, then track number for artists
            value = allSongs
                .filter { it.artist == artistName }
                .sortedWith(compareBy({ it.album }, { it.trackNumber }))
        }
    }
    
    val albumCount = songs.map { it.album }.distinct().size
    val songCount = songs.size
    
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // Hero Section with Full-Width Artist Image
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                ) {
                    // Full-width artist image background
                    com.example.myapplication.ui.components.AlbumArtImage(
                        filePath = songs.firstOrNull()?.path,
                        contentDescription = "Artist Image",
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
                    
                    // Artist Info overlay
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = artistName,
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$albumCount albums • $songCount songs",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.9f)
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
            
            // Popular Songs Header
            item {
                ListItem(
                    headlineContent = {
                        Text(
                            "Popular Songs",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )
            }
            
            // Song List
            if (songs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No songs by this artist",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(songs) { song ->
                    ArtistSongItem(
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
                    // Navigate back to Library Artists tab (tab index 2)
                    navController.navigate(com.example.myapplication.ui.navigation.Screen.Library.createRoute(tab = 2)) {
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
fun ArtistSongItem(
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
                text = song.album,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.small,
                tonalElevation = 1.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        trailingContent = {
            IconButton(onClick = { /* TODO: More options */ }) {
                Icon(Icons.Default.MoreVert, "More")
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

package com.example.myapplication.ui.screens.favorites

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.data.entity.Song
import com.example.myapplication.ui.components.AlbumArtImage
import com.example.myapplication.ui.components.ExpressiveButton
import com.example.myapplication.ui.components.ExpressiveCard
import com.example.myapplication.ui.components.ExpressiveIconButton
import com.example.myapplication.ui.viewmodel.FavoritesViewModel
import com.example.myapplication.ui.viewmodel.FavoritesSortOrder
import com.example.myapplication.ui.viewmodel.MusicPlayerViewModel

/**
 * Favorites Screen - Liked Songs with Material 3 Expressive Design
 * Features: Sort options, shuffle all, clear all, individual song management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = viewModel(),
    musicPlayerViewModel: MusicPlayerViewModel = viewModel()
) {
    val likedSongs by viewModel.likedSongs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    
    var showSortMenu by remember { mutableStateOf(false) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    
    // Refresh data when screen appears
    LaunchedEffect(Unit) {
        android.util.Log.d("FavoritesScreen", "Screen appeared, refreshing data...")
        viewModel.refresh()
    }
    
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Liked Songs",
                            style = MaterialTheme.typography.headlineLarge
                        )
                        if (likedSongs.isNotEmpty()) {
                            Text(
                                text = "${likedSongs.size} songs",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Sort menu
                    Box {
                        ExpressiveIconButton(
                            onClick = { showSortMenu = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort"
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            FavoritesSortOrder.values().forEach { order ->
                                DropdownMenuItem(
                                    text = { Text(getSortOrderLabel(order)) },
                                    onClick = {
                                        viewModel.setSortOrder(order)
                                        showSortMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = getSortOrderIcon(order),
                                            contentDescription = null
                                        )
                                    }
                                )
                            }
                        }
                    }
                    
                    // More options
                    ExpressiveIconButton(
                        onClick = { /* TODO: Show more options menu */ }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        floatingActionButton = {
            if (likedSongs.isNotEmpty()) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Clear all FAB
                    FloatingActionButton(
                        onClick = { showClearAllDialog = true },
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ClearAll,
                            contentDescription = "Clear all favorites"
                        )
                    }
                    
                    // Shuffle all FAB
                    FloatingActionButton(
                        onClick = { 
                            musicPlayerViewModel.playSong(likedSongs[0], customPlaylist = likedSongs)
                            musicPlayerViewModel.toggleShuffle()
                        },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle all"
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (likedSongs.isEmpty()) {
            EmptyFavoritesState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = likedSongs,
                    key = { song -> song.id }
                ) { song ->
                    LikedSongItem(
                        song = song,
                        isFavorite = true,
                        onSongClick = { 
                            musicPlayerViewModel.playSong(song, customPlaylist = likedSongs)
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(song) },
                        onMoreClick = { /* TODO: Show song options */ }
                    )
                }
                
                // Bottom spacing for FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
    
    // Clear all dialog
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = {
                Text("Clear All Favorites")
            },
            text = {
                Text("Are you sure you want to remove all songs from your favorites? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllFavorites()
                        showClearAllDialog = false
                    }
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearAllDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun LikedSongItem(
    song: Song,
    isFavorite: Boolean,
    onSongClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    ExpressiveCard(
        onClick = onSongClick,
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.medium),
                tonalElevation = 2.dp
            ) {
                AlbumArtImage(
                    filePath = song.path,
                    contentDescription = "Album Art for ${song.title}"
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Song info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${song.artist} • ${song.album}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Duration
            Text(
                text = formatDuration(song.durationMs),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            // Favorite button
            ExpressiveIconButton(
                onClick = onToggleFavorite
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // More options
            ExpressiveIconButton(
                onClick = onMoreClick
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options"
                )
            }
        }
    }
}

@Composable
fun EmptyFavoritesState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Heart icon with gradient background
        Surface(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "No Liked Songs Yet",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Tap the heart icon on any song to add it to your favorites",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        ExpressiveButton(
            onClick = { /* Navigate to library */ },
            isPrimary = true
        ) {
            Icon(
                imageVector = Icons.Default.LibraryMusic,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Browse Library")
        }
    }
}

private fun getSortOrderLabel(order: FavoritesSortOrder): String {
    return when (order) {
        FavoritesSortOrder.RECENTLY_LIKED -> "Recently Liked"
        FavoritesSortOrder.TITLE -> "Title"
        FavoritesSortOrder.ARTIST -> "Artist"
        FavoritesSortOrder.ALBUM -> "Album"
        FavoritesSortOrder.DATE_ADDED -> "Date Added"
    }
}

private fun getSortOrderIcon(order: FavoritesSortOrder): androidx.compose.ui.graphics.vector.ImageVector {
    return when (order) {
        FavoritesSortOrder.RECENTLY_LIKED -> Icons.Default.Schedule
        FavoritesSortOrder.TITLE -> Icons.Default.SortByAlpha
        FavoritesSortOrder.ARTIST -> Icons.Default.Person
        FavoritesSortOrder.ALBUM -> Icons.Default.Album
        FavoritesSortOrder.DATE_ADDED -> Icons.Default.CalendarToday
    }
}

private fun formatDuration(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / (1000 * 60)) % 60
    val hours = (ms / (1000 * 60 * 60))
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}

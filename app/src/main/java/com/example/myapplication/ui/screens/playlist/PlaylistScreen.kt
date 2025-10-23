package com.example.myapplication.ui.screens.playlist

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.media.MediaMetadataRetriever
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.myapplication.data.entity.Playlist
import com.example.myapplication.data.entity.Song
import com.example.myapplication.ui.components.AlbumArtImage
import com.example.myapplication.ui.components.ExpressiveButton
import com.example.myapplication.ui.components.ExpressiveCard
import com.example.myapplication.ui.components.ExpressiveIconButton
import com.example.myapplication.ui.viewmodel.PlaylistViewModel
import com.example.myapplication.ui.viewmodel.MusicPlayerViewModel

/**
 * Playlist Screen - User and System Playlists with Material 3 Expressive Design
 * Features: Create, edit, delete playlists, system playlists, playlist management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: PlaylistViewModel = viewModel(),
    musicPlayerViewModel: MusicPlayerViewModel = viewModel()
) {
    val playlists by viewModel.playlists.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    
    // Refresh data when screen appears
    LaunchedEffect(Unit) {
        android.util.Log.d("PlaylistScreen", "Screen appeared, refreshing data...")
        viewModel.refresh()
    }
    
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Playlists",
                        style = MaterialTheme.typography.headlineLarge
                    )
                },
                actions = {
                    // Add playlist button
                    ExpressiveIconButton(
                        onClick = { showCreateDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create playlist"
                        )
                    }
                    
                    // More options
                    ExpressiveIconButton(
                        onClick = { showMoreMenu = true }
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
        } else if (playlists.isEmpty()) {
            EmptyPlaylistsState(
                onCreatePlaylist = { showCreateDialog = true },
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // System Playlists Section
                item {
                    SystemPlaylistsSection(
                        onPlaylistClick = { playlist ->
                            // Navigate to playlist detail
                            navController.navigate("playlist/${playlist.id}")
                        }
                    )
                }
                
                // User Playlists Section
                if (playlists.isNotEmpty()) {
                    item {
                        Text(
                            text = "Your Playlists",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    items(playlists) { playlist ->
                        PlaylistItem(
                            playlist = playlist,
                            onPlaylistClick = { 
                                navController.navigate("playlist/${playlist.id}")
                            },
                            onMoreClick = { /* TODO: Show playlist options */ },
                            navController = navController
                        )
                    }
                }
                
                // Bottom spacing for FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
    
    // Create playlist dialog
    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onCreatePlaylist = { name, description ->
                viewModel.createPlaylist(name, description)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun SystemPlaylistsSection(
    onPlaylistClick: (SystemPlaylist) -> Unit,
    modifier: Modifier = Modifier
) {
    val systemPlaylists = listOf(
        SystemPlaylist(
            id = "recently_played",
            name = "Recently Played",
            description = "Your recently played songs",
            icon = Icons.Default.History,
            songCount = 0 // TODO: Get actual count
        ),
        SystemPlaylist(
            id = "most_played",
            name = "Most Played",
            description = "Your most played songs",
            icon = Icons.Default.TrendingUp,
            songCount = 0 // TODO: Get actual count
        ),
        SystemPlaylist(
            id = "recently_added",
            name = "Recently Added",
            description = "Recently added to library",
            icon = Icons.Default.NewReleases,
            songCount = 0 // TODO: Get actual count
        )
    )
    
    Column(modifier = modifier) {
        Text(
            text = "System Playlists",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(systemPlaylists) { playlist ->
                SystemPlaylistCard(
                    playlist = playlist,
                    onClick = { onPlaylistClick(playlist) }
                )
            }
        }
    }
}

@Composable
fun SystemPlaylistCard(
    playlist: SystemPlaylist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ExpressiveCard(
        onClick = onClick,
        modifier = modifier
            .width(160.dp)
            .height(120.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.tertiaryContainer
                            )
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = playlist.icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Column {
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${playlist.songCount} songs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun PlaylistItem(
    playlist: Playlist,
    onPlaylistClick: () -> Unit,
    onMoreClick: () -> Unit,
    navController: NavController
) {
    var showMenu by remember { mutableStateOf(false) }
    
    // Get playlist songs for dynamic colors and info
    val playlistViewModel: PlaylistViewModel = viewModel()
    val playlistSongs by playlistViewModel.currentPlaylistSongs.collectAsState()
    
    // Load playlist songs when this item is displayed
    LaunchedEffect(playlist.id) {
        playlistViewModel.loadPlaylistById(playlist.id)
    }
    
    // Extract colors from first few songs for dynamic background
    var dominantColor by remember { mutableStateOf<Color?>(null) }
    var vibrantColor by remember { mutableStateOf<Color?>(null) }
    
    // Get default colors in @Composable context
    val defaultPrimaryContainer = MaterialTheme.colorScheme.primaryContainer
    val defaultPrimary = MaterialTheme.colorScheme.primary
    
    LaunchedEffect(playlistSongs) {
        if (playlistSongs.isNotEmpty()) {
            // Use first song's album art for color extraction
            val firstSong = playlistSongs.first()
            try {
                withContext(Dispatchers.Default) {
                    // Extract colors from album art using MediaMetadataRetriever
                    val retriever = MediaMetadataRetriever()
                    try {
                        retriever.setDataSource(firstSong.path)
                        val albumArt = retriever.embeddedPicture
                        if (albumArt != null) {
                            val bitmap = android.graphics.BitmapFactory.decodeByteArray(albumArt, 0, albumArt.size)
                            val palette = Palette.from(bitmap).generate()
                            
                            val dominantSwatch = palette.dominantSwatch
                            val vibrantSwatch = palette.vibrantSwatch
                            
                            dominantColor = dominantSwatch?.rgb?.let { Color(it) } ?: defaultPrimaryContainer
                            vibrantColor = vibrantSwatch?.rgb?.let { Color(it) } ?: defaultPrimary
                        } else {
                            // No album art, use hash-based colors
                            val hash = playlist.name.hashCode()
                            dominantColor = Color(
                                red = ((hash and 0xFF0000) shr 16) / 255f,
                                green = ((hash and 0x00FF00) shr 8) / 255f,
                                blue = (hash and 0x0000FF) / 255f,
                                alpha = 0.8f
                            )
                            vibrantColor = Color(
                                red = ((hash and 0xFF0000) shr 16) / 255f,
                                green = ((hash and 0x00FF00) shr 8) / 255f,
                                blue = (hash and 0x0000FF) / 255f,
                                alpha = 1f
                            )
                        }
                    } finally {
                        retriever.release()
                    }
                }
            } catch (e: Exception) {
                // Fallback to default colors
                dominantColor = defaultPrimaryContainer
                vibrantColor = defaultPrimary
            }
        } else {
            // No songs in playlist, use default colors
            dominantColor = defaultPrimaryContainer
            vibrantColor = defaultPrimary
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlaylistClick),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            dominantColor ?: MaterialTheme.colorScheme.primaryContainer,
                            vibrantColor ?: MaterialTheme.colorScheme.primary
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Playlist icon
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlaylistPlay,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Playlist info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (playlist.description != null && playlist.description.isNotBlank()) {
                        Text(
                            text = playlist.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "${playlistSongs.size} songs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                }

                // More options menu
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Playlist") },
                            onClick = {
                                showMenu = false
                                navController.navigate("playlist/${playlist.id}")
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text("Delete Playlist") },
                            onClick = {
                                showMenu = false
                                playlistViewModel.deletePlaylist(playlist)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyPlaylistsState(
    onCreatePlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Playlist icon with gradient background
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
                    imageVector = Icons.Default.PlaylistPlay,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "No Playlists Yet",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Create your first playlist to organize your music",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        ExpressiveButton(
            onClick = onCreatePlaylist,
            isPrimary = true
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Playlist")
        }
    }
}


@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreatePlaylist: (String, String?) -> Unit
) {
    var playlistName by remember { mutableStateOf("") }
    var playlistDescription by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create New Playlist")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    label = { Text("Playlist Name") },
                    placeholder = { Text("My Awesome Playlist") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = playlistDescription,
                    onValueChange = { playlistDescription = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("A description for your playlist") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (playlistName.isNotBlank()) {
                        onCreatePlaylist(playlistName.trim(), playlistDescription.trim().takeIf { it.isNotBlank() })
                    }
                },
                enabled = playlistName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

data class SystemPlaylist(
    val id: String,
    val name: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val songCount: Int
)

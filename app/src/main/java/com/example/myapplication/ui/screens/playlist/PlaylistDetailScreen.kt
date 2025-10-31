package com.example.myapplication.ui.screens.playlist

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.data.entity.Playlist
import com.example.myapplication.data.entity.Song
import com.example.myapplication.ui.components.AlbumArtImage
import com.example.myapplication.ui.components.ExpressiveButton
import com.example.myapplication.ui.components.ExpressiveCard
import com.example.myapplication.ui.components.ExpressiveIconButton
import com.example.myapplication.ui.viewmodel.PlaylistViewModel
import com.example.myapplication.ui.viewmodel.MusicPlayerViewModel

/**
 * Playlist Detail Screen - Individual playlist management with Material 3 Expressive Design
 * Features: Play all, shuffle, add songs, remove songs, reorder, playlist info
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: PlaylistViewModel = viewModel(),
    musicPlayerViewModel: MusicPlayerViewModel = viewModel()
) {
    val currentPlaylist by viewModel.currentPlaylist.collectAsState()
    val playlistSongs by viewModel.currentPlaylistSongs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var showAddSongsDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var isReorderMode by remember { mutableStateOf(false) }
    var reorderWorkingList by remember(playlistSongs, isReorderMode) {
        mutableStateOf(playlistSongs.toList())
    }
    
    // Load playlist when screen appears
    LaunchedEffect(playlistId) {
        android.util.Log.d("PlaylistDetailScreen", "Loading playlist: $playlistId")
        viewModel.loadPlaylistById(playlistId)
    }
    
    // Debug: Log playlist songs changes
    LaunchedEffect(playlistSongs) {
        android.util.Log.d("PlaylistDetailScreen", "Playlist songs updated: ${playlistSongs.size} songs")
        playlistSongs.forEach { song ->
            android.util.Log.d("PlaylistDetailScreen", "  - ${song.title} by ${song.artist}")
        }
    }
    
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentPlaylist?.name ?: "Playlist",
                            style = MaterialTheme.typography.headlineLarge
                        )
                        if (!currentPlaylist?.description.isNullOrBlank()) {
                            Text(
                                text = currentPlaylist?.description ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (playlistSongs.isNotEmpty()) {
                            Text(
                                text = "${playlistSongs.size} songs",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Add songs
                    ExpressiveIconButton(
                        onClick = { showAddSongsDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add songs"
                        )
                    }
                    
                    // More options
                    Box {
                        ExpressiveIconButton(
                            onClick = { showMoreMenu = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options"
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Playlist") },
                                onClick = {
                                    showMoreMenu = false
                                    showEditDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                }
                            )
                            
                            DropdownMenuItem(
                                text = { Text(if (isReorderMode) "Done Reordering" else "Reorder Songs") },
                                onClick = {
                                    showMoreMenu = false
                                    isReorderMode = !isReorderMode
                                    reorderWorkingList = playlistSongs.toMutableList()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.SwapVert, contentDescription = null)
                                }
                            )
                            
                            DropdownMenuItem(
                                text = { Text("Delete Playlist") },
                                onClick = {
                                    showMoreMenu = false
                                    showDeleteDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                }
                            )
                        }
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
        } else if (playlistSongs.isEmpty()) {
            EmptyPlaylistState(
                onAddSongs = { showAddSongsDialog = true },
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
                if (isReorderMode) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ExpressiveButton(
                                onClick = {
                                    currentPlaylist?.let { playlist ->
                                        viewModel.persistReorderedList(playlist, reorderWorkingList)
                                        isReorderMode = false
                                    }
                                },
                                isPrimary = true,
                                modifier = Modifier.weight(1f)
                            ) { Text("Save") }
                            ExpressiveButton(
                                onClick = {
                                    isReorderMode = false
                                },
                                isPrimary = false,
                                modifier = Modifier.weight(1f)
                            ) { Text("Cancel") }
                        }
                    }
                }
                // Action buttons
                if (!isReorderMode) {
                    item {
                        PlaylistActionButtons(
                            onPlayAll = {
                                if (playlistSongs.isNotEmpty()) {
                                    musicPlayerViewModel.playSong(playlistSongs[0], customPlaylist = playlistSongs)
                                }
                            },
                            onShuffle = {
                                if (playlistSongs.isNotEmpty()) {
                                    val shuffledSongs = playlistSongs.shuffled()
                                    musicPlayerViewModel.playSong(shuffledSongs[0], customPlaylist = shuffledSongs)
                                    musicPlayerViewModel.toggleShuffle()
                                }
                            },
                            onAddToQueue = {
                                // TODO: Add all songs to queue
                            }
                        )
                    }
                }
                
                // Songs list
                items(
                    items = if (isReorderMode) reorderWorkingList else playlistSongs,
                    key = { song -> song.id }
                ) { song ->
                    if (isReorderMode) {
                        ReorderablePlaylistItem(
                            song = song,
                            onMoveUp = {
                                val idx = reorderWorkingList.indexOf(song)
                                if (idx > 0) {
                                    val copy = reorderWorkingList.toMutableList()
                                    java.util.Collections.swap(copy, idx, idx - 1)
                                    reorderWorkingList = copy
                                }
                            },
                            onMoveDown = {
                                val idx = reorderWorkingList.indexOf(song)
                                if (idx >= 0 && idx < reorderWorkingList.size - 1) {
                                    val copy = reorderWorkingList.toMutableList()
                                    java.util.Collections.swap(copy, idx, idx + 1)
                                    reorderWorkingList = copy
                                }
                            },
                            onDragStepUp = {
                                val idx = reorderWorkingList.indexOf(song)
                                if (idx > 0) {
                                    val copy = reorderWorkingList.toMutableList()
                                    java.util.Collections.swap(copy, idx, idx - 1)
                                    reorderWorkingList = copy
                                }
                            },
                            onDragStepDown = {
                                val idx = reorderWorkingList.indexOf(song)
                                if (idx >= 0 && idx < reorderWorkingList.size - 1) {
                                    val copy = reorderWorkingList.toMutableList()
                                    java.util.Collections.swap(copy, idx, idx + 1)
                                    reorderWorkingList = copy
                                }
                            }
                        )
                    } else {
                        PlaylistSongItem(
                            song = song,
                            onSongClick = { 
                                musicPlayerViewModel.playSong(song, customPlaylist = playlistSongs)
                            },
                            onRemoveFromPlaylist = {
                                currentPlaylist?.let { playlist ->
                                    viewModel.removeSongFromPlaylist(playlist, song)
                                }
                            },
                            onMoreClick = { 
                                // Handle move up/down actions
                                currentPlaylist?.let { playlist ->
                                    val currentIndex = playlistSongs.indexOf(song)
                                    if (currentIndex > 0) {
                                        viewModel.moveSongUp(playlist, song)
                                    } else if (currentIndex < playlistSongs.size - 1) {
                                        viewModel.moveSongDown(playlist, song)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Add songs dialog
    if (showAddSongsDialog) {
        AddSongsDialog(
            onDismiss = { showAddSongsDialog = false },
            onAddSongs = { songs ->
                android.util.Log.d("PlaylistDetailScreen", "Adding ${songs.size} songs to playlist")
                currentPlaylist?.let { playlist ->
                    songs.forEach { song ->
                        android.util.Log.d("PlaylistDetailScreen", "Adding song: ${song.title}")
                        viewModel.addSongToPlaylist(playlist, song)
                    }
                }
                showAddSongsDialog = false
            }
        )
    }
    
    // Edit playlist dialog
    if (showEditDialog) {
        EditPlaylistDialog(
            playlist = currentPlaylist,
            onDismiss = { showEditDialog = false },
            onSave = { name, description ->
                currentPlaylist?.let { playlist ->
                    viewModel.updatePlaylist(playlist, name, description)
                }
                showEditDialog = false
            }
        )
    }
    
    // Delete playlist dialog
    if (showDeleteDialog) {
        DeletePlaylistDialog(
            playlist = currentPlaylist,
            onDismiss = { showDeleteDialog = false },
            onDelete = {
                currentPlaylist?.let { playlist ->
                    viewModel.deletePlaylist(playlist)
                    navController.popBackStack()
                }
                showDeleteDialog = false
            }
        )
    }
}

@Composable
fun PlaylistActionButtons(
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    onAddToQueue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Play All button
        ExpressiveButton(
            onClick = onPlayAll,
            isPrimary = true,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Play All")
        }
        
        // Shuffle button
        ExpressiveButton(
            onClick = onShuffle,
            isPrimary = false,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Shuffle")
        }
        
        // Add to Queue button
        ExpressiveIconButton(
            onClick = onAddToQueue
        ) {
            Icon(
                imageVector = Icons.Default.QueueMusic,
                contentDescription = "Add to queue"
            )
        }
    }
}

@Composable
fun PlaylistSongItem(
    song: Song,
    onSongClick: () -> Unit,
    onRemoveFromPlaylist: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ExpressiveCard(
        onClick = onSongClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
            
            // Remove from playlist button
            ExpressiveIconButton(
                onClick = onRemoveFromPlaylist
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Remove from playlist",
                    tint = MaterialTheme.colorScheme.error
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
fun ReorderablePlaylistItem(
    song: Song,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDragStepUp: () -> Unit,
    onDragStepDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val threshold = with(LocalDensity.current) { 40.dp.toPx() }
    var isDragging by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isDragging) 0.98f else 1f, animationSpec = tween(120), label = "drag-scale")
    ExpressiveCard(
        onClick = {},
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .pointerInput(Unit) {
                var accumulated = 0f
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDrag = { _, dragAmount: Offset ->
                        accumulated += dragAmount.y
                        if (accumulated <= -threshold) {
                            onDragStepUp()
                            accumulated = 0f
                        } else if (accumulated >= threshold) {
                            onDragStepDown()
                            accumulated = 0f
                        }
                    },
                    onDragEnd = { accumulated = 0f; isDragging = false },
                    onDragCancel = { accumulated = 0f; isDragging = false }
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                ExpressiveIconButton(onClick = onMoveUp) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move up")
                }
                ExpressiveIconButton(onClick = onMoveDown) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move down")
                }
                Icon(
                    imageVector = Icons.Default.DragIndicator,
                    contentDescription = "Drag",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmptyPlaylistState(
    onAddSongs: () -> Unit,
    modifier: Modifier = Modifier
) {
        Column(
            modifier = modifier
                .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Music note icon with gradient background
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
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Empty Playlist",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Add songs to this playlist to get started",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        ExpressiveButton(
            onClick = onAddSongs,
            isPrimary = true
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Songs")
        }
    }
}

@Composable
fun AddSongsDialog(
    onDismiss: () -> Unit,
    onAddSongs: (List<Song>) -> Unit
) {
    val libraryViewModel: com.example.myapplication.ui.viewmodel.LibraryViewModel = viewModel()
    val allSongs by libraryViewModel.songs.collectAsState()
    var selectedSongs by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Filter songs based on search query
    val filteredSongs = remember(allSongs, searchQuery) {
        if (searchQuery.isBlank()) {
            allSongs
        } else {
            allSongs.filter { song ->
                song.title.contains(searchQuery, ignoreCase = true) ||
                song.artist.contains(searchQuery, ignoreCase = true) ||
                song.album.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Songs to Playlist")
        },
        text = {
            if (allSongs.isEmpty()) {
                Text("No songs available to add.")
            } else {
                Column {
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search songs") },
                        placeholder = { Text("Search by title, artist, or album") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Results count
                    Text(
                        text = if (searchQuery.isBlank()) {
                            "${allSongs.size} songs available"
                        } else {
                            "${filteredSongs.size} of ${allSongs.size} songs"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Songs list
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 500.dp)
                    ) {
                        items(filteredSongs) { song ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedSongs = if (song.id in selectedSongs) {
                                            selectedSongs - song.id
                                        } else {
                                            selectedSongs + song.id
                                        }
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = song.id in selectedSongs,
                                    onCheckedChange = { isChecked ->
                                        selectedSongs = if (isChecked) {
                                            selectedSongs + song.id
                                        } else {
                                            selectedSongs - song.id
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
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
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val songsToAdd = filteredSongs.filter { it.id in selectedSongs }
                    onAddSongs(songsToAdd)
                },
                enabled = selectedSongs.isNotEmpty()
            ) {
                Text("Add Selected (${selectedSongs.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PlaylistSongItem(
    song: Song,
    onSongClick: () -> Unit,
    onRemoveFromPlaylist: () -> Unit,
    onMoreClick: () -> Unit
) {
    var showSongMenu by remember { mutableStateOf(false) }
    
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
                text = "${song.artist} • ${song.album}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 1.dp
            ) {
                AlbumArtImage(
                    filePath = song.path,
                    contentDescription = "Album art for ${song.title}"
                )
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatDuration(song.durationMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Spacer(modifier = Modifier.width(8.dp))

                Box {
                    IconButton(onClick = { showSongMenu = true }) {
                        Icon(Icons.Default.MoreVert, "More")
                    }
                    
                    DropdownMenu(
                        expanded = showSongMenu,
                        onDismissRequest = { showSongMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Remove from Playlist") },
                            onClick = {
                                showSongMenu = false
                                onRemoveFromPlaylist()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Remove, contentDescription = null)
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text("Play Next") },
                            onClick = {
                                showSongMenu = false
                                // TODO: Implement play next
                            },
                            leadingIcon = {
                                Icon(Icons.Default.PriorityHigh, contentDescription = null)
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text("Add to Queue") },
                            onClick = {
                                showSongMenu = false
                                // TODO: Implement add to queue
                            },
                            leadingIcon = {
                                Icon(Icons.Default.QueueMusic, contentDescription = null)
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text("Move Up") },
                            onClick = {
                                showSongMenu = false
                                onMoreClick() // This will be handled by the parent
                            },
                            leadingIcon = {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text("Move Down") },
                            onClick = {
                                showSongMenu = false
                                onMoreClick() // This will be handled by the parent
                            },
                            leadingIcon = {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                            }
                        )
                        
                        
                    }
                }
            }
        },
        modifier = Modifier.clickable(onClick = onSongClick)
    )
}

@Composable
fun EditPlaylistDialog(
    playlist: Playlist?,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit
) {
    var playlistName by remember { mutableStateOf(playlist?.name ?: "") }
    var playlistDescription by remember { mutableStateOf(playlist?.description ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Playlist")
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
                        onSave(playlistName.trim(), playlistDescription.trim().takeIf { it.isNotBlank() })
                    }
                },
                enabled = playlistName.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeletePlaylistDialog(
    playlist: Playlist?,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Delete Playlist")
        },
        text = {
            Column {
                Text("Are you sure you want to delete this playlist?")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"${playlist?.name ?: "Unknown"}\"",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This action cannot be undone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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
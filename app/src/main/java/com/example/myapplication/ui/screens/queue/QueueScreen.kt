package com.example.myapplication.ui.screens.queue

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.data.entity.Song
import com.example.myapplication.ui.viewmodel.MusicPlayerViewModel
import kotlinx.coroutines.launch

/**
 * Queue Screen - Current playback queue with reordering
 * Material 3 Expressive Design
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun QueueScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    musicPlayerViewModel: MusicPlayerViewModel = viewModel(),
    sortedList: List<Song>? = null
) {
    // Load queue hierarchy from music player service
    var playNextQueue by remember { mutableStateOf<List<Song>>(emptyList()) }
    var regularQueue by remember { mutableStateOf<List<Song>>(emptyList()) }
    var originalContext by remember { mutableStateOf<List<Song>>(emptyList()) }
    var currentSong by remember { mutableStateOf<Song?>(null) }
    val scope = rememberCoroutineScope()
    
    // Refresh queue periodically
    LaunchedEffect(Unit) {
        while (true) {
            playNextQueue = musicPlayerViewModel.getPlayNextQueue()
            regularQueue = musicPlayerViewModel.getRegularQueue()
            // Use sorted list if provided, otherwise try to get current sorted list
            originalContext = if (sortedList != null) {
                musicPlayerViewModel.getSortedOriginalContext(sortedList)
            } else {
                val currentSortedList = musicPlayerViewModel.getCurrentSortedList()
                if (currentSortedList.isNotEmpty()) {
                    musicPlayerViewModel.getSortedOriginalContext(currentSortedList)
                } else {
                    musicPlayerViewModel.getOriginalContext()
                }
            }
            currentSong = musicPlayerViewModel.currentSong
            kotlinx.coroutines.delay(500) // Update every 500ms
        }
    }
    
    // Drag state
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var targetIndex by remember { mutableStateOf<Int?>(null) }
    var draggedSection by remember { mutableStateOf<String?>(null) }
    var accumulatedDy by remember { mutableStateOf(0f) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Queue") },
                navigationIcon = {
                    com.example.myapplication.ui.components.InteractiveIconButton(
                        onClick = { navController.navigateUp() }
                    ) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    com.example.myapplication.ui.components.InteractiveIconButton(
                        onClick = {
                            musicPlayerViewModel.clearQueue()
                            scope.launch {
                                kotlinx.coroutines.delay(100)
                                playNextQueue = musicPlayerViewModel.getPlayNextQueue()
                                regularQueue = musicPlayerViewModel.getRegularQueue()
                                originalContext = musicPlayerViewModel.getOriginalContext()
                            }
                        }
                    ) {
                        Icon(Icons.Default.ClearAll, "Clear All")
                    }
                }
            )
        }
    ) { paddingValues ->
        val totalSongs = playNextQueue.size + regularQueue.size + originalContext.size
        val listState = rememberLazyListState()
        
        if (totalSongs == 0 && currentSong == null) {
            // Empty queue
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Queue is empty",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Songs you play will appear here",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                // Now Playing section
                item(key = "now_playing_header") {
                    Text(
                        text = "Now Playing",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                currentSong?.let { song ->
                    item(key = "current_${song.id}") {
                        QueueSongItem(
                            song = song,
                            isPlaying = true,
                            isDraggable = false,
                            onRemove = { },
                            onClick = { }
                        )
                    }
                }
                
                // Play Next section
                if (playNextQueue.isNotEmpty()) {
                    item(key = "play_next_header") {
                        Text(
                            text = "Play Next (${playNextQueue.size} songs)",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    
                    itemsIndexed(
                        items = playNextQueue,
                        key = { _, song -> "play_next_${song.id}" }
                    ) { index, song ->
                        QueueSongItem(
                            song = song,
                            isPlaying = false,
                            isDraggable = true,
                            section = "play_next",
                            onRemove = {
                                musicPlayerViewModel.removeFromPlayNext(index)
                                scope.launch {
                                    kotlinx.coroutines.delay(100)
                                    playNextQueue = musicPlayerViewModel.getPlayNextQueue()
                                }
                            },
                            onClick = {
                                // Jump to this song
                                for (i in 0 until index) {
                                    musicPlayerViewModel.seekToNext()
                                }
                            },
                            onDragStart = {
                                draggedIndex = index
                                draggedSection = "play_next"
                            },
                            onDragEnd = {
                                draggedIndex?.let { from ->
                                    targetIndex?.let { to ->
                                        if (from != to) {
                                            musicPlayerViewModel.movePlayNextItem(from, to)
                                            scope.launch {
                                                kotlinx.coroutines.delay(100)
                                                playNextQueue = musicPlayerViewModel.getPlayNextQueue()
                                            }
                                        }
                                    }
                                }
                                draggedIndex = null
                                targetIndex = null
                                draggedSection = null
                                accumulatedDy = 0f
                            },
                            onDragTargetChanged = { isTarget ->
                                if (isTarget) {
                                    targetIndex = index
                                }
                            },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }
                
                // Regular Queue section
                if (regularQueue.isNotEmpty()) {
                    item(key = "queue_header") {
                        Text(
                            text = "Queue (${regularQueue.size} songs)",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    
                    itemsIndexed(
                        items = regularQueue,
                        key = { _, song -> "queue_${song.id}" }
                    ) { index, song ->
                        QueueSongItem(
                            song = song,
                            isPlaying = false,
                            isDraggable = true,
                            section = "queue",
                            onRemove = {
                                musicPlayerViewModel.removeFromRegularQueue(index)
                                scope.launch {
                                    kotlinx.coroutines.delay(100)
                                    regularQueue = musicPlayerViewModel.getRegularQueue()
                                }
                            },
                            onClick = {
                                // Jump to this song
                                for (i in 0 until playNextQueue.size + index) {
                                    musicPlayerViewModel.seekToNext()
                                }
                            },
                            onDragStart = {
                                draggedIndex = index
                                draggedSection = "queue"
                            },
                            onDragEnd = {
                                draggedIndex?.let { from ->
                                    targetIndex?.let { to ->
                                        if (from != to) {
                                            musicPlayerViewModel.moveRegularQueueItem(from, to)
                                            scope.launch {
                                                kotlinx.coroutines.delay(100)
                                                regularQueue = musicPlayerViewModel.getRegularQueue()
                                            }
                                        }
                                    }
                                }
                                draggedIndex = null
                                targetIndex = null
                                draggedSection = null
                                accumulatedDy = 0f
                            },
                            onDragTargetChanged = { isTarget ->
                                if (isTarget) {
                                    targetIndex = index
                                }
                            },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }
                
                // Original Context section
                if (originalContext.isNotEmpty()) {
                    item(key = "context_header") {
                        Text(
                            text = "From Album/Playlist (${originalContext.size} songs)",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    
                    itemsIndexed(
                        items = originalContext,
                        key = { _, song -> "context_${song.id}" }
                    ) { index, song ->
                        QueueSongItem(
                            song = song,
                            isPlaying = false,
                            isDraggable = false,
                            section = "context",
                            onRemove = { },
                            onClick = {
                                // Play this song directly from the sorted context
                                musicPlayerViewModel.playSongFromContext(song, originalContext)
                            }
                        )
                    }
                }
                
                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun QueueSongItem(
    song: Song,
    isPlaying: Boolean,
    isDraggable: Boolean,
    onRemove: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    section: String? = null,
    onDragStart: () -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDragTargetChanged: (Boolean) -> Unit = {}
) {
    var isDragging by remember { mutableStateOf(false) }
    val elevation by animateDpAsState(
        targetValue = if (isDragging) 8.dp else 0.dp,
        label = "elevation"
    )
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isDraggable) {
                    Modifier.pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                isDragging = true
                                onDragStart()
                            },
                            onDragEnd = {
                                isDragging = false
                                onDragEnd()
                            },
                            onDragCancel = {
                                isDragging = false
                                onDragEnd()
                            },
                            onDrag = { _, dragAmount ->
                                // Notify parent that this item is a potential target while dragging
                                onDragTargetChanged(true)
                            }
                        )
                    }
                } else Modifier
            ),
        tonalElevation = elevation,
        shadowElevation = elevation
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = song.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isPlaying) MaterialTheme.colorScheme.primary else Color.Unspecified
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
                when {
                    isPlaying -> {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Playing",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    section == "play_next" -> {
                        Icon(
                            Icons.Default.PriorityHigh,
                            contentDescription = "Play Next",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    section == "queue" -> {
                        Icon(
                            Icons.Default.QueueMusic,
                            contentDescription = "In Queue",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    section == "context" -> {
                        Icon(
                            Icons.Default.Album,
                            contentDescription = "From Album/Playlist",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    isDraggable -> {
                        Icon(
                            Icons.Default.DragHandle,
                            contentDescription = "Drag to reorder",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            trailingContent = {
                if (isDraggable) {
                    com.example.myapplication.ui.components.InteractiveIconButton(
                        onClick = onRemove
                    ) {
                        Icon(Icons.Default.Close, "Remove")
                    }
                }
            },
            modifier = Modifier
                .clickable(onClick = onClick)
                .graphicsLayer {
                    if (isDragging) {
                        scaleX = 1.05f
                        scaleY = 1.05f
                        alpha = 0.8f
                    }
                }
        )
    }
}

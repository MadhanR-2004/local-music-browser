package com.example.myapplication.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.InitialLoadInitializer
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.data.entity.Song
import com.example.myapplication.ui.components.AlbumArtImage
import com.example.myapplication.ui.viewmodel.*

/**
 * Library Screen - Complete music collection with tabs
 * Material 3 Expressive Design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    initialTab: Int = 0,
    viewModel: LibraryViewModel = viewModel(),
    musicPlayerViewModel: MusicPlayerViewModel = viewModel()
) {
    val sortingViewModel: SortingViewModel = viewModel()
    val context = LocalContext.current
    
    // Initialize sorting preferences
    LaunchedEffect(Unit) {
        sortingViewModel.initialize(context)
    }
    
    var selectedTab by remember { mutableStateOf(initialTab) }
    val tabs = listOf("Songs", "Albums", "Artists", "Playlists", "Liked")
    
    val songs by viewModel.songs.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Library",
                        style = MaterialTheme.typography.headlineLarge
                    )
                },
                actions = {
                    var showSortMenu by remember { mutableStateOf(false) }
                    
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort")
                        }
                        
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            val currentSortOrder = when (selectedTab) {
                                0 -> sortingViewModel.songsSortOrder.value
                                1 -> sortingViewModel.albumsSortOrder.value
                                2 -> sortingViewModel.artistsSortOrder.value
                                3 -> sortingViewModel.playlistsSortOrder.value
                                4 -> sortingViewModel.likedSortOrder.value
                                else -> SortOrder.TITLE
                            }
                            
                            val sortOptions = when (selectedTab) {
                                0, 4 -> listOf(
                                    "Title" to SortOrder.TITLE,
                                    "Artist" to SortOrder.ARTIST,
                                    "Album" to SortOrder.ALBUM,
                                    "Date Added" to SortOrder.DATE_ADDED,
                                    "Duration" to SortOrder.DURATION,
                                    "Play Count" to SortOrder.PLAY_COUNT
                                )
                                1, 2, 3 -> listOf(
                                    "Title" to SortOrder.TITLE,
                                    "Date Added" to SortOrder.DATE_ADDED
                                )
                                else -> emptyList()
                            }
                            
                            sortOptions.forEach { (label, order) ->
                                DropdownMenuItem(
                                    text = { 
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(label)
                                            if (currentSortOrder == order) {
                                                Icon(Icons.Default.Check, contentDescription = null)
                                            }
                                        }
                                    },
                                    onClick = {
                                        when (selectedTab) {
                                            0 -> sortingViewModel.setSongsSortOrder(order)
                                            1 -> sortingViewModel.setAlbumsSortOrder(order)
                                            2 -> sortingViewModel.setArtistsSortOrder(order)
                                            3 -> sortingViewModel.setPlaylistsSortOrder(order)
                                            4 -> sortingViewModel.setLikedSortOrder(order)
                                        }
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                edgePadding = 16.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    )
                }
            }
            
            // Tab Content
            when (selectedTab) {
                0 -> SongsTab(songs, isLoading, musicPlayerViewModel, navController, sortingViewModel)
                1 -> AlbumsTab(albums, isLoading, navController, musicPlayerViewModel, sortingViewModel)
                2 -> ArtistsTab(artists, isLoading, navController, musicPlayerViewModel, sortingViewModel)
                3 -> PlaylistsTab(musicPlayerViewModel, sortingViewModel)
                4 -> LikedTab(viewModel, musicPlayerViewModel, sortingViewModel)
            }
        }
    }
}

@Composable
fun SongsTab(
    songs: List<Song>, 
    isLoading: Boolean,
    musicPlayerViewModel: MusicPlayerViewModel,
    navController: NavController? = null,
    sortingViewModel: SortingViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sortOrder by sortingViewModel.songsSortOrder.collectAsState()
    
    // Apply sorting to songs
    val sortedSongs = remember(songs, sortOrder) {
        when (sortOrder) {
            SortOrder.TITLE -> songs.sortedBy { it.title.lowercase() }
            SortOrder.ARTIST -> songs.sortedBy { it.artist.lowercase() }
            SortOrder.ALBUM -> songs.sortedBy { it.album.lowercase() }
            SortOrder.DATE_ADDED -> songs.sortedByDescending { it.dateAddedEpochMs }
            SortOrder.DURATION -> songs.sortedByDescending { it.durationMs }
            SortOrder.PLAY_COUNT -> songs.sortedByDescending { it.playCount }
            SortOrder.RECENTLY_ADDED -> songs.sortedByDescending { it.dateAddedEpochMs }
        }
    }
    
    // Update queue when sort order changes while music is playing
    LaunchedEffect(sortOrder) {
        if (musicPlayerViewModel.currentSong != null) {
            musicPlayerViewModel.updateQueueWithSortedList(sortedSongs)
        }
    }
    
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (sortedSongs.isEmpty()) {
        EmptyLibraryState("No songs in your library")
    } else {
        val swipeState = rememberSwipeRefreshState(isLoading)
        SwipeRefresh(
            state = swipeState,
            onRefresh = {
                // Trigger rescan via InitialLoadInitializer (uses flows to update UI)
                if (context is android.app.Application) {
                    InitialLoadInitializer.runAsync(context)
                }
            }
        ) {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 0.dp)
        ) {
            items(
                items = sortedSongs,
                key = { song -> song.id }, // stable item keys
                contentType = { _ -> "song" } // helps Compose reuse item nodes
            ) { song ->
                SongListItem(
                    song = song,
                    onClick = { musicPlayerViewModel.playSong(song, customPlaylist = sortedSongs) },
                    navController = navController,
                    musicPlayerViewModel = musicPlayerViewModel
                )
            }
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
        }
    }
}

@Composable
fun AlbumsTab(
    albums: List<AlbumGroup>, 
    isLoading: Boolean,
    navController: NavController,
    musicPlayerViewModel: MusicPlayerViewModel,
    sortingViewModel: SortingViewModel
) {
    val sortOrder by sortingViewModel.albumsSortOrder.collectAsState()
    val context = LocalContext.current
    
    // Apply sorting to albums
    val sortedAlbums = remember(albums, sortOrder) {
        when (sortOrder) {
            SortOrder.TITLE -> albums.sortedBy { it.name.lowercase() }
            SortOrder.DATE_ADDED -> albums.sortedByDescending { it.songs.maxOfOrNull { song -> song.dateAddedEpochMs } ?: 0L }
            else -> albums.sortedBy { it.name.lowercase() }
        }
    }
    
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (sortedAlbums.isEmpty()) {
        EmptyLibraryState("No albums in your library")
    } else {
        val swipeState = rememberSwipeRefreshState(isLoading)
        SwipeRefresh(
            state = swipeState,
            onRefresh = {
                if (context is android.app.Application) {
                    InitialLoadInitializer.runAsync(context)
                }
            }
        ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = sortedAlbums,
                key = { album -> album.name },
                contentType = { _ -> "album" }
            ) { album ->
                AlbumGridItem(
                    album = album, 
                    onClick = { 
                        // Navigate to album detail screen
                        navController.navigate(
                            com.example.myapplication.ui.navigation.Screen.AlbumDetail.createRoute(album.name)
                        )
                    }
                )
            }
        }
        }
    }
}

@Composable
fun ArtistsTab(
    artists: List<ArtistGroup>, 
    isLoading: Boolean,
    navController: NavController,
    musicPlayerViewModel: MusicPlayerViewModel = viewModel(),
    sortingViewModel: SortingViewModel
) {
    val sortOrder by sortingViewModel.artistsSortOrder.collectAsState()
    val context = LocalContext.current
    
    // Apply sorting to artists
    val sortedArtists = remember(artists, sortOrder) {
        when (sortOrder) {
            SortOrder.TITLE -> artists.sortedBy { it.name.lowercase() }
            SortOrder.DATE_ADDED -> artists.sortedByDescending { it.songs.maxOfOrNull { song -> song.dateAddedEpochMs } ?: 0L }
            else -> artists.sortedBy { it.name.lowercase() }
        }
    }
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (sortedArtists.isEmpty()) {
        EmptyLibraryState("No artists in your library")
    } else {
        val swipeState = rememberSwipeRefreshState(isLoading)
        SwipeRefresh(
            state = swipeState,
            onRefresh = {
                if (context is android.app.Application) {
                    InitialLoadInitializer.runAsync(context)
                }
            }
        ) {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(sortedArtists) { artist ->
                ArtistListItem(
                    artist = artist, 
                    onClick = { 
                        // Navigate to artist detail screen
                        navController.navigate(
                            com.example.myapplication.ui.navigation.Screen.ArtistDetail.createRoute(artist.name)
                        )
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
        }
    }
}

@Composable
fun PlaylistsTab(musicPlayerViewModel: MusicPlayerViewModel = viewModel(), sortingViewModel: SortingViewModel) {
    // TODO: Implement playlists from database
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.QueueMusic,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Playlists",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Create custom playlists",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        FilledTonalButton(
            onClick = { 
                // TODO: Navigate to create playlist
                android.util.Log.d("LibraryScreen", "Create playlist clicked")
            }
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Playlist")
        }
    }
}

@Composable
fun LikedTab(
    viewModel: LibraryViewModel = viewModel(),
    musicPlayerViewModel: MusicPlayerViewModel = viewModel(),
    sortingViewModel: SortingViewModel
) {
    val likedSongs by viewModel.likedSongs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val sortOrder by sortingViewModel.likedSortOrder.collectAsState()
    val context = LocalContext.current
    
    // Apply sorting to liked songs
    val sortedLikedSongs = remember(likedSongs, sortOrder) {
        when (sortOrder) {
            SortOrder.TITLE -> likedSongs.sortedBy { it.title.lowercase() }
            SortOrder.ARTIST -> likedSongs.sortedBy { it.artist.lowercase() }
            SortOrder.ALBUM -> likedSongs.sortedBy { it.album.lowercase() }
            SortOrder.DATE_ADDED -> likedSongs.sortedByDescending { it.dateAddedEpochMs }
            SortOrder.DURATION -> likedSongs.sortedByDescending { it.durationMs }
            SortOrder.PLAY_COUNT -> likedSongs.sortedByDescending { it.playCount }
            SortOrder.RECENTLY_ADDED -> likedSongs.sortedByDescending { it.dateAddedEpochMs }
        }
    }
    
    // Update queue when sort order changes while music is playing
    LaunchedEffect(sortOrder) {
        if (musicPlayerViewModel.currentSong != null) {
            musicPlayerViewModel.updateQueueWithSortedList(sortedLikedSongs)
        }
    }
    
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (sortedLikedSongs.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No liked songs",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap ❤️ on songs to add them here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with stats
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${sortedLikedSongs.size} songs",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Your favorites",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    FilledTonalButton(
                        onClick = { 
                            if (sortedLikedSongs.isNotEmpty()) {
                                // Shuffle all liked songs
                                musicPlayerViewModel.playSong(sortedLikedSongs.random(), customPlaylist = sortedLikedSongs)
                                android.util.Log.d("LibraryScreen", "Shuffling liked songs")
                            }
                        }
                    ) {
                        Icon(Icons.Default.Shuffle, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Shuffle")
                    }
                }
            }
            
            // Songs list with pull-to-refresh
            val swipeState = rememberSwipeRefreshState(isLoading)
            SwipeRefresh(
                state = swipeState,
                onRefresh = {
                    if (context is android.app.Application) {
                        InitialLoadInitializer.runAsync(context)
                    }
                }
            ) {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(
                    items = sortedLikedSongs,
                    key = { song -> song.id },
                    contentType = { _ -> "liked_song" }
                ) { song ->
                    SongListItem(
                        song = song,
                        onClick = { musicPlayerViewModel.playSong(song, customPlaylist = sortedLikedSongs) },
                        navController = null, // NavController not available in this scope
                        musicPlayerViewModel = musicPlayerViewModel
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
            }
        }
    }
}

@Composable
fun SongListItem(
    song: Song,
    onClick: () -> Unit,
    navController: NavController? = null,
    musicPlayerViewModel: MusicPlayerViewModel? = null
) {
    // Memoize the song info to prevent unnecessary recompositions
    val songInfo = remember(song.title, song.artist, song.album) {
        "${song.artist} • ${song.album}"
    }
    
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
                text = songInfo,
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
                // Album art is cached; avoid remeasuring during scroll
                AlbumArtImage(
                    filePath = song.path,
                    contentDescription = "Album art for ${song.title}"
                )
            }
        },
        trailingContent = {
            var showMenu by remember { mutableStateOf(false) }
            val isCurrent = musicPlayerViewModel?.currentSong?.id == song.id
            val isPlaying = musicPlayerViewModel?.isPlaying == true

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Now Playing expressive indicator
                com.example.myapplication.ui.components.NowPlayingIndicator(
                    isActive = isCurrent && isPlaying,
                    modifier = Modifier
                        .height(14.dp)
                        .padding(end = 8.dp)
                )

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "More")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                    musicPlayerViewModel?.let { viewModel ->
                        DropdownMenuItem(
                            text = { Text("Play Next") },
                            onClick = {
                                showMenu = false
                                viewModel.addToPlayNext(song)
                                android.util.Log.d("LibraryScreen", "Play next: ${song.title}")
                            },
                            leadingIcon = {
                                Icon(Icons.Default.PriorityHigh, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Add to Queue") },
                            onClick = {
                                showMenu = false
                                viewModel.addToQueue(song)
                                android.util.Log.d("LibraryScreen", "Add to queue: ${song.title}")
                            },
                            leadingIcon = {
                                Icon(Icons.Default.QueueMusic, contentDescription = null)
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Add to Playlist") },
                        onClick = {
                            showMenu = false
                            android.util.Log.d("LibraryScreen", "Add to playlist: ${song.title}")
                        },
                        leadingIcon = {
                            Icon(Icons.Default.PlaylistAdd, contentDescription = null)
                        }
                    )
                    if (navController != null) {
                        DropdownMenuItem(
                            text = { Text("Go to Album") },
                            onClick = {
                                showMenu = false
                                navController.navigate(
                                    com.example.myapplication.ui.navigation.Screen.AlbumDetail.createRoute(song.album)
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Album, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Go to Artist") },
                            onClick = {
                                showMenu = false
                                navController.navigate(
                                    com.example.myapplication.ui.navigation.Screen.ArtistDetail.createRoute(song.artist)
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Song Info") },
                        onClick = {
                            showMenu = false
                            android.util.Log.d("LibraryScreen", "Song info: ${song.title}")
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Info, contentDescription = null)
                        }
                    )
                    }
                }
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
fun AlbumGridItem(
    album: AlbumGroup,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            shape = MaterialTheme.shapes.large
        ) {
            AlbumArtImage(
                filePath = album.albumArtPath,
                contentDescription = "Album art for ${album.name}",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = album.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = album.artist,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ArtistListItem(
    artist: ArtistGroup,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = artist.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = "${artist.songCount} songs • ${artist.albumCount} albums",
                maxLines = 1
            )
        },
        leadingContent = {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                tonalElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.tertiaryContainer,
                                    MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
fun EmptyLibraryState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LibraryMusic,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}




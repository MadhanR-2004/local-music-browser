package com.example.myapplication.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.media.MediaMetadataRetriever
import android.graphics.BitmapFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.InitialLoadInitializer
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.data.entity.Song
import com.example.myapplication.ui.components.AlbumArtImage
import com.example.myapplication.ui.components.InteractiveIconButton
import com.example.myapplication.ui.components.ExpressiveButton
import com.example.myapplication.ui.components.ExpressiveCard
import com.example.myapplication.ui.components.ExpressiveIconButton
import com.example.myapplication.ui.viewmodel.*
import com.example.myapplication.ui.screens.favorites.FavoritesScreen
import com.example.myapplication.ui.screens.playlist.PlaylistScreen

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
    val favoritesViewModel: FavoritesViewModel = viewModel()
    val playlistViewModel: PlaylistViewModel = viewModel()
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
                0 -> SongsTab(songs, isLoading, musicPlayerViewModel, navController, sortingViewModel,     favoritesViewModel, playlistViewModel)
                1 -> AlbumsTab(albums, isLoading, navController, musicPlayerViewModel, sortingViewModel)
                2 -> ArtistsTab(artists, isLoading, navController, musicPlayerViewModel, sortingViewModel)
                3 -> PlaylistsTab(musicPlayerViewModel, sortingViewModel, navController)
                4 -> LikedTab(viewModel, musicPlayerViewModel, sortingViewModel, navController)
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
    sortingViewModel: SortingViewModel,
    favoritesViewModel: FavoritesViewModel = viewModel(),
    playlistViewModel: PlaylistViewModel = viewModel()
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
                    musicPlayerViewModel = musicPlayerViewModel,
                    favoritesViewModel = favoritesViewModel,
                    playlistViewModel = playlistViewModel
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
fun PlaylistsTab(
    musicPlayerViewModel: MusicPlayerViewModel = viewModel(), 
    sortingViewModel: SortingViewModel,
    navController: NavController
) {
    val playlistViewModel: PlaylistViewModel = viewModel()
    val playlists by playlistViewModel.playlists.collectAsState()
    val isLoading by playlistViewModel.isLoading.collectAsState()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    
    // Refresh playlists when this tab is displayed
    LaunchedEffect(Unit) {
        playlistViewModel.refresh()
    }
    
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (playlists.isEmpty()) {
    Column(
        modifier = Modifier
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
                onClick = { showCreateDialog = true },
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
    } else {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar with add button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Playlists",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                ExpressiveIconButton(
                    onClick = { showCreateDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create playlist"
                    )
                }
            }
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
        }
    }
    
    // Create playlist dialog
    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onCreatePlaylist = { name, description ->
                playlistViewModel.createPlaylist(name, description)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun LikedTab(
    viewModel: LibraryViewModel = viewModel(),
    musicPlayerViewModel: MusicPlayerViewModel = viewModel(),
    sortingViewModel: SortingViewModel,
    navController: NavController
) {
    val favoritesViewModel: FavoritesViewModel = viewModel()
    val playlistViewModel: PlaylistViewModel = viewModel()
    val likedSongs by favoritesViewModel.likedSongs.collectAsState()
    val isLoading by favoritesViewModel.isLoading.collectAsState()
    val sortOrder by sortingViewModel.likedSortOrder.collectAsState()
    
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
                    
                    ExpressiveButton(
                        onClick = { 
                            if (sortedLikedSongs.isNotEmpty()) {
                                // Shuffle all liked songs
                                musicPlayerViewModel.playSong(sortedLikedSongs.random(), customPlaylist = sortedLikedSongs)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Shuffle, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Shuffle")
                    }
                }
            }
            
            // Songs list
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
                        navController = navController,
                        musicPlayerViewModel = musicPlayerViewModel,
                        favoritesViewModel = favoritesViewModel,
                        playlistViewModel = playlistViewModel
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
fun SongListItem(
    song: Song,
    onClick: () -> Unit,
    navController: NavController? = null,
    musicPlayerViewModel: MusicPlayerViewModel? = null,
    favoritesViewModel: FavoritesViewModel? = null,
    playlistViewModel: PlaylistViewModel? = null
) {
    // Memoize the song info to prevent unnecessary recompositions
    val songInfo = remember(song.title, song.artist, song.album) {
        "${song.artist} • ${song.album}"
    }
    
    // State for playlist selection dialog
    var showPlaylistDialog by remember { mutableStateOf(false) }
    
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
            val isFavorite = favoritesViewModel?.isFavorite(song.id) ?: false

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
                    
                    // Like/Unlike option
                    favoritesViewModel?.let { favViewModel ->
                        val isFavorite = favViewModel.isFavorite(song.id)
                        DropdownMenuItem(
                            text = { Text(if (isFavorite) "Remove from Favorites" else "Add to Favorites") },
                            onClick = {
                                showMenu = false
                                favViewModel.toggleFavorite(song)
                                android.util.Log.d("LibraryScreen", "${if (isFavorite) "Removed from" else "Added to"} favorites: ${song.title}")
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Add to Playlist") },
                        onClick = {
                            showMenu = false
                            showPlaylistDialog = true
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
    
    // Playlist selection dialog
    if (showPlaylistDialog) {
        PlaylistSelectionDialog(
            song = song,
            onDismiss = { showPlaylistDialog = false },
            onPlaylistSelected = { playlist ->
                playlistViewModel?.addSongToPlaylist(playlist, song)
                showPlaylistDialog = false
            },
            playlistViewModel = playlistViewModel
        )
    }
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

@Composable
fun PlaylistItem(
    playlist: com.example.myapplication.data.entity.Playlist,
    onPlaylistClick: () -> Unit,
    onMoreClick: () -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    
    // Get playlist songs and count directly from database
    val playlistViewModel: PlaylistViewModel = viewModel()
    var songCount by remember { mutableStateOf(0) }
    var playlistSongs by remember { mutableStateOf<List<com.example.myapplication.data.entity.Song>>(emptyList()) }
    
    // Get context outside of LaunchedEffect
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Load songs when this item is displayed
    LaunchedEffect(playlist.id) {
        try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val songs = com.example.myapplication.data.DatabaseProvider.get(context)
                    .playlistDao().getSongsInPlaylist(playlist.id)
                playlistSongs = songs.take(4) // Get first 4 songs
                songCount = songs.size
            }
        } catch (e: Exception) {
            songCount = 0
            playlistSongs = emptyList()
        }
    }
    
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlaylistClick),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Large square album-style cover with 4 thumbnails
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Square aspect ratio
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (playlistSongs.isNotEmpty()) {
                    // Show 2x2 grid of album art thumbnails
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Top row (2 thumbnails)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            // Top-left thumbnail
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                if (playlistSongs.size > 0) {
                                    AlbumArtImage(
                                        filePath = playlistSongs[0].path,
                                        contentDescription = "Album art 1",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            // Top-right thumbnail
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                if (playlistSongs.size > 1) {
                                    AlbumArtImage(
                                        filePath = playlistSongs[1].path,
                                        contentDescription = "Album art 2",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    // Placeholder
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                }
                            }
                        }
                        
                        // Bottom row (2 thumbnails)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            // Bottom-left thumbnail
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                if (playlistSongs.size > 2) {
                                    AlbumArtImage(
                                        filePath = playlistSongs[2].path,
                                        contentDescription = "Album art 3",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    // Placeholder
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                }
                            }
                            // Bottom-right thumbnail
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                if (playlistSongs.size > 3) {
                                    AlbumArtImage(
                                        filePath = playlistSongs[3].path,
                                        contentDescription = "Album art 4",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    // Placeholder
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Fallback icon when no songs
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlaylistPlay,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Playlist info below the square
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (playlist.description != null && playlist.description.isNotBlank()) {
                        Text(
                            text = playlist.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Text(
                        text = "$songCount songs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // More options menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            modifier = Modifier.size(20.dp)
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

@Composable
fun PlaylistSelectionDialog(
    song: Song,
    onDismiss: () -> Unit,
    onPlaylistSelected: (com.example.myapplication.data.entity.Playlist) -> Unit,
    playlistViewModel: PlaylistViewModel? = null
) {
    val viewModel: PlaylistViewModel = playlistViewModel ?: viewModel()
    val playlists by viewModel.playlists.collectAsState()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add to Playlist")
        },
        text = {
            if (playlists.isEmpty()) {
                Text("No playlists available. Create a playlist first.")
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(playlists) { playlist ->
                        ListItem(
                            headlineContent = {
                                Text(playlist.name)
                            },
                            supportingContent = {
                                Text(playlist.description ?: "No description")
                            },
                            leadingContent = {
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    shape = MaterialTheme.shapes.medium,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlaylistPlay,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.clickable {
                                onPlaylistSelected(playlist)
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                enabled = playlists.isNotEmpty()
            ) {
                Text("Cancel")
            }
        }
    )
}


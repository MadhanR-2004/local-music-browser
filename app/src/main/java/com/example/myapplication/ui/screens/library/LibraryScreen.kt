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
import com.example.myapplication.data.entity.Song
import com.example.myapplication.ui.components.AlbumArtImage
import com.example.myapplication.ui.viewmodel.*
import com.example.myapplication.ui.viewmodel.MusicPlayerViewModel
import com.example.myapplication.ui.viewmodel.SortOrder

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
                            DropdownMenuItem(
                                text = { Text("Alphabetical") },
                                onClick = {
                                    viewModel.setSortOrder(SortOrder.ALPHABETICAL)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("By Artist") },
                                onClick = {
                                    viewModel.setSortOrder(SortOrder.ARTIST)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Recently Added") },
                                onClick = {
                                    viewModel.setSortOrder(SortOrder.RECENTLY_ADDED)
                                    showSortMenu = false
                                }
                            )
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
                0 -> SongsTab(songs, isLoading, musicPlayerViewModel, navController)
                1 -> AlbumsTab(albums, isLoading, navController, musicPlayerViewModel)
                2 -> ArtistsTab(artists, isLoading, navController, musicPlayerViewModel)
                3 -> PlaylistsTab(musicPlayerViewModel)
                4 -> LikedTab(viewModel, musicPlayerViewModel)
            }
        }
    }
}

@Composable
fun SongsTab(
    songs: List<Song>, 
    isLoading: Boolean,
    musicPlayerViewModel: MusicPlayerViewModel,
    navController: NavController? = null
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (songs.isEmpty()) {
        EmptyLibraryState("No songs in your library")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 0.dp)
        ) {
                items(songs) { song ->
                SongListItem(
                    song = song,
                    onClick = { musicPlayerViewModel.playSong(song, customPlaylist = songs) },
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

@Composable
fun AlbumsTab(
    albums: List<AlbumGroup>, 
    isLoading: Boolean,
    navController: NavController,
    musicPlayerViewModel: MusicPlayerViewModel
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (albums.isEmpty()) {
        EmptyLibraryState("No albums in your library")
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(albums) { album ->
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

@Composable
fun ArtistsTab(
    artists: List<ArtistGroup>, 
    isLoading: Boolean,
    navController: NavController,
    musicPlayerViewModel: MusicPlayerViewModel = viewModel()
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (artists.isEmpty()) {
        EmptyLibraryState("No artists in your library")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(artists) { artist ->
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

@Composable
fun PlaylistsTab(musicPlayerViewModel: MusicPlayerViewModel = viewModel()) {
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
    musicPlayerViewModel: MusicPlayerViewModel = viewModel()
) {
    val likedSongs by viewModel.likedSongs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (likedSongs.isEmpty()) {
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
                            text = "${likedSongs.size} songs",
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
                            if (likedSongs.isNotEmpty()) {
                                // Shuffle all liked songs
                                musicPlayerViewModel.playSong(likedSongs.random())
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
            
            // Songs list
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(likedSongs) { song ->
                    SongListItem(
                        song = song,
                        onClick = { musicPlayerViewModel.playSong(song) },
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

@Composable
fun SongListItem(
    song: Song,
    onClick: () -> Unit,
    navController: NavController? = null,
    musicPlayerViewModel: MusicPlayerViewModel? = null
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
            var showMenu by remember { mutableStateOf(false) }
            
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




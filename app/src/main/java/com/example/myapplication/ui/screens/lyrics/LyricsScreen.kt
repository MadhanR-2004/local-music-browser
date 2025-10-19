package com.example.myapplication.ui.screens.lyrics

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Lyrics Screen - Synced lyrics with auto-scroll
 * Material 3 Expressive Design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // TODO: Load lyrics from database or API
    val mockLyrics = remember {
        listOf(
            LyricLine(0, "First line of lyrics"),
            LyricLine(5000, "Second line of lyrics"),
            LyricLine(10000, "Third line of lyrics"),
            LyricLine(15000, "Fourth line of lyrics"),
            LyricLine(20000, "Fifth line of lyrics"),
            LyricLine(25000, "Sixth line of lyrics"),
            LyricLine(30000, "Seventh line of lyrics"),
            LyricLine(35000, "Eighth line of lyrics")
        )
    }
    
    // Mock current position
    var currentPosition by remember { mutableStateOf(0L) }
    val listState = rememberLazyListState()
    
    // Find current lyric index
    val currentIndex = mockLyrics.indexOfLast { it.timestamp <= currentPosition }
    
    // Auto-scroll effect
    LaunchedEffect(currentIndex) {
        if (currentIndex >= 0) {
            listState.animateScrollToItem(currentIndex)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lyrics") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    IconButton(onClick = { /* TODO: Search lyrics */ }) {
                        Icon(Icons.Default.Search, "Search")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            if (mockLyrics.isEmpty()) {
                // No lyrics available
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No lyrics available",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Lyrics will appear here when available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    itemsIndexed(mockLyrics) { index, lyric ->
                        LyricLineItem(
                            text = lyric.text,
                            isActive = index == currentIndex,
                            isPast = index < currentIndex
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(200.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun LyricLineItem(
    text: String,
    isActive: Boolean,
    isPast: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.1f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow), label = "scale"
    )
    
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
        color = when {
            isActive -> MaterialTheme.colorScheme.primary
            isPast -> MaterialTheme.colorScheme.onSurfaceVariant
            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        },
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

data class LyricLine(
    val timestamp: Long,
    val text: String
)

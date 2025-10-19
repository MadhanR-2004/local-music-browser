package com.example.myapplication.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Composable that displays album art from either a Bitmap or file path
 * Falls back to a music note icon if no art is available
 */
@Composable
fun AlbumArtImage(
    bitmap: Bitmap? = null,
    filePath: String? = null,
    contentDescription: String? = "Album Art",
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    when {
        // If bitmap is provided, use it directly
        bitmap != null -> {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = modifier.fillMaxSize(),
                contentScale = contentScale
            )
        }
        // If file path is provided, try to load embedded art
        filePath != null -> {
            var albumArtBitmap by remember(filePath) { mutableStateOf<Bitmap?>(null) }
            var isLoading by remember(filePath) { mutableStateOf(true) }
            
            LaunchedEffect(filePath) {
                isLoading = true
                albumArtBitmap = withContext(Dispatchers.IO) {
                    try {
                        val file = File(filePath)
                        if (file.exists()) {
                            // Extract embedded art using MediaMetadataRetriever
                            val mmr = android.media.MediaMetadataRetriever()
                            try {
                                mmr.setDataSource(filePath)
                                val art = mmr.embeddedPicture
                                if (art != null) {
                                    android.util.Log.d("AlbumArtImage", "Found embedded art for: $filePath")
                                    android.graphics.BitmapFactory.decodeByteArray(art, 0, art.size)
                                } else {
                                    android.util.Log.w("AlbumArtImage", "No embedded art for: $filePath")
                                    null
                                }
                            } finally {
                                mmr.release()
                            }
                        } else {
                            android.util.Log.w("AlbumArtImage", "File not found: $filePath")
                            null
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("AlbumArtImage", "Error loading album art from $filePath", e)
                        null
                    }
                }
                isLoading = false
            }
            
            when {
                isLoading -> {
                    // Loading placeholder
                    Box(
                        modifier = modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.tertiaryContainer
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                        )
                    }
                }
                albumArtBitmap != null -> {
                    // Display extracted bitmap
                    Image(
                        bitmap = albumArtBitmap!!.asImageBitmap(),
                        contentDescription = contentDescription,
                        modifier = modifier.fillMaxSize(),
                        contentScale = contentScale
                    )
                }
                else -> {
                    // No art available
                    AlbumArtPlaceholder(modifier = modifier.fillMaxSize())
                }
            }
        }
        // No bitmap or file path provided
        else -> {
            AlbumArtPlaceholder(modifier = modifier.fillMaxSize())
        }
    }
}

/**
 * Placeholder shown when no album art is available
 */
@Composable
private fun AlbumArtPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.tertiaryContainer
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
        )
    }
}


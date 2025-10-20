package com.example.myapplication.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import com.example.myapplication.utils.AlbumArtCache

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
            val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }
            Image(
                bitmap = imageBitmap,
                contentDescription = contentDescription,
                modifier = modifier.fillMaxSize(),
                contentScale = contentScale,
                filterQuality = FilterQuality.Low
            )
        }
        // If file path is provided, try to load embedded art
        filePath != null -> {
            var albumArtBitmap by remember(filePath) { mutableStateOf<Bitmap?>(AlbumArtCache.get(filePath)) }
            var isLoading by remember(filePath) { 
                mutableStateOf(albumArtBitmap == null && !AlbumArtCache.hasNoArt(filePath)) 
            }
            
            LaunchedEffect(filePath) {
                // Short-circuit if cache already had it
                val cached = AlbumArtCache.get(filePath)
                if (cached != null) {
                    albumArtBitmap = cached
                    isLoading = false
                } else if (AlbumArtCache.hasNoArt(filePath)) {
                    // We already know this file has no art
                    albumArtBitmap = null
                    isLoading = false
                } else {
                    isLoading = true
                    val decoded = withContext(Dispatchers.IO) {
                        try {
                            val file = File(filePath)
                            if (!file.exists() || !file.canRead()) {
                                android.util.Log.w("AlbumArtImage", "File not accessible: $filePath")
                                return@withContext null
                            }
                            
                            val mmr = android.media.MediaMetadataRetriever()
                            try {
                                mmr.setDataSource(filePath)
                                val art = mmr.embeddedPicture
                                if (art != null && art.size > 0) {
                                    android.util.Log.d("AlbumArtImage", "Found embedded art for: $filePath (${art.size} bytes)")
                                    val bitmap = android.graphics.BitmapFactory.decodeByteArray(art, 0, art.size)
                                    if (bitmap != null) {
                                        // Scale down large images to save memory
                                        val maxSize = 512
                                        if (bitmap.width > maxSize || bitmap.height > maxSize) {
                                            val scale = maxSize.toFloat() / maxOf(bitmap.width, bitmap.height)
                                            val scaledWidth = (bitmap.width * scale).toInt()
                                            val scaledHeight = (bitmap.height * scale).toInt()
                                            android.graphics.Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
                                        } else {
                                            bitmap
                                        }
                                    } else {
                                        android.util.Log.w("AlbumArtImage", "Failed to decode embedded art for: $filePath")
                                        null
                                    }
                                } else {
                                    android.util.Log.d("AlbumArtImage", "No embedded art for: $filePath")
                                    null
                                }
                            } finally {
                                try {
                                    mmr.release()
                                } catch (e: Exception) {
                                    android.util.Log.w("AlbumArtImage", "Error releasing MediaMetadataRetriever", e)
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("AlbumArtImage", "Error loading album art from $filePath: ${e.message}")
                            null
                        }
                    }
                    // Cache both hits and misses to avoid repeat work
                    AlbumArtCache.put(filePath, decoded)
                    albumArtBitmap = decoded
                    isLoading = false
                }
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
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
                albumArtBitmap != null -> {
                    // Display extracted bitmap
                    val imageBitmap = remember(albumArtBitmap) { albumArtBitmap!!.asImageBitmap() }
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = contentDescription,
                        modifier = modifier.fillMaxSize(),
                        contentScale = contentScale,
                        filterQuality = FilterQuality.Low
                    )
                }
                else -> {
                    // No art available - show colorful placeholder
                    AlbumArtPlaceholder(
                        modifier = modifier.fillMaxSize(),
                        filePath = filePath
                    )
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
 * Generates a colorful gradient based on the file path for consistency
 */
@Composable
private fun AlbumArtPlaceholder(
    modifier: Modifier = Modifier,
    filePath: String? = null
) {
    // Read theme colors in composable scope (allowed), then use them inside remember
    val colorScheme = MaterialTheme.colorScheme
    // Theme-aligned gradient that automatically follows dynamic colors
    val colors = remember(
        colorScheme.surfaceVariant,
        colorScheme.primaryContainer
    ) {
        listOf(
            colorScheme.surfaceVariant,
            colorScheme.primaryContainer
        )
    }
    
    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(colors)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(32.dp)
        )
    }
}

/**
 * Generate consistent colors based on file path
 */
private fun generateColorsFromPath(filePath: String): List<androidx.compose.ui.graphics.Color> {
    val hash = filePath.hashCode()
    val color1 = androidx.compose.ui.graphics.Color(
        red = ((hash and 0xFF0000) shr 16) / 255f,
        green = ((hash and 0x00FF00) shr 8) / 255f,
        blue = (hash and 0x0000FF) / 255f,
        alpha = 0.8f
    )
    val color2 = androidx.compose.ui.graphics.Color(
        red = ((hash and 0xFF0000) shr 16) / 255f * 0.7f,
        green = ((hash and 0x00FF00) shr 8) / 255f * 0.7f,
        blue = (hash and 0x0000FF) / 255f * 0.7f,
        alpha = 0.6f
    )
    return listOf(color1, color2)
}


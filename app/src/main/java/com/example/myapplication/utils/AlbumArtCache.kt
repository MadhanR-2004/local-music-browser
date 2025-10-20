package com.example.myapplication.utils

import android.graphics.Bitmap
import android.util.LruCache
import java.io.File
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Simple in-memory LRU cache for album art bitmaps keyed by file path.
 * This prevents repeated MediaMetadataRetriever work during list scrolls.
 */
object AlbumArtCache {
    // Allocate roughly 1/8th of available memory to the cache
    private val maxMemoryKb: Int = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSizeKb: Int = (maxMemoryKb / 8).coerceAtLeast(1024) // at least ~1MB

    private val cache = object : LruCache<String, Bitmap>(cacheSizeKb) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            // Cache size measured in kilobytes
            return (value.byteCount / 1024).coerceAtLeast(1)
        }
    }
    
    // Cache for "no art" results to avoid repeated MediaMetadataRetriever calls (thread-safe)
    private val noArtCache: MutableSet<String> = ConcurrentHashMap.newKeySet()

    private fun keyOf(path: String): String {
        val normalized = try {
            File(path).absolutePath
        } catch (_: Throwable) {
            path
        }
        return normalized.trim().lowercase(Locale.ROOT)
    }

    fun get(key: String): Bitmap? = cache.get(keyOf(key))
    
    fun hasNoArt(key: String): Boolean = noArtCache.contains(keyOf(key))

    fun put(key: String, bitmap: Bitmap?) {
        val k = keyOf(key)
        if (bitmap == null) {
            // Cache that this file has no art
            noArtCache.add(k)
            return
        }
        // Avoid overwriting identical entry needlessly
        if (cache.get(k) == null) cache.put(k, bitmap)
    }

    fun clear() {
        cache.evictAll()
        noArtCache.clear()
    }
}



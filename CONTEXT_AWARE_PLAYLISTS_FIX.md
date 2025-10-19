# Context-Aware Playlists - Next/Previous Respects Context

## Problem Solved

**User Question**: "what if I selected a song in the middle of an album and tries to play next, which song it plays?"

**Previous Behavior** ❌:
- Play song #5 from an album
- Click "Next" → Plays song #6 from **ALL songs** (not the next album song)
- Context was lost

**New Behavior** ✅:
- Play song #5 from an album
- Click "Next" → Plays song #6 from **the same album**
- Context is preserved!

---

## How It Works

### Context-Aware Playlist System

The `playSong()` function now accepts an optional `customPlaylist` parameter:

```kotlin
fun playSong(song: Song, customPlaylist: List<Song>? = null) {
    // Use custom playlist if provided, otherwise use all songs
    val playlist = customPlaylist ?: allSongs
    val songIndex = playlist.indexOfFirst { it.id == song.id }
    
    // Set this playlist in the service
    musicService?.setPlaylist(playlist, songIndex)
    ...
}
```

###  Different Contexts Use Different Playlists

| Context | Playlist Used | Next/Previous Behavior |
|---------|---------------|------------------------|
| **Library (all songs)** | All songs from database | Next song in library |
| **Album Detail** | Songs from that album only | Next song in album |
| **Artist Detail** | Songs from that artist only | Next song by artist |
| **Search Results** | All songs from database | Next song in library |
| **Home Screen** | All songs from database | Next song in library |

---

## Implementation Details

### 1. Core Function Update (`MusicPlayerViewModel.kt`)

```kotlin
fun playSong(song: Song, customPlaylist: List<Song>? = null) {
    val playlist = customPlaylist ?: allSongs  // Context-aware!
    val songIndex = playlist.indexOfFirst { it.id == song.id }
    
    musicService?.setPlaylist(playlist, songIndex)
    musicService?.playSong(song)
    ...
}
```

### 2. Fixed Song Matching (`MusicPlayerService.kt`)

Changed from **reference equality** to **ID-based equality**:

```kotlin
// OLD (Broken):
val foundIndex = playlist.indexOf(song)  // Uses ==, fails!

// NEW (Fixed):
val foundIndex = playlist.indexOfFirst { it.id == song.id }  // Compares IDs ✅
```

**Why this matters**: Song objects from different sources (database, service, ViewModel) are different instances but represent the same song. Comparing by `id` ensures we find the correct song.

### 3. Album Detail Screen

Songs in an album only cycle through that album:

```kotlin
// Individual song click
AlbumTrackItem(
    song = song,
    onClick = { viewModel.playSong(song, customPlaylist = songs) }
)

// Play Album button
Button(
    onClick = { 
        if (songs.isNotEmpty()) {
            viewModel.playSong(songs.first(), customPlaylist = songs)
        }
    }
)

// Shuffle Album button
FilledTonalButton(
    onClick = { 
        if (songs.isNotEmpty()) {
            val shuffled = songs.shuffled()
            viewModel.playSong(shuffled.first(), customPlaylist = shuffled)
        }
    }
)
```

### 4. Artist Detail Screen

Songs by an artist only cycle through that artist's songs:

```kotlin
// Individual song click
ArtistSongItem(
    song = song,
    onClick = { viewModel.playSong(song, customPlaylist = songs) }
)

// Play All button
FilledTonalButton(
    onClick = { 
        if (songs.isNotEmpty()) {
            viewModel.playSong(songs.first(), customPlaylist = songs)
        }
    }
)

// Shuffle button
FilledTonalButton(
    onClick = { 
        if (songs.isNotEmpty()) {
            val shuffled = songs.shuffled()
            viewModel.playSong(shuffled.first(), customPlaylist = shuffled)
        }
    }
)
```

### 5. Library & Other Screens

Default behavior uses all songs (no custom playlist):

```kotlin
// Library screen
SongListItem(
    song = song,
    onClick = { musicPlayerViewModel.playSong(song) }  // No custom playlist
)
```

---

## User Experience

### Scenario 1: Playing from Album

```
1. Open "Dark Side of the Moon" album
2. Click song #4: "Time"
3. Click Next → Plays song #5: "The Great Gig in the Sky" ✅
4. Click Next → Plays song #6: "Money" ✅
5. Stays within the album!
```

### Scenario 2: Playing from Artist

```
1. Open "Pink Floyd" artist page
2. Click any song
3. Click Next → Plays next Pink Floyd song ✅
4. All songs are by Pink Floyd
```

### Scenario 3: Playing from Library

```
1. Library → All Songs
2. Click any song
3. Click Next → Plays next song in library ✅
4. Cycles through all songs
```

### Scenario 4: Shuffle Album

```
1. Open any album
2. Click "Shuffle" button
3. Plays shuffled songs from that album only ✅
4. Next/Previous stay within shuffled album
```

---

## Technical Improvements

### 1. ✅ Song Matching by ID
- Old: `playlist.indexOf(song)` - failed due to reference inequality
- New: `playlist.indexOfFirst { it.id == song.id }` - works reliably

### 2. ✅ Optional Custom Playlist
- Backward compatible: existing code works without changes
- Context-aware: passing custom playlist preserves context
- Flexible: can be used for any filtered list

### 3. ✅ Logging for Debugging
```kotlin
Log.d(TAG, "Playing song: ${song.title} at index $songIndex of ${playlist.size} songs (custom playlist: ${customPlaylist != null})")
```

---

## Testing Checklist

Test these scenarios:

1. **Album Context**:
   - ✅ Click song in album → Next plays next album song
   - ✅ Click "Play" button → Plays album from start
   - ✅ Click "Shuffle" button → Shuffles album only

2. **Artist Context**:
   - ✅ Click song by artist → Next plays next artist song
   - ✅ Click "Play" button → Plays all artist songs
   - ✅ Click "Shuffle" button → Shuffles artist songs only

3. **Library Context**:
   - ✅ Click song in library → Next plays next library song
   - ✅ Cycles through all songs in database

4. **Search Context**:
   - ✅ Click song in search → Next plays next song in library
   - ✅ Works correctly

5. **Home Context**:
   - ✅ Click song on home screen → Next plays next song
   - ✅ Works correctly

---

## Summary

| Feature | Status | Details |
|---------|--------|---------|
| Context-aware playlists | ✅ Implemented | Different contexts use appropriate playlists |
| Album navigation | ✅ Working | Next/Previous stay within album |
| Artist navigation | ✅ Working | Next/Previous stay within artist |
| Library navigation | ✅ Working | Next/Previous cycle through all songs |
| Song matching fix | ✅ Fixed | Uses ID comparison instead of reference |
| Play Album button | ✅ Working | Plays from album context |
| Shuffle Album button | ✅ Working | Shuffles album only |

Your music player now intelligently respects the context in which you're playing music! 🎵



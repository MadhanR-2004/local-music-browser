# 🔍 Debugging Guide - Songs & Album Art Not Showing

## Quick Diagnosis

Run these batch files in order:

### 1. Check if songs are in database
```cmd
check_database.bat
```

**Expected output:**
- Database file exists
- Song count > 0
- Shows list of songs with paths
- Shows selected folders

**If song count is 0:**
- Folders weren't scanned
- No audio files in selected folders
- Permission issues

### 2. Watch logs in real-time
```cmd
watch_logs.bat
```

Then launch the app and watch for:
- `PathBasedScanner: Starting scan of X folders`
- `PathBasedScanner: Found X songs in [path]`
- `HomeViewModel: Loaded X songs from database`
- `MusicPlayerViewModel: Loading album art from: [path]`

### 3. Test album art extraction
```cmd
test_album_art.bat
```

Checks if:
- Song files are accessible
- File permissions are correct
- Paths are valid

## Common Issues & Fixes

### Issue 1: No Songs in Database

**Symptoms:**
- Empty home screen
- Mini-player doesn't show
- Database query returns 0 songs

**Diagnosis:**
```cmd
adb logcat | findstr "PathBasedScanner"
```

**Possible Causes:**

1. **Folders don't exist:**
   ```
   PathBasedScanner: Folder does not exist: /storage/emulated/0/Music
   ```
   **Fix:** The folders in onboarding are hardcoded. Your device might use different paths.

2. **Permission denied:**
   ```
   PathBasedScanner: Cannot read directory: /storage/emulated/0/Music
   ```
   **Fix:** Grant storage permissions manually in Settings → Apps → Music Player → Permissions

3. **No audio files:**
   ```
   PathBasedScanner: Found 0 songs in [folder]
   ```
   **Fix:** Check if folders actually contain MP3/FLAC/M4A files

### Issue 2: Album Art Not Showing

**Symptoms:**
- Songs load but show music note icon
- No album art in mini-player

**Diagnosis:**
```cmd
adb logcat | findstr "MusicPlayerViewModel.*album"
```

**Expected logs:**
```
MusicPlayerViewModel: Loading album art from: /storage/emulated/0/Music/song.mp3
MusicPlayerViewModel: Found embedded art, size: 45678 bytes
MusicPlayerViewModel: Album art decoded: 500x500
```

**If you see:**
```
MusicPlayerViewModel: No embedded art found in file
```
**Fix:** Your MP3 files don't have embedded album art. Test with a file that has album art.

**If you see:**
```
MusicPlayerViewModel: Failed to load album art: Permission denied
```
**Fix:** File permissions issue. The app can't read the audio file.

### Issue 3: Songs Not Playable

**Current Status:** ❌ **ExoPlayer not integrated yet**

The playback controls are connected to the ViewModel, but there's no actual audio player backend. This needs to be implemented.

**What Works:**
- Previous/Next buttons cycle through songs in database
- Play/Pause button toggles state
- Progress bar updates (but no actual playback)

**What Doesn't Work:**
- No sound plays
- No real-time progress tracking
- No media session controls

## Manual Testing Steps

### Test 1: Verify Folder Paths

1. Connect device via ADB
2. Check what music folders exist:
```cmd
adb shell "ls -la /storage/emulated/0/"
```

3. Look for folders like:
   - Music
   - Download
   - Documents
   - WhatsApp/Media/WhatsApp Audio

4. List audio files in a folder:
```cmd
adb shell "find /storage/emulated/0/Music -name '*.mp3' | head -n 5"
```

### Test 2: Manual Database Insert

If scanning isn't working, manually insert a test song:

```cmd
adb shell "run-as com.example.myapplication sqlite3 databases/music_database \"INSERT INTO songs (title, artist, album, path, durationMs, trackNumber, year, playCount, dateAddedEpochMs) VALUES ('Test Song', 'Test Artist', 'Test Album', '/storage/emulated/0/Music/test.mp3', 180000, 1, 2024, 0, 1234567890000);\""
```

Then restart the app.

### Test 3: Force Rescan

Clear app data and go through onboarding again:

```cmd
adb shell pm clear com.example.myapplication
adb shell am start -n com.example.myapplication/.MainActivityCompose
```

Watch logs during onboarding:
```cmd
adb logcat -c
adb logcat | findstr /I "Onboarding PathBasedScanner"
```

## Log Analysis

### Good Scan Log Example:
```
Onboarding: Saved 7 folders: [/storage/emulated/0/Music, ...]
Onboarding: Starting music scan of 7 folders...
PathBasedScanner: Starting scan of 7 folders
PathBasedScanner: Scanning folder 1/7: /storage/emulated/0/Music
PathBasedScanner: Found 142 songs in /storage/emulated/0/Music
PathBasedScanner: Scan complete: 423 songs from 7 folders (0 skipped)
HomeViewModel: Loaded 423 songs from database
MusicPlayerViewModel: Loaded 423 songs from database
MusicPlayerViewModel: Loading album art from: /storage/emulated/0/Music/song.mp3
```

### Bad Scan Log Example:
```
Onboarding: Saved 7 folders: [/storage/emulated/0/Music, ...]
Onboarding: Starting music scan of 7 folders...
PathBasedScanner: Starting scan of 7 folders
PathBasedScanner: Folder does not exist: /storage/emulated/0/Music
PathBasedScanner: Folder does not exist: /storage/emulated/0/Download
PathBasedScanner: Scan complete: 0 songs from 0 folders (7 skipped)
HomeViewModel: Loaded 0 songs from database
MusicPlayerViewModel: No songs in database
```

## Next Steps

### If Songs Still Don't Load:

1. **Use SAF (Storage Access Framework) instead:**
   - Current implementation uses hardcoded paths
   - Should use `ACTION_OPEN_DOCUMENT_TREE` to let user pick folders
   - This guarantees permissions

2. **Use MediaStore API:**
   - Query all audio files from MediaStore
   - Doesn't require folder selection
   - Works on all Android versions

### If Album Art Doesn't Load:

1. **Test with known-good files:**
   - Download MP3 with embedded album art
   - Put in /storage/emulated/0/Download
   - Rescan

2. **Add ContentResolver support:**
   - Current code only works with file:// paths
   - Should support content:// URIs

### For Playback:

ExoPlayer integration is needed. This requires:
1. Creating PlayerManager to wrap ExoPlayer
2. Connecting MusicPlayerViewModel to PlayerManager
3. Implementing MediaSession for lock screen controls
4. Adding play/pause/skip functionality

## Contact/Support

If issues persist, collect logs and share:

```cmd
adb logcat -d > music_player_logs.txt
```

Include:
- Device model
- Android version
- Exact error messages
- Database query results from check_database.bat








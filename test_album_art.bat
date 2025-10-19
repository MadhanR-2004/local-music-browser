@echo off
echo ========================================
echo Testing Album Art Extraction
echo ========================================
echo.

echo Getting first song path from database...
for /f "delims=" %%i in ('adb shell "run-as com.example.myapplication sqlite3 databases/music_database \"SELECT path FROM songs LIMIT 1;\""') do set SONG_PATH=%%i

echo Song path: %SONG_PATH%
echo.

echo Checking if file exists on device...
adb shell "ls -la %SONG_PATH%" 2>nul

echo.
echo Checking file permissions...
adb shell "stat %SONG_PATH%" 2>nul

echo.
pause






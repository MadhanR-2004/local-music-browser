@echo off
echo ========================================
echo Checking Music Player Database
echo ========================================
echo.

echo 1. Checking if database exists...
adb shell "run-as com.example.myapplication ls -la databases/" 2>nul
if errorlevel 1 (
    echo ERROR: Cannot access app database. Is the app installed?
    pause
    exit /b 1
)

echo.
echo 2. Checking song count in database...
adb shell "run-as com.example.myapplication sqlite3 databases/music_database 'SELECT COUNT(*) as song_count FROM songs;'" 2>nul

echo.
echo 3. Checking first 5 songs...
adb shell "run-as com.example.myapplication sqlite3 databases/music_database 'SELECT title, artist, path FROM songs LIMIT 5;'" 2>nul

echo.
echo 4. Checking selected folders from SharedPreferences...
adb shell "run-as com.example.myapplication cat shared_prefs/app_prefs.xml" 2>nul

echo.
echo 5. Checking if onboarding is complete...
adb shell "run-as com.example.myapplication cat shared_prefs/app_prefs.xml | findstr onboarding_done" 2>nul

echo.
echo ========================================
echo Diagnosis Complete
echo ========================================
pause





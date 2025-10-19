@echo off
echo ========================================
echo Music Player Debug Script
echo ========================================
echo.

echo Checking if app is installed...
adb shell pm list packages | findstr "com.example.myapplication"
if %errorlevel% neq 0 (
    echo ERROR: App not found! Please install the app first.
    pause
    exit /b 1
)

echo.
echo App found! Checking database...
echo.

echo Checking if songs table exists and has data...
adb shell "run-as com.example.myapplication sqlite3 /data/data/com.example.myapplication/databases/app_database 'SELECT COUNT(*) FROM songs;'"

echo.
echo Checking recent logs for music scanning...
adb logcat -d | findstr -i "InitialLoadInitializer\|MediaStoreScanner\|FolderScanner\|music\|song" | findstr -v "System.err"

echo.
echo Checking current app state...
adb shell "run-as com.example.myapplication ls -la /data/data/com.example.myapplication/databases/"

echo.
echo ========================================
echo Debug complete!
echo ========================================
echo.
echo If you see 0 songs in the database, the issue is with music scanning.
echo If you see songs but the app doesn't show them, the issue is with the UI.
echo.
pause


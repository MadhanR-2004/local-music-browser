@echo off
echo ========================================
echo Music Scanning Test Script
echo ========================================
echo.

echo Building the app...
call gradlew assembleDebug
if %errorlevel% neq 0 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo Installing the app...
adb install -r app\build\outputs\apk\debug\app-debug.apk
if %errorlevel% neq 0 (
    echo ERROR: Installation failed!
    pause
    exit /b 1
)

echo.
echo App installed successfully!
echo.
echo Now please:
echo 1. Open the app
echo 2. Go through onboarding (grant permissions)
echo 3. Check if songs appear in the Library tab
echo 4. Check the logs below for debugging info
echo.

echo Checking logs for music scanning...
adb logcat -c
echo.
echo Starting log monitoring (press Ctrl+C to stop)...
echo Look for messages containing: InitialLoadInitializer, MediaStoreScanner
echo.
adb logcat | findstr -i "InitialLoadInitializer\|MediaStoreScanner\|music\|song"

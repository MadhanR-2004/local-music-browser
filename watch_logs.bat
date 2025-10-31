@echo off
echo ========================================
echo Watching Music Player Logs
echo Press Ctrl+C to stop
echo ========================================
echo.

REM Clear old logs
adb logcat -c

REM Watch relevant logs
adb logcat -v time | findstr /I "PathBasedScanner InitialLoadInitializer HomeViewModel MusicPlayerViewModel Onboarding"












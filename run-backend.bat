@echo off
title Backend Server SIPKA Polres Sukoharjo (Port 8081)
color 0A
echo ============================================================
echo   SERVER BACKEND SPRING BOOT SIPKA POLRES SUKOHARJO
echo ============================================================
echo.
echo Memulai server backend pada Port 8081...
echo Tekan Ctrl + C jika ingin menghentikan server.
echo.
cd /d "%~dp0backend"
"C:\Program Files\Android\Android Studio\jbr\bin\java.exe" -jar "target\backend-0.0.1-SNAPSHOT.jar"
pause
 
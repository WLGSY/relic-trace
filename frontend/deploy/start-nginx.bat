@echo off
chcp 65001 >nul
cd /d "%~dp0nginx-1.28.0"

echo ==========================================
echo   Relic Trace - Nginx Start (port 80)
echo ==========================================
echo.

tasklist /FI "IMAGENAME eq nginx.exe" 2>nul | find /I "nginx.exe" >nul
if not errorlevel 1 (
    echo [INFO] nginx is already running. Reloading configuration...
    nginx.exe -s reload
    goto done
)

start "" nginx.exe
timeout /t 2 >nul

tasklist /FI "IMAGENAME eq nginx.exe" 2>nul | find /I "nginx.exe" >nul
if errorlevel 1 (
    echo [ERROR] nginx failed to start. Check logs\error.log
    pause
    exit /b 1
)

:done
echo.
echo   Frontend : http://localhost
echo   API      : http://localhost/api
echo.
echo   Make sure Spring Boot is running on port 8080 first.
echo.
pause

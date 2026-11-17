@echo off
chcp 65001 >nul
cd /d "%~dp0nginx-1.28.0"

tasklist /FI "IMAGENAME eq nginx.exe" 2>nul | find /I "nginx.exe" >nul
if errorlevel 1 (
    echo [INFO] nginx is not running.
    pause
    exit /b 0
)

nginx.exe -s quit
timeout /t 2 >nul
echo [OK] nginx stopped.
echo.
pause

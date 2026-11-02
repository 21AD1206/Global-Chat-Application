@echo off
REM ==================================================================
REM  ChatSpace one-click launcher for Windows
REM  1. checks Java and Node.js are installed
REM  2. installs frontend packages (first run only)
REM  3. opens a window for the backend  (Spring Boot, port 8080)
REM  4. opens a window for the frontend (Vite/React, port 5173)
REM  5. waits until the backend answers, then opens the browser
REM  To stop the app: close the two windows (or press Ctrl+C in them).
REM ==================================================================
setlocal
title ChatSpace launcher
cd /d "%~dp0"

echo.
echo ===== ChatSpace launcher =====
echo.

where java >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Java was not found. Install JDK 17 or newer: https://adoptium.net
    echo         Then close this window, open a NEW terminal and run start.bat again.
    pause
    exit /b 1
)
where npm >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Node.js / npm was not found. Install Node.js 18 or newer: https://nodejs.org
    echo         Then close this window, open a NEW terminal and run start.bat again.
    pause
    exit /b 1
)

echo Java version:
java -version
echo Node version:
node -v
echo.

if not exist "frontend\node_modules" (
    echo [1/3] Installing frontend packages - first run only, takes about 1 minute...
    pushd frontend
    call npm install
    if errorlevel 1 (
        popd
        echo [ERROR] npm install failed. Check your internet connection and try again.
        pause
        exit /b 1
    )
    popd
) else (
    echo [1/3] Frontend packages already installed.
)

echo [2/3] Starting backend in a new window...
echo       First run downloads Maven and libraries - this can take 3-5 minutes.
start "ChatSpace BACKEND - port 8080" /D "%~dp0backend" cmd /k mvnw.cmd spring-boot:run

echo [3/3] Starting frontend in a new window...
start "ChatSpace FRONTEND - port 5173" /D "%~dp0frontend" cmd /k npm run dev

echo.
echo Waiting for the backend to be ready on http://localhost:8080 ...
set /a tries=0
:wait
timeout /t 3 /nobreak >nul
set /a tries+=1
curl -s -o nul http://localhost:8080/api/presence
if not errorlevel 1 goto ready
if %tries% GEQ 200 goto slow
goto wait

:slow
echo.
echo The backend is taking unusually long. Look at the BACKEND window for errors.
echo When it shows "Started ChatApplication", open http://localhost:5173 yourself.
pause
exit /b 0

:ready
echo.
echo ==============================================================
echo   ChatSpace is running!
echo   App:          http://localhost:5173
echo   Backend API:  http://localhost:8080
echo   DB console:   http://localhost:8080/h2-console
echo   To stop: close the BACKEND and FRONTEND windows.
echo ==============================================================
start "" http://localhost:5173
echo.
pause

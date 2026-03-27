@echo off
setlocal EnableDelayedExpansion

set "TEMPLATE_ROOT=%~dp0"
set "BACKEND_DIR=%TEMPLATE_ROOT%backend"
set "FRONTEND_DIR=%TEMPLATE_ROOT%frontend"
set "BACKEND_START_SCRIPT=%BACKEND_DIR%\scripts\start-backend-background.ps1"
set "JDK_HOME=%BACKEND_DIR%\.jdks\temurin-21-unpacked\jdk-21.0.10+7"

if not exist "%JDK_HOME%\bin\java.exe" (
  echo [travel-platform] JDK 21 was not found at:
  echo %JDK_HOME%
  pause
  exit /b 1
)

call :ensure_backend
call :ensure_frontend
call :open_when_ready

echo [travel-platform] Project launch has been triggered. The page will open automatically when services are ready.
exit /b 0

:ensure_backend
powershell -NoProfile -Command ^
  "try { $resp = Invoke-WebRequest -Uri 'http://localhost:8080/api/health' -UseBasicParsing -TimeoutSec 3; if ($resp.StatusCode -eq 200) { exit 0 } else { exit 1 } } catch { exit 1 }"
if %errorlevel%==0 (
  echo [travel-platform] Backend is already running.
  exit /b 0
)

echo [travel-platform] Starting backend with JDK 21 in database mode ...
start "" powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%BACKEND_START_SCRIPT%" -RepositoryMode database
exit /b 0

:ensure_frontend
powershell -NoProfile -Command ^
  "try { $resp = Invoke-WebRequest -Uri 'http://localhost:5173' -UseBasicParsing -TimeoutSec 3; if ($resp.StatusCode -ge 200) { exit 0 } else { exit 1 } } catch { exit 1 }"
if %errorlevel%==0 (
  echo [travel-platform] Frontend is already running.
  exit /b 0
)

echo [travel-platform] Starting frontend dev server ...
start "" cmd.exe /c "cd /d %FRONTEND_DIR% && npm.cmd run dev -- --host 0.0.0.0"
exit /b 0

:open_when_ready
start "" powershell.exe -NoProfile -WindowStyle Hidden -Command ^
  "$backendReady = $false; $frontendReady = $false; for ($i = 0; $i -lt 45; $i++) { if (-not $backendReady) { try { $resp = Invoke-WebRequest -Uri 'http://localhost:8080/api/health' -UseBasicParsing -TimeoutSec 3; if ($resp.StatusCode -eq 200) { $backendReady = $true } } catch {} }; if (-not $frontendReady) { try { $resp = Invoke-WebRequest -Uri 'http://localhost:5173' -UseBasicParsing -TimeoutSec 3; if ($resp.StatusCode -ge 200) { $frontendReady = $true } } catch {} }; if ($backendReady -and $frontendReady) { Start-Process 'http://localhost:5173'; exit 0 }; Start-Sleep -Seconds 2 }; Start-Process 'http://localhost:5173'"
exit /b 0

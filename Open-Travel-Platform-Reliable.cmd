@echo off
setlocal
title Typesafe Travel Platform Launcher

for %%I in ("%~dp0.") do set "REPO_ROOT=%%~fI"

if defined JAVA_HOME set "PATH=%JAVA_HOME%\bin;%PATH%"
if defined SBT_HOME set "PATH=%SBT_HOME%\bin;%PATH%"
if defined NODE_HOME set "PATH=%NODE_HOME%;%PATH%"
if defined NPM_CONFIG_PREFIX set "PATH=%NPM_CONFIG_PREFIX%;%PATH%"

echo Starting Typesafe Travel Platform...
echo.
echo The backend may need 60-90 seconds on the first launch while Scala modules compile.
echo A browser window will open after the app is ready.
echo.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\start-travel-platform-stable.ps1" -BackendPort 19095 -FrontendPort 5174
if errorlevel 1 (
  echo.
  echo Startup failed. Please keep this window open and check:
  echo %~dp0.launcher-logs\launcher-stable.log
  echo %~dp0.launcher-logs\backend.stderr.log
  echo.
  pause
)
endlocal

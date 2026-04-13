@echo off
setlocal
title Typesafe Travel Platform Launcher

set "JAVA_HOME=C:\typesafe\tools\jdk-22"
set "SBT_HOME=C:\typesafe\sbt"
set "NODE_HOME=C:\typesafe\tools\nodejs"
set "NPM_CONFIG_PREFIX=C:\typesafe\npm-global"
set "PATH=C:\typesafe\Git\cmd;C:\typesafe\sbt\bin;C:\typesafe\tools\jdk-22\bin;C:\typesafe\tools\nodejs;C:\typesafe\npm-global;C:\typesafe\tools\postgresql-18\bin;%PATH%"

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

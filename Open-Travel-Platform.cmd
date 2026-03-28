@echo off
set "LAUNCH_SCRIPT=%~dp0scripts\launch-travel-platform.ps1"
start "" /min powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%LAUNCH_SCRIPT%"

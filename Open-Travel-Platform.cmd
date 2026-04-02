@echo off
start "" /min powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\start-travel-platform-clean.ps1" -BackendPort 19095 -FrontendPort 5174

@echo off
start "" /min powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0backend\scripts\start-backend.ps1" -RepositoryMode database -BackendPort 19095
start "" /min cmd.exe /c "%~dp0frontend\scripts\start-frontend-dev.cmd"
start "" /min powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\launch-travel-platform.ps1" -BackendPort 19095 -FrontendPort 5173

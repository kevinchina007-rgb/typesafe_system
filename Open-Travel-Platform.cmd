@echo off
start "" /min powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0backend\scripts\start-backend.ps1" -RepositoryMode database -BackendPort 19095
start "" /min powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0frontend\scripts\serve-frontend.ps1" -Port 5173 -BackendOrigin http://localhost:19095
start "" /min powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\launch-travel-platform.ps1" -BackendPort 19095 -FrontendPort 5173

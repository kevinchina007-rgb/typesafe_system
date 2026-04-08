@echo off
setlocal

set FRONTEND_PORT=%~1
if "%FRONTEND_PORT%"=="" set FRONTEND_PORT=19173

set BACKEND_ORIGIN=%~2
if "%BACKEND_ORIGIN%"=="" set BACKEND_ORIGIN=http://localhost:19095

cd /d E:\typesafe\template\frontend

powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "[System.IO.File]::WriteAllText('E:\typesafe\template\frontend\dist\runtime-config.js', \"window.__TRAVEL_BACKEND_ORIGIN__ = '%BACKEND_ORIGIN%';\", [System.Text.Encoding]::UTF8)"

cd /d E:\typesafe\template\frontend\dist
python -m http.server %FRONTEND_PORT% --bind 0.0.0.0

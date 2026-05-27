@echo off
setlocal

for %%I in ("%~dp0..") do set "FRONTEND_ROOT=%%~fI"

cd /d "%FRONTEND_ROOT%"
npm.cmd run dev -- --host 0.0.0.0

@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "JAVA_HOME_RESOLVED="
if defined JAVA_HOME (
  if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_HOME_RESOLVED=%JAVA_HOME%"
)

if not defined JAVA_HOME_RESOLVED (
  for /d %%D in (
    "%USERPROFILE%\.jdks\*"
    "%USERPROFILE%\.java\*"
    "%ProgramFiles%\Java\*"
    "%ProgramFiles%\Eclipse Adoptium\*"
    "%ProgramFiles%\Eclipse Temurin\*"
    "%ProgramFiles(x86)%\Java\*"
  ) do (
    if not defined JAVA_HOME_RESOLVED if exist "%%~fD\bin\java.exe" set "JAVA_HOME_RESOLVED=%%~fD"
  )
)

if defined JAVA_HOME_RESOLVED (
  set "JAVA_HOME=%JAVA_HOME_RESOLVED%"
  if exist "%JAVA_HOME%\bin" set "PATH=%JAVA_HOME%\bin;%PATH%"
)

set "SBT_BAT="
if defined SBT_HOME (
  if exist "%SBT_HOME%\bin\sbt.bat" (
    set "SBT_HOME=%SBT_HOME%"
    set "SBT_BAT=%SBT_HOME%\bin\sbt.bat"
  )
)

if not defined SBT_BAT (
  if exist "%~dp0..\tools\sbt\bin\sbt.bat" (
    set "SBT_HOME=%~dp0..\tools\sbt"
    set "SBT_BAT=%~dp0..\tools\sbt\bin\sbt.bat"
  )
)

if not defined SBT_BAT (
  for %%D in (
    "%USERPROFILE%\tools\sbt\*\sbt"
    "%USERPROFILE%\Downloads\sbt-*\sbt"
    "%LOCALAPPDATA%\Programs\sbt\*\sbt"
    "C:\Program Files\sbt\*\sbt"
    "C:\Program Files (x86)\sbt\*\sbt"
  ) do (
    if not defined SBT_BAT if exist "%%~fD\bin\sbt.bat" (
      set "SBT_HOME=%%~fD"
      set "SBT_BAT=%%~fD\bin\sbt.bat"
    )
  )
)

if not defined SBT_BAT (
  for /f "delims=" %%I in ('where sbt.bat 2^>nul') do (
    if not defined SBT_BAT (
      set "SBT_BAT=%%I"
      set "SBT_HOME=%%~dpI.."
    )
  )
)

if not defined SBT_HOME if defined SBT_BAT set "SBT_HOME=!SBT_BAT:\bin\sbt.bat=!"

if not defined SBT_BAT (
  echo sbt was not found. Set SBT_HOME or install sbt on PATH.
  exit /b 1
)

call "%SBT_BAT%" %*
exit /b %errorlevel%

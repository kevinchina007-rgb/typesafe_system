@echo off
setlocal
if defined JAVA_HOME (
  if exist "%JAVA_HOME%\bin" set "PATH=%JAVA_HOME%\bin;%PATH%"
)

if defined SBT_HOME (
  if exist "%SBT_HOME%\bin\sbt.bat" (
    call "%SBT_HOME%\bin\sbt.bat" %*
    exit /b %errorlevel%
  )
)

if exist "%~dp0..\tools\sbt\bin\sbt.bat" (
  call "%~dp0..\tools\sbt\bin\sbt.bat" %*
  exit /b %errorlevel%
)

echo sbt was not found. Set SBT_HOME or install sbt on PATH.
exit /b 1

@echo off
setlocal

set REPOSITORY_MODE=%~1
if "%REPOSITORY_MODE%"=="" set REPOSITORY_MODE=database
set BACKEND_PORT=%~2
if "%BACKEND_PORT%"=="" set BACKEND_PORT=19095

for %%I in ("%~dp0..\..") do set "REPO_ROOT=%%~fI"

if defined JAVA_HOME (
  set "PATH=%JAVA_HOME%\bin;%PATH%"
)

set "TRAVEL_REPOSITORY_MODE=%REPOSITORY_MODE%"
set "TRAVEL_BACKEND_PORT=%BACKEND_PORT%"
set "SBT_OPTS=-Dsbt.boot.directory=%REPO_ROOT%\backend\.sbt-home\boot -Dsbt.global.base=%REPO_ROOT%\backend\.sbt-home -Dsbt.ivy.home=%REPO_ROOT%\backend\.ivy2 -Divy.home=%REPO_ROOT%\backend\.ivy2 -Dcoursier.cache=%REPO_ROOT%\backend\.coursier -Dsbt.coursier.home=%REPO_ROOT%\backend\.coursier -Dsbt.repository.config=%REPO_ROOT%\backend\project\repositories -Dsbt.override.build.repos=true -Dsbt.supershell=false -Dsbt.ci=true -Dsbt.server.autostart=false"
set "COURSIER_CACHE=%REPO_ROOT%\backend\.coursier"
set "COURSIER_ARCHIVE_CACHE=%REPO_ROOT%\backend\.coursier\archive"
set "COURSIER_JVM_CACHE=%REPO_ROOT%\backend\.coursier\jvm"

cd /d "%REPO_ROOT%\backend"
call "%REPO_ROOT%\bin\sbt.cmd" --batch run

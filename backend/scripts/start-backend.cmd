@echo off
setlocal

set REPOSITORY_MODE=%~1
if "%REPOSITORY_MODE%"=="" set REPOSITORY_MODE=database

set "JAVA_HOME=E:\typesafe\template\backend\.jdks\temurin-21-unpacked\jdk-21.0.10+7"
set "Path=%JAVA_HOME%\bin;%Path%"
set "TRAVEL_REPOSITORY_MODE=%REPOSITORY_MODE%"
set "SBT_OPTS=-Dsbt.boot.directory=E:/typesafe/template/backend/.sbt-home/boot -Dsbt.global.base=E:/typesafe/template/backend/.sbt-home -Dsbt.ivy.home=E:/typesafe/template/backend/.ivy2 -Divy.home=E:/typesafe/template/backend/.ivy2 -Dcoursier.cache=E:/typesafe/template/backend/.coursier -Dsbt.coursier.home=E:/typesafe/template/backend/.coursier -Dsbt.repository.config=E:/typesafe/template/backend/project/repositories -Dsbt.override.build.repos=true -Dsbt.supershell=false -Dsbt.ci=true -Dsbt.server.autostart=false"
set "COURSIER_CACHE=E:/typesafe/template/backend/.coursier"
set "COURSIER_ARCHIVE_CACHE=E:/typesafe/template/backend/.coursier/archive"
set "COURSIER_JVM_CACHE=E:/typesafe/template/backend/.coursier/jvm"

cd /d E:\typesafe\template\backend
sbt --batch run

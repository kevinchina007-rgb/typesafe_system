@echo off
setlocal
set "SBT_HOME=C:\Users\X1\tools\sbt\1.10.0\sbt"
set "JAVA_HOME=C:\Users\X1\.jdks\openjdk-25.0.2"
set "PATH=%JAVA_HOME%\bin;%PATH%"
call "C:\Users\X1\tools\sbt\1.10.0\sbt\bin\sbt.bat" %*

$env:JAVA_HOME = 'E:\typesafe\template\backend\.jdks\temurin-21-unpacked\jdk-21.0.10+7'
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
$env:SBT_OPTS = '-Dsbt.boot.directory=E:/typesafe/template/backend/.sbt-home/boot -Dsbt.global.base=E:/typesafe/template/backend/.sbt-home -Dsbt.ivy.home=E:/typesafe/template/backend/.ivy2 -Divy.home=E:/typesafe/template/backend/.ivy2 -Dcoursier.cache=E:/typesafe/template/backend/.coursier -Dsbt.coursier.home=E:/typesafe/template/backend/.coursier -Dsbt.repository.config=E:/typesafe/template/backend/project/repositories -Dsbt.override.build.repos=true -Dsbt.supershell=false -Dsbt.ci=true -Dsbt.server.autostart=false'
$env:COURSIER_CACHE = 'E:/typesafe/template/backend/.coursier'
$env:COURSIER_ARCHIVE_CACHE = 'E:/typesafe/template/backend/.coursier/archive'
$env:COURSIER_JVM_CACHE = 'E:/typesafe/template/backend/.coursier/jvm'

Set-Location 'E:\typesafe\template\backend'
sbt --batch run

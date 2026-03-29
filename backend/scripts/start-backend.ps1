param(
  [ValidateSet('database', 'in-memory')]
  [string]$RepositoryMode = 'database',
  [int]$BackendPort = 19095
)

$templateRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$logDir = Join-Path $templateRoot '.launcher-logs'
New-Item -ItemType Directory -Path $logDir -Force | Out-Null
$backendScriptLog = Join-Path $logDir 'backend-script.log'
$backendRunLog = Join-Path $logDir 'backend-run.log'

$env:JAVA_HOME = 'E:\typesafe\template\backend\.jdks\temurin-21-unpacked\jdk-21.0.10+7'
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
$env:TRAVEL_REPOSITORY_MODE = $RepositoryMode
$env:TRAVEL_BACKEND_PORT = "$BackendPort"
$env:SBT_OPTS = '-Dsbt.boot.directory=E:/typesafe/template/backend/.sbt-home/boot -Dsbt.global.base=E:/typesafe/template/backend/.sbt-home -Dsbt.ivy.home=E:/typesafe/template/backend/.ivy2 -Divy.home=E:/typesafe/template/backend/.ivy2 -Dcoursier.cache=E:/typesafe/template/backend/.coursier -Dsbt.coursier.home=E:/typesafe/template/backend/.coursier -Dsbt.repository.config=E:/typesafe/template/backend/project/repositories -Dsbt.override.build.repos=true -Dsbt.supershell=false -Dsbt.ci=true -Dsbt.server.autostart=false'
$env:COURSIER_CACHE = 'E:/typesafe/template/backend/.coursier'
$env:COURSIER_ARCHIVE_CACHE = 'E:/typesafe/template/backend/.coursier/archive'
$env:COURSIER_JVM_CACHE = 'E:/typesafe/template/backend/.coursier/jvm'

Add-Content -Path $backendScriptLog -Value "[backend] start script entered $(Get-Date -Format o) mode=$RepositoryMode port=$BackendPort"
Set-Location 'E:\typesafe\template\backend'
& 'E:\typesafe\bin\sbt.bat' --batch run *>&1 | Tee-Object -FilePath $backendRunLog -Append | Out-Null
$exitCode = if ($LASTEXITCODE -ne $null) { $LASTEXITCODE } else { 0 }
Add-Content -Path $backendScriptLog -Value "[backend] sbt exited $(Get-Date -Format o) code=$exitCode"
exit $exitCode

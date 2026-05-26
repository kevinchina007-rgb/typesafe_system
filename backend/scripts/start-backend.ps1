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
$backendErrorLog = Join-Path $logDir 'backend-error.log'

$env:JAVA_HOME = 'E:\typesafe\template\backend\.jdks\temurin-21-unpacked\jdk-21.0.10+7'
$env:TRAVEL_REPOSITORY_MODE = $RepositoryMode
$env:TRAVEL_BACKEND_PORT = "$BackendPort"
$launcherDatabasePath = (Join-Path $templateRoot 'backend\data\travel-platform-runtime').Replace('\', '/')
$env:TRAVEL_DB_URL = "jdbc:postgresql://127.0.0.1:5432/travel_platform"
$env:TRAVEL_DB_DRIVER = "org.postgresql.Driver"
$env:TRAVEL_DB_USER = "postgres"
$env:SBT_OPTS = '-Dsbt.boot.directory=E:/typesafe/template/backend/.sbt-home/boot -Dsbt.global.base=E:/typesafe/template/backend/.sbt-home -Dsbt.ivy.home=E:/typesafe/template/backend/.ivy2 -Divy.home=E:/typesafe/template/backend/.ivy2 -Dcoursier.cache=E:/typesafe/template/backend/.coursier -Dsbt.coursier.home=E:/typesafe/template/backend/.coursier -Dsbt.repository.config=E:/typesafe/template/backend/project/repositories -Dsbt.override.build.repos=true -Dsbt.supershell=false -Dsbt.ci=true -Dsbt.server.autostart=false'
$env:COURSIER_CACHE = 'E:/typesafe/template/backend/.coursier'
$env:COURSIER_ARCHIVE_CACHE = 'E:/typesafe/template/backend/.coursier/archive'
$env:COURSIER_JVM_CACHE = 'E:/typesafe/template/backend/.coursier/jvm'

Add-Content -Path $backendScriptLog -Value "[backend] start script entered $(Get-Date -Format o) mode=$RepositoryMode port=$BackendPort db=$($env:TRAVEL_DB_URL)"
Set-Location 'E:\typesafe\template\backend'
$classpathExport = 'E:\typesafe\template\backend\modules\api-gateway\target\streams\runtime\fullClasspathAsJars\_global\streams\export'
if (-not (Test-Path $classpathExport)) {
  Add-Content -Path $backendScriptLog -Value "[backend] classpath export missing at $classpathExport"
  Write-Host "[travel-platform] Backend runtime classpath was not found. Compile the backend once before using the shortcut."
  exit 1
}

$runtimeClasspathEntries =
  (Get-Content -Path $classpathExport -Raw).Trim().Split(';', [System.StringSplitOptions]::RemoveEmptyEntries) |
  ForEach-Object {
    if ($_ -match '^(.*\\modules\\[^\\]+\\target\\scala-3\.3\.3)\\[^\\]+_3-[^\\]+\.jar$') {
      $classesDir = Join-Path $matches[1] 'classes'
      if (Test-Path $classesDir) {
        $classesDir
      } else {
        $_
      }
    } else {
      $_
    }
  }

$runtimeClasspath = $runtimeClasspathEntries -join ';'

Remove-Item $backendRunLog, $backendErrorLog -Force -ErrorAction SilentlyContinue
$javaProcess = Start-Process -FilePath "$env:JAVA_HOME\bin\java.exe" -ArgumentList @('-classpath', $runtimeClasspath, 'com.typesafe.travel.api.Main') -RedirectStandardOutput $backendRunLog -RedirectStandardError $backendErrorLog -WindowStyle Hidden -PassThru
Add-Content -Path $backendScriptLog -Value "[backend] java launched $(Get-Date -Format o) pid=$($javaProcess.Id)"
exit 0

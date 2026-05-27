param(
  [ValidateSet('database', 'in-memory')]
  [string]$RepositoryMode = 'database',
  [int]$BackendPort = 19095
)

$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$backendRoot = Join-Path $repoRoot 'backend'
$logDir = Join-Path $repoRoot '.launcher-logs'
New-Item -ItemType Directory -Path $logDir -Force | Out-Null
$backendScriptLog = Join-Path $logDir 'backend-script.log'
$backendRunLog = Join-Path $logDir 'backend-run.log'
$backendErrorLog = Join-Path $logDir 'backend-error.log'

$bundledJavaHome = Join-Path $backendRoot '.jdks\temurin-21-unpacked\jdk-21.0.10+7'
if ($env:JAVA_HOME -and (Test-Path $env:JAVA_HOME)) {
  $javaHome = $env:JAVA_HOME
} elseif (Test-Path $bundledJavaHome) {
  $javaHome = $bundledJavaHome
} else {
  throw 'JAVA_HOME is required. Set JAVA_HOME or place a bundled JDK under backend/.jdks.'
}

$env:JAVA_HOME = $javaHome
$env:TRAVEL_REPOSITORY_MODE = $RepositoryMode
$env:TRAVEL_BACKEND_PORT = "$BackendPort"
$launcherDatabasePath = (Join-Path $backendRoot 'data\travel-platform-runtime').Replace('\', '/')
$env:TRAVEL_DB_URL = "jdbc:postgresql://127.0.0.1:5432/travel_platform"
$env:TRAVEL_DB_DRIVER = "org.postgresql.Driver"
$env:TRAVEL_DB_USER = "postgres"
$backendSbtHome = Join-Path $backendRoot '.sbt-home'
$backendIvyHome = Join-Path $backendRoot '.ivy2'
$backendCoursierHome = Join-Path $backendRoot '.coursier'
$sbtRepositoryConfig = (Join-Path $backendRoot 'project\repositories').Replace('\', '/')
$sbtBootDirectory = (Join-Path $backendSbtHome 'boot').Replace('\', '/')
$sbtGlobalBase = $backendSbtHome.Replace('\', '/')
$sbtIvyHome = $backendIvyHome.Replace('\', '/')
$sbtCoursierHome = $backendCoursierHome.Replace('\', '/')
$env:SBT_OPTS = "-Dsbt.boot.directory=$sbtBootDirectory -Dsbt.global.base=$sbtGlobalBase -Dsbt.ivy.home=$sbtIvyHome -Divy.home=$sbtIvyHome -Dcoursier.cache=$sbtCoursierHome -Dsbt.coursier.home=$sbtCoursierHome -Dsbt.repository.config=$sbtRepositoryConfig -Dsbt.override.build.repos=true -Dsbt.supershell=false -Dsbt.ci=true -Dsbt.server.autostart=false"
$env:COURSIER_CACHE = $backendCoursierHome
$env:COURSIER_ARCHIVE_CACHE = Join-Path $backendCoursierHome 'archive'
$env:COURSIER_JVM_CACHE = Join-Path $backendCoursierHome 'jvm'

Add-Content -Path $backendScriptLog -Value "[backend] start script entered $(Get-Date -Format o) mode=$RepositoryMode port=$BackendPort db=$($env:TRAVEL_DB_URL)"
Set-Location $backendRoot
$classpathExport = Join-Path $backendRoot 'modules\api-gateway\target\streams\runtime\fullClasspathAsJars\_global\streams\export'
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

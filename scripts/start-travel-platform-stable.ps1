param(
  [int]$BackendPort = 19095,
  [int]$FrontendPort = 5173
)

$templateRoot = Split-Path -Parent $PSScriptRoot
$workspaceRoot = Split-Path -Parent $templateRoot
$backendRoot = Join-Path $templateRoot 'backend'
$frontendRoot = Join-Path $templateRoot 'frontend'
$distDir = Join-Path $frontendRoot 'dist'
$logDir = Join-Path $templateRoot '.launcher-logs'
$launcherLog = Join-Path $logDir 'launcher-stable.log'
$backendStdout = Join-Path $logDir 'backend.stdout.log'
$backendStderr = Join-Path $logDir 'backend.stderr.log'
$backendArgFile = Join-Path $logDir 'backend-java.args'
$frontendStdout = Join-Path $logDir 'frontend.stdout.log'
$frontendStderr = Join-Path $logDir 'frontend.stderr.log'

function Write-LauncherLog {
  param([string]$Message)
  Add-Content -Path $launcherLog -Value "[travel-platform] $(Get-Date -Format o) $Message"
}

function Test-HttpReady {
  param([string]$Url)

  try {
    $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 2
    return $response.StatusCode -ge 200
  } catch {
    return $false
  }
}

function Get-BackendHealthPayload {
  param([string]$BackendHealthUrl)

  try {
    $response = Invoke-WebRequest -Uri $BackendHealthUrl -UseBasicParsing -TimeoutSec 2
    if ($response.StatusCode -lt 200 -or $response.StatusCode -ge 300) {
      return $null
    }

    return ($response.Content | Out-String).Trim()
  } catch {
    return $null
  }
}

function Test-BackendHealthy {
  param([string]$BackendHealthUrl)

  $payload = Get-BackendHealthPayload -BackendHealthUrl $BackendHealthUrl
  return $null -ne $payload -and $payload.Contains('"status":"ok"')
}

function Test-StableBackendHealthy {
  param([string]$BackendHealthUrl)

  if (-not (Test-BackendHealthy -BackendHealthUrl $BackendHealthUrl)) {
    return $false
  }

  Start-Sleep -Milliseconds 750
  return Test-BackendHealthy -BackendHealthUrl $BackendHealthUrl
}

function Get-PgAdminSavedPassword {
  $pgAdminPython = Join-Path $workspaceRoot 'postgresql\pgAdmin 4\python\python.exe'
  if (-not (Test-Path $pgAdminPython)) {
    return $null
  }

  $script = @'
import sqlite3, importlib.util, keyring
crypto_path = r'E:\typesafe\postgresql\pgAdmin 4\web\pgadmin\utils\crypto.py'
spec = importlib.util.spec_from_file_location('pgadmin_crypto', crypto_path)
crypto = importlib.util.module_from_spec(spec)
spec.loader.exec_module(crypto)
conn = sqlite3.connect(r'C:\Users\X1\AppData\Roaming\pgAdmin\pgadmin4.db')
cur = conn.cursor()
master_key = keyring.get_password('pgAdmin4', 'pgadmin4-master-password')
cur.execute("SELECT password FROM server WHERE host = 'localhost' AND port = 5432 ORDER BY id LIMIT 1")
row = cur.fetchone()
if not master_key or not row or not row[0]:
    print('')
else:
    server_password = bytes.fromhex(row[0])
    print(crypto.decrypt(server_password, master_key).decode())
'@

  try {
    $savedPassword = ($script | & $pgAdminPython - 2>$null | Out-String).Trim()
    if ($savedPassword.Length -gt 0) {
      return $savedPassword
    }
  } catch {
  }

  return $null
}

function Start-BackgroundCommand {
  param(
    [string]$FilePath,
    [string]$Arguments,
    [string]$WorkingDirectory
  )

  $processStartInfo = New-Object System.Diagnostics.ProcessStartInfo
  $processStartInfo.FileName = $FilePath
  $processStartInfo.Arguments = $Arguments
  $processStartInfo.WorkingDirectory = $WorkingDirectory
  $processStartInfo.UseShellExecute = $false
  $processStartInfo.CreateNoWindow = $true

  $process = New-Object System.Diagnostics.Process
  $process.StartInfo = $processStartInfo
  $process.Start() | Out-Null
  return $process
}

function Get-BackendRuntimeClasspath {
  $classpathExport = Join-Path $backendRoot 'modules\api-gateway\target\streams\runtime\fullClasspathAsJars\_global\streams\export'
  if (-not (Test-Path $classpathExport)) {
    throw "Backend runtime classpath export was not found at $classpathExport. Compile the backend once before using the shortcut."
  }

  $entries =
    (Get-Content -Path $classpathExport -Raw).Trim().Split(';', [System.StringSplitOptions]::RemoveEmptyEntries) |
    ForEach-Object {
      $entry = $_
      if ($entry -like 'C:\Users\*\AppData\Local\Coursier\Cache\v1\*') {
        $relativeCachePath = $entry.Substring($entry.IndexOf('\Cache\v1\') + '\Cache\v1\'.Length).TrimStart('\')
        $workspaceCacheCandidate = Join-Path $backendRoot (Join-Path '.coursier-cache' $relativeCachePath)
        $workspaceCoursierCandidate = Join-Path $backendRoot (Join-Path '.coursier\cache' $relativeCachePath)

        if (Test-Path $workspaceCacheCandidate) {
          $entry = $workspaceCacheCandidate
        } elseif (Test-Path $workspaceCoursierCandidate) {
          $entry = $workspaceCoursierCandidate
        }
      }

      if ($entry -match '^(.*\\modules\\[^\\]+\\target\\scala-3\.3\.3)\\[^\\]+_3-[^\\]+\.jar$') {
        $classesDir = Join-Path $matches[1] 'classes'
        if (Test-Path $classesDir) {
          $classesDir
        } else {
          $entry
        }
      } else {
        $entry
      }
    }

  return ($entries -join ';')
}

function Write-RuntimeConfig {
  param([bool]$BackendHealthy)

  $initialHealthJson =
    if ($BackendHealthy) {
      "{ status: 'ok', service: 'travel-platform-backend', backendPort: $BackendPort }"
    } else {
      "null"
    }

  [System.IO.File]::WriteAllText(
    $runtimeConfigPath,
    @"
window.__TRAVEL_BACKEND_ORIGIN__ = '$backendOrigin';
window.__TRAVEL_INITIAL_BACKEND_HEALTH__ = $initialHealthJson;
"@,
    (New-Object System.Text.UTF8Encoding($false))
  )
}

New-Item -ItemType Directory -Path $logDir -Force | Out-Null
Set-Content -Path $launcherLog -Value "[travel-platform] launcher started $(Get-Date -Format o)" -Encoding UTF8

$backendOrigin = "http://localhost:$BackendPort"
$frontendOrigin = "http://localhost:$FrontendPort"
$bootToken = Get-Date -Format yyyyMMddHHmmssfff
$frontendLaunchUrl = "$frontendOrigin/?boot=$bootToken"

if (-not (Test-Path (Join-Path $distDir 'index.html'))) {
  Write-Host "[travel-platform] Frontend dist was not found at $distDir. Run npm.cmd run build once before using the shortcut."
  Write-LauncherLog "frontend dist missing at $distDir"
  exit 1
}

$runtimeConfigPath = Join-Path $distDir 'runtime-config.js'
Write-RuntimeConfig -BackendHealthy $false
Write-LauncherLog "runtime config written for backend $backendOrigin"

$indexHtmlPath = Join-Path $distDir 'index.html'
if (Test-Path $indexHtmlPath) {
  $indexHtmlContent = Get-Content -Path $indexHtmlPath -Raw
  $indexHtmlContent = $indexHtmlContent -replace '/runtime-config\.js(\?boot=\d+)?', "/runtime-config.js?boot=$bootToken"
  [System.IO.File]::WriteAllText($indexHtmlPath, $indexHtmlContent, (New-Object System.Text.UTF8Encoding($false)))
  Write-LauncherLog "index html updated with runtime-config cache bust token $bootToken"
}

$backendHealthUrl = "$backendOrigin/api/health"

if (-not (Test-StableBackendHealthy $backendHealthUrl)) {
  try {
    $javaHome = Join-Path $backendRoot '.jdks\temurin-21-unpacked\jdk-21.0.10+7'
    $javaExe = Join-Path $javaHome 'bin\java.exe'
    $runtimeClasspath = Get-BackendRuntimeClasspath
    $fallbackDatabasePath = (Join-Path $backendRoot 'data\travel-platform-runtime').Replace('\', '/')
    $savedPostgresPassword = Get-PgAdminSavedPassword
    $databaseUrl =
      if ($env:TRAVEL_DB_URL -and $env:TRAVEL_DB_URL.Trim().Length -gt 0) {
        $env:TRAVEL_DB_URL.Trim()
      } elseif ($savedPostgresPassword) {
        "jdbc:postgresql://127.0.0.1:5432/travel_platform"
      } else {
        "jdbc:h2:file:$fallbackDatabasePath;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE"
      }
    $databaseDriver =
      if ($env:TRAVEL_DB_DRIVER -and $env:TRAVEL_DB_DRIVER.Trim().Length -gt 0) {
        $env:TRAVEL_DB_DRIVER.Trim()
      } elseif ($savedPostgresPassword) {
        "org.postgresql.Driver"
      } else {
        "org.h2.Driver"
      }
    $databaseUser =
      if ($env:TRAVEL_DB_USER -and $env:TRAVEL_DB_USER.Trim().Length -gt 0) {
        $env:TRAVEL_DB_USER.Trim()
      } elseif ($savedPostgresPassword) {
        "postgres"
      } else {
        "sa"
      }
    $databasePassword =
      if ($null -ne $env:TRAVEL_DB_PASSWORD) {
        $env:TRAVEL_DB_PASSWORD
      } elseif ($savedPostgresPassword) {
        $savedPostgresPassword
      } else {
        ""
      }
    Remove-Item $backendStdout, $backendStderr -Force -ErrorAction SilentlyContinue
    [System.IO.File]::WriteAllLines(
      $backendArgFile,
      @(
        '-classpath',
        $runtimeClasspath,
        'com.typesafe.travel.api.Main'
      ),
      (New-Object System.Text.UTF8Encoding($false))
    )
    $backendCommand = "/c set ""JAVA_HOME=$javaHome"" && set ""TRAVEL_REPOSITORY_MODE=database"" && set ""TRAVEL_BACKEND_PORT=$BackendPort"" && set ""TRAVEL_DB_URL=$databaseUrl"" && set ""TRAVEL_DB_DRIVER=$databaseDriver"" && set ""TRAVEL_DB_USER=$databaseUser"" && set ""TRAVEL_DB_PASSWORD=$databasePassword"" && ""$javaExe"" ""@$backendArgFile"" 1>>""$backendStdout"" 2>>""$backendStderr"""
    $backendProcess = Start-BackgroundCommand -FilePath 'cmd.exe' -Arguments $backendCommand -WorkingDirectory $backendRoot
    Write-LauncherLog "backend process started pid=$($backendProcess.Id) db=$databaseUrl driver=$databaseDriver"
  } catch {
    Write-LauncherLog "backend start failed: $($_.Exception.Message)"
    throw
  }
} else {
  Write-LauncherLog "backend already healthy"
}

if (-not (Test-HttpReady $frontendOrigin)) {
  $pythonExe = 'python'
  Remove-Item $frontendStdout, $frontendStderr -Force -ErrorAction SilentlyContinue
  $frontendCommand = "/c cd /d ""$distDir"" && $pythonExe -m http.server $FrontendPort --bind 127.0.0.1 1>>""$frontendStdout"" 2>>""$frontendStderr"""
  $frontendProcess = Start-BackgroundCommand -FilePath 'cmd.exe' -Arguments $frontendCommand -WorkingDirectory $distDir
  Write-LauncherLog "frontend process started pid=$($frontendProcess.Id)"
} else {
  Write-LauncherLog "frontend already healthy"
}

for ($attempt = 0; $attempt -lt 90; $attempt++) {
  $frontendReady = Test-HttpReady $frontendOrigin
  $backendReady = Test-BackendHealthy $backendHealthUrl

  if ($frontendReady -and $backendReady) {
    Write-RuntimeConfig -BackendHealthy $true
    Write-LauncherLog "ready backend=$backendOrigin frontend=$frontendOrigin launch=$frontendLaunchUrl"
    & cmd.exe /c start "" $frontendLaunchUrl | Out-Null
    exit 0
  }

  Start-Sleep -Seconds 2
}

Write-LauncherLog "timed out waiting for backend/frontend readiness"
Write-RuntimeConfig -BackendHealthy (Test-BackendHealthy -BackendHealthUrl $backendHealthUrl)
& cmd.exe /c start "" $frontendLaunchUrl | Out-Null
exit 0

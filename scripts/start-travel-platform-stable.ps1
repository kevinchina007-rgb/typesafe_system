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

function Get-LanHost {
  try {
    $candidateAddress =
      [System.Net.NetworkInformation.NetworkInterface]::GetAllNetworkInterfaces() |
        Where-Object {
          $_.OperationalStatus -eq 'Up' -and
          $_.NetworkInterfaceType -ne [System.Net.NetworkInformation.NetworkInterfaceType]::Loopback
        } |
        ForEach-Object {
          $_.GetIPProperties().UnicastAddresses |
            Where-Object {
              $_.Address.AddressFamily -eq [System.Net.Sockets.AddressFamily]::InterNetwork -and
              -not $_.Address.ToString().StartsWith('127.') -and
              -not $_.Address.ToString().StartsWith('169.254.')
            } |
            ForEach-Object { $_.Address.ToString() }
        } |
        Select-Object -First 1

    if ($candidateAddress) {
      return $candidateAddress
    }
  } catch {
  }

  return 'localhost'
}

function Get-ConfiguredOrigin {
  param(
    [string]$ExplicitOrigin,
    [string]$Scheme,
    [string]$HostName,
    [int]$Port
  )

  if ($ExplicitOrigin -and $ExplicitOrigin.Trim().Length -gt 0) {
    return $ExplicitOrigin.Trim().TrimEnd('/')
  }

  return "${Scheme}://${HostName}:$Port"
}

function Get-BackendRuntimeClasspath {
  $classpathExportCandidates = @(
    (Join-Path $backendRoot 'projects\api-gateway\target\streams\runtime\fullClasspathAsJars\_global\streams\export'),
    (Join-Path $backendRoot 'projects\api-gateway\target\streams\runtime\dependencyClasspathAsJars\_global\streams\export'),
    (Join-Path $backendRoot 'target\streams\runtime\fullClasspathAsJars\_global\streams\export'),
    (Join-Path $backendRoot 'target\streams\runtime\dependencyClasspathAsJars\_global\streams\export')
  )

  $classpathExport = $classpathExportCandidates | Where-Object { Test-Path $_ } | Select-Object -First 1
  if (-not $classpathExport) {
    throw "Backend runtime classpath export was not found in the expected target streams locations. Compile the backend once before using the shortcut."
  }

  Write-LauncherLog "using backend classpath export $classpathExport"

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

      if ($entry -match '^(.*\\projects\\[^\\]+\\target\\scala-3\.3\.3)\\[^\\]+_3-[^\\]+\.jar$') {
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
window.__TRAVEL_PUBLIC_BACKEND_ORIGIN__ = '$backendOrigin';
window.__TRAVEL_INITIAL_BACKEND_HEALTH__ = $initialHealthJson;
"@,
  (New-Object System.Text.UTF8Encoding($false))
)
}

New-Item -ItemType Directory -Path $logDir -Force | Out-Null
Set-Content -Path $launcherLog -Value "[travel-platform] launcher started $(Get-Date -Format o)" -Encoding UTF8

$lanHost = Get-LanHost
$publicHost =
  if ($env:TRAVEL_PUBLIC_HOST -and $env:TRAVEL_PUBLIC_HOST.Trim().Length -gt 0) {
    $env:TRAVEL_PUBLIC_HOST.Trim()
  } else {
    $lanHost
  }
$publicScheme =
  if ($env:TRAVEL_PUBLIC_SCHEME -and $env:TRAVEL_PUBLIC_SCHEME.Trim().Length -gt 0) {
    $env:TRAVEL_PUBLIC_SCHEME.Trim()
  } else {
    'http'
  }
$backendOrigin = Get-ConfiguredOrigin -ExplicitOrigin $env:TRAVEL_PUBLIC_BACKEND_ORIGIN -Scheme $publicScheme -HostName $publicHost -Port $BackendPort
$frontendOrigin = Get-ConfiguredOrigin -ExplicitOrigin $env:TRAVEL_PUBLIC_FRONTEND_ORIGIN -Scheme $publicScheme -HostName $publicHost -Port $FrontendPort
$localFrontendOrigin = "http://localhost:$FrontendPort"
$bootToken = Get-Date -Format yyyyMMddHHmmssfff
$frontendLaunchUrl = "$localFrontendOrigin/?boot=$bootToken"

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
$localBackendHealthUrl = "http://127.0.0.1:$BackendPort/api/health"
$allowedOrigins =
  if ($env:TRAVEL_ALLOWED_ORIGINS -and $env:TRAVEL_ALLOWED_ORIGINS.Trim().Length -gt 0) {
    $env:TRAVEL_ALLOWED_ORIGINS.Trim()
  } else {
    "$frontendOrigin,$localFrontendOrigin,http://localhost:$FrontendPort"
  }

if (-not (Test-StableBackendHealthy $localBackendHealthUrl)) {
  try {
    $savedPostgresPassword = Get-PgAdminSavedPassword
    $databaseUrl =
      if ($env:TRAVEL_DB_URL -and $env:TRAVEL_DB_URL.Trim().Length -gt 0) {
        $env:TRAVEL_DB_URL.Trim()
      } else {
        "jdbc:postgresql://127.0.0.1:5432/travel_platform"
      }
    $databaseDriver =
      if ($env:TRAVEL_DB_DRIVER -and $env:TRAVEL_DB_DRIVER.Trim().Length -gt 0) {
        $env:TRAVEL_DB_DRIVER.Trim()
      } else {
        "org.postgresql.Driver"
      }
    $databaseUser =
      if ($env:TRAVEL_DB_USER -and $env:TRAVEL_DB_USER.Trim().Length -gt 0) {
        $env:TRAVEL_DB_USER.Trim()
      } else {
        "postgres"
      }
    $databasePassword =
      if ($null -ne $env:TRAVEL_DB_PASSWORD) {
        $env:TRAVEL_DB_PASSWORD
      } elseif ($savedPostgresPassword) {
        $savedPostgresPassword
      } else {
        "hzhishengheng"
      }
    Remove-Item $backendStdout, $backendStderr -Force -ErrorAction SilentlyContinue
    $backendCommand = "/c cd /d ""$backendRoot"" && set ""TRAVEL_REPOSITORY_MODE=database"" && set ""TRAVEL_BACKEND_PORT=$BackendPort"" && set ""TRAVEL_DB_URL=$databaseUrl"" && set ""TRAVEL_DB_DRIVER=$databaseDriver"" && set ""TRAVEL_DB_USER=$databaseUser"" && set ""TRAVEL_DB_PASSWORD=$databasePassword"" && set ""TRAVEL_ALLOWED_ORIGINS=$allowedOrigins"" && set ""TRAVEL_AVATAR_UPLOAD_ROOT=$backendRoot\uploads\avatars"" && set ""TRAVEL_CONTENT_UPLOAD_ROOT=$backendRoot\uploads\content"" && set ""TRAVEL_FRONTEND_DIST_ROOT=$distDir"" && sbt --batch ""api-gateway / runMain com.typesafe.travel.api.Main"" 1>>""$backendStdout"" 2>>""$backendStderr"""
    $backendProcess = Start-BackgroundCommand -FilePath 'cmd.exe' -Arguments $backendCommand -WorkingDirectory $backendRoot
    Write-LauncherLog "backend process started pid=$($backendProcess.Id) via=sbt db=$databaseUrl driver=$databaseDriver"
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
  $frontendCommand = "/c cd /d ""$distDir"" && $pythonExe -m http.server $FrontendPort --bind 0.0.0.0 1>>""$frontendStdout"" 2>>""$frontendStderr"""
  $frontendProcess = Start-BackgroundCommand -FilePath 'cmd.exe' -Arguments $frontendCommand -WorkingDirectory $distDir
  Write-LauncherLog "frontend process started pid=$($frontendProcess.Id)"
} else {
  Write-LauncherLog "frontend already healthy"
}

for ($attempt = 0; $attempt -lt 90; $attempt++) {
  $frontendReady = Test-HttpReady $localFrontendOrigin
  $backendReady = Test-BackendHealthy $localBackendHealthUrl

  if ($frontendReady -and $backendReady) {
    Write-RuntimeConfig -BackendHealthy $true
    Write-LauncherLog "ready backend=$backendOrigin frontend=$frontendOrigin localLaunch=$frontendLaunchUrl localBackendHealth=$localBackendHealthUrl"
    & cmd.exe /c start "" $frontendLaunchUrl | Out-Null
    exit 0
  }

  Start-Sleep -Seconds 2
}

Write-LauncherLog "timed out waiting for backend/frontend readiness"
Write-RuntimeConfig -BackendHealthy (Test-BackendHealthy -BackendHealthUrl $localBackendHealthUrl)
& cmd.exe /c start "" $frontendLaunchUrl | Out-Null
exit 0

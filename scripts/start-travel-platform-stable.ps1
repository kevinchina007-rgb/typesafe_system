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

function Get-CommandPath {
  param(
    [string[]]$Candidates,
    [string]$DisplayName
  )

  foreach ($candidate in $Candidates) {
    if (-not $candidate) {
      continue
    }

    $resolved = Get-Command $candidate -ErrorAction SilentlyContinue
    if ($resolved) {
      return $resolved.Source
    }

    if (Test-Path $candidate) {
      return $candidate
    }
  }

  throw "Required command '$DisplayName' was not found. Checked: $($Candidates -join ', ')"
}

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

  $cryptoPath = (Join-Path $workspaceRoot 'postgresql\pgAdmin 4\web\pgadmin\utils\crypto.py').Replace('\', '/')
  $pgAdminDbPath = (Join-Path $env:APPDATA 'pgAdmin\pgadmin4.db').Replace('\', '/')
  $script = @"
import sqlite3, importlib.util, keyring
crypto_path = r'$cryptoPath'
spec = importlib.util.spec_from_file_location('pgadmin_crypto', crypto_path)
crypto = importlib.util.module_from_spec(spec)
spec.loader.exec_module(crypto)
conn = sqlite3.connect(r'$pgAdminDbPath')
cur = conn.cursor()
master_key = keyring.get_password('pgAdmin4', 'pgadmin4-master-password')
cur.execute("SELECT password FROM server WHERE host = 'localhost' AND port = 5432 ORDER BY id LIMIT 1")
row = cur.fetchone()
if not master_key or not row or not row[0]:
    print('')
else:
    server_password = bytes.fromhex(row[0])
    print(crypto.decrypt(server_password, master_key).decode())
"@

  try {
    $savedPassword = ($script | & $pgAdminPython - 2>$null | Out-String).Trim()
    if ($savedPassword.Length -gt 0) {
      return $savedPassword
    }
  } catch {
  }

  return $null
}

function Get-PostgresBinRoot {
  $candidateRoots = @(
    (Join-Path $templateRoot 'postgresql\bin'),
    (Join-Path $workspaceRoot 'tools\postgresql-18\bin'),
    (Join-Path $workspaceRoot 'postgresql\bin'),
    'C:\typesafe\tools\postgresql-18\bin',
    'C:\Program Files\PostgreSQL\18\bin',
    'C:\Program Files\PostgreSQL\17\bin',
    'C:\Program Files\PostgreSQL\16\bin'
  )

  foreach ($candidateRoot in $candidateRoots) {
    if (
      (Test-Path (Join-Path $candidateRoot 'postgres.exe'))
    ) {
      return $candidateRoot
    }
  }

  $pgCtlCommand = Get-Command 'pg_ctl.exe' -ErrorAction SilentlyContinue
  if ($pgCtlCommand) {
    $commandRoot = Split-Path -Parent $pgCtlCommand.Source
    if (Test-Path (Join-Path $commandRoot 'postgres.exe')) {
      return $commandRoot
    }
  }

  return $null
}

function Test-PostgresPortReady {
  try {
    $connection = New-Object System.Net.Sockets.TcpClient
    $connectResult = $connection.BeginConnect('127.0.0.1', 5432, $null, $null)
    if (-not $connectResult.AsyncWaitHandle.WaitOne(1000, $false)) {
      $connection.Close()
      return $false
    }
    $connection.EndConnect($connectResult)
    $connection.Close()
    return $true
  } catch {
    return $false
  }
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

function Get-BackendDatabaseSettings {
  $localPostgresDataRoot = Join-Path $backendRoot '.postgres-dev\data'
  $localPostgresBinRoot = Get-PostgresBinRoot
  $localPostgresExe =
    if ($localPostgresBinRoot) {
      Join-Path $localPostgresBinRoot 'postgres.exe'
    } else {
      $null
    }

  if (
    -not $env:TRAVEL_DB_URL -and
    -not $env:TRAVEL_DB_DRIVER -and
    -not $env:TRAVEL_DB_USER -and
    $null -eq $env:TRAVEL_DB_PASSWORD -and
    (Test-Path (Join-Path $localPostgresDataRoot 'PG_VERSION')) -and
    $localPostgresExe -and
    (Test-Path $localPostgresExe)
  ) {
    Write-LauncherLog "using postgres tools at $localPostgresBinRoot"
    $hasLocalPostgresData = Test-Path (Join-Path $localPostgresDataRoot 'PG_VERSION')
    if (-not (Test-PostgresPortReady)) {
      Write-LauncherLog "starting local postgres with data root $localPostgresDataRoot"
      $postgresProcess = Start-BackgroundCommand -FilePath $localPostgresExe -Arguments "-D ""$localPostgresDataRoot"" -p 5432" -WorkingDirectory $backendRoot
      Write-LauncherLog "postgres process started pid=$($postgresProcess.Id)"
      for ($attempt = 0; $attempt -lt 30; $attempt++) {
        if (Test-PostgresPortReady) {
          break
        }
        Start-Sleep -Seconds 1
      }
    }

    if (Test-PostgresPortReady) {
      Write-LauncherLog "local postgres ready on 127.0.0.1:5432"
      return @{
        Url = 'jdbc:postgresql://127.0.0.1:5432/travel_platform'
        Driver = 'org.postgresql.Driver'
        User = 'postgres'
        Password = 'root'
      }
    }

    if ($hasLocalPostgresData) {
      throw "Local PostgreSQL data exists at $localPostgresDataRoot, but PostgreSQL did not become ready on 127.0.0.1:5432. Check .launcher-logs\postgres.local.log or start PostgreSQL manually."
    }
  }

  if (
    -not $env:TRAVEL_DB_URL -and
    -not $env:TRAVEL_DB_DRIVER -and
    -not $env:TRAVEL_DB_USER -and
    $null -eq $env:TRAVEL_DB_PASSWORD -and
    (Test-Path (Join-Path $localPostgresDataRoot 'PG_VERSION')) -and
    -not (Test-PostgresPortReady) -and
    -not $localPostgresBinRoot
  ) {
    throw "Local PostgreSQL data exists at $localPostgresDataRoot, but postgres.exe was not found. Put PostgreSQL bin under <repo>\postgresql\bin, <repo-parent>\tools\postgresql-18\bin, C:\typesafe\tools\postgresql-18\bin, or add it to PATH."
  }

  return @{
    Url =
      if ($env:TRAVEL_DB_URL -and $env:TRAVEL_DB_URL.Trim().Length -gt 0) {
        $env:TRAVEL_DB_URL.Trim()
      } else {
        'jdbc:postgresql://127.0.0.1:5432/travel_platform'
      }
    Driver =
      if ($env:TRAVEL_DB_DRIVER -and $env:TRAVEL_DB_DRIVER.Trim().Length -gt 0) {
        $env:TRAVEL_DB_DRIVER.Trim()
      } else {
        'org.postgresql.Driver'
      }
    User =
      if ($env:TRAVEL_DB_USER -and $env:TRAVEL_DB_USER.Trim().Length -gt 0) {
        $env:TRAVEL_DB_USER.Trim()
      } else {
        'postgres'
      }
    Password =
      if ($null -ne $env:TRAVEL_DB_PASSWORD) {
        $env:TRAVEL_DB_PASSWORD
      } else {
        ''
      }
  }
}

New-Item -ItemType Directory -Path $logDir -Force | Out-Null
Set-Content -Path $launcherLog -Value "[travel-platform] launcher started $(Get-Date -Format o)" -Encoding UTF8

try {

$npmCommand = Get-CommandPath -Candidates @(
  'npm.cmd',
  'C:\Program Files\nodejs\npm.cmd'
) -DisplayName 'npm.cmd'
$sbtCommand = Get-CommandPath -Candidates @(
  (Join-Path $workspaceRoot 'bin\sbt.cmd'),
  'sbt',
  'sbt.bat'
) -DisplayName 'sbt'
$pythonCommand = Get-CommandPath -Candidates @(
  'python',
  'py',
  'py.exe',
  'C:\Windows\py.exe'
) -DisplayName 'python'

Write-LauncherLog "resolved commands npm=$npmCommand sbt=$sbtCommand python=$pythonCommand"

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
  Write-LauncherLog "frontend dist missing at $distDir, preparing build"

  if (-not (Test-Path (Join-Path $frontendRoot 'node_modules'))) {
    Write-LauncherLog "frontend node_modules missing, running npm install"
    Push-Location $frontendRoot
    try {
      & $npmCommand install
      if ($LASTEXITCODE -ne 0) {
        throw "npm install failed with exit code $LASTEXITCODE"
      }
    } finally {
      Pop-Location
    }
  }

  Push-Location $frontendRoot
  try {
    & $npmCommand run build
    if ($LASTEXITCODE -ne 0) {
      throw "npm run build failed with exit code $LASTEXITCODE"
    }
  } finally {
    Pop-Location
  }

  Write-LauncherLog "frontend dist build completed"
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
Write-LauncherLog "static assets prepared"

$backendHealthUrl = "$backendOrigin/api/health"
$localBackendHealthUrl = "http://127.0.0.1:$BackendPort/api/health"
$allowedOrigins =
  if ($env:TRAVEL_ALLOWED_ORIGINS -and $env:TRAVEL_ALLOWED_ORIGINS.Trim().Length -gt 0) {
    $env:TRAVEL_ALLOWED_ORIGINS.Trim()
  } else {
    "$frontendOrigin,$localFrontendOrigin,http://localhost:$FrontendPort,http://127.0.0.1:$FrontendPort"
  }
Write-LauncherLog "allowed origins resolved"

if (-not (Test-StableBackendHealthy $localBackendHealthUrl)) {
  Write-LauncherLog "backend not healthy before launch"
  try {
    $savedPostgresPassword = Get-PgAdminSavedPassword
    Write-LauncherLog "pgAdmin password lookup completed"
    $databaseSettings = Get-BackendDatabaseSettings
    Write-LauncherLog "database settings resolved"
    $databaseUrl = $databaseSettings.Url
    $databaseDriver = $databaseSettings.Driver
    $databaseUser = $databaseSettings.User
    $databasePassword =
      if ($databaseDriver -eq 'org.postgresql.Driver' -and -not $env:TRAVEL_DB_PASSWORD -and $savedPostgresPassword) {
        $savedPostgresPassword
      } else {
        $databaseSettings.Password
      }
    Remove-Item $backendStdout, $backendStderr -Force -ErrorAction SilentlyContinue
    $backendCommand = "/c cd /d ""$backendRoot"" && set ""TRAVEL_REPOSITORY_MODE=database"" && set ""TRAVEL_BACKEND_PORT=$BackendPort"" && set ""TRAVEL_DB_URL=$databaseUrl"" && set ""TRAVEL_DB_DRIVER=$databaseDriver"" && set ""TRAVEL_DB_USER=$databaseUser"" && set ""TRAVEL_DB_PASSWORD=$databasePassword"" && set ""TRAVEL_ALLOWED_ORIGINS=$allowedOrigins"" && set ""TRAVEL_AVATAR_UPLOAD_ROOT=$backendRoot\uploads\avatars"" && set ""TRAVEL_CONTENT_UPLOAD_ROOT=$backendRoot\uploads\content"" && set ""TRAVEL_FRONTEND_DIST_ROOT=$distDir"" && call ""$sbtCommand"" --batch ""api-gateway / runMain com.typesafe.travel.api.Main"" 1>>""$backendStdout"" 2>>""$backendStderr"""
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
  Write-LauncherLog "frontend not healthy before launch"
  Remove-Item $frontendStdout, $frontendStderr -Force -ErrorAction SilentlyContinue
  $frontendCommand = "/c cd /d ""$distDir"" && call ""$pythonCommand"" -m http.server $FrontendPort --bind 0.0.0.0 1>>""$frontendStdout"" 2>>""$frontendStderr"""
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

} catch {
  Write-LauncherLog "fatal launcher error: $($_.Exception.Message)"
  throw
}

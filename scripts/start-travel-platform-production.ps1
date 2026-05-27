param(
  [int]$BackendPort = 19095
)

$templateRoot = Split-Path -Parent $PSScriptRoot
$backendRoot = Join-Path $templateRoot 'backend'
$frontendRoot = Join-Path $templateRoot 'frontend'
$distDir = Join-Path $frontendRoot 'dist'
$logDir = Join-Path $templateRoot '.launcher-logs'
$launcherLog = Join-Path $logDir 'launcher-production.log'
$backendStdout = Join-Path $logDir 'backend.production.stdout.log'
$backendStderr = Join-Path $logDir 'backend.production.stderr.log'

function Write-LauncherLog {
  param([string]$Message)
  Add-Content -Path $launcherLog -Value "[travel-platform-production] $(Get-Date -Format o) $Message"
}

function Test-BackendHealthy {
  param([string]$BackendHealthUrl)

  try {
    $response = Invoke-WebRequest -Uri $BackendHealthUrl -UseBasicParsing -TimeoutSec 3
    return $response.StatusCode -eq 200 -and ($response.Content | Out-String).Contains('"status":"ok"')
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

function Write-RuntimeConfig {
  param([string]$RuntimeBackendOrigin)

  $runtimeConfigPath = Join-Path $distDir 'runtime-config.js'
  [System.IO.File]::WriteAllText(
    $runtimeConfigPath,
    @"
window.__TRAVEL_BACKEND_ORIGIN__ = '$RuntimeBackendOrigin';
window.__TRAVEL_PUBLIC_BACKEND_ORIGIN__ = '$RuntimeBackendOrigin';
"@,
    (New-Object System.Text.UTF8Encoding($false))
  )
}

New-Item -ItemType Directory -Path $logDir -Force | Out-Null
Set-Content -Path $launcherLog -Value "[travel-platform-production] launcher started $(Get-Date -Format o)" -Encoding UTF8

$publicHost =
  if ($env:TRAVEL_PUBLIC_HOST -and $env:TRAVEL_PUBLIC_HOST.Trim().Length -gt 0) {
    $env:TRAVEL_PUBLIC_HOST.Trim()
  } else {
    throw "TRAVEL_PUBLIC_HOST is required for production startup."
  }

$publicScheme =
  if ($env:TRAVEL_PUBLIC_SCHEME -and $env:TRAVEL_PUBLIC_SCHEME.Trim().Length -gt 0) {
    $env:TRAVEL_PUBLIC_SCHEME.Trim()
  } else {
    'https'
  }

$publicFrontendOrigin = Get-ConfiguredOrigin -ExplicitOrigin $env:TRAVEL_PUBLIC_FRONTEND_ORIGIN -Scheme $publicScheme -HostName $publicHost -Port 443
$publicBackendOrigin = Get-ConfiguredOrigin -ExplicitOrigin $env:TRAVEL_PUBLIC_BACKEND_ORIGIN -Scheme $publicScheme -HostName $publicHost -Port 443
$backendHealthUrl = "http://127.0.0.1:$BackendPort/api/health"
$allowedOrigins =
  if ($env:TRAVEL_ALLOWED_ORIGINS -and $env:TRAVEL_ALLOWED_ORIGINS.Trim().Length -gt 0) {
    $env:TRAVEL_ALLOWED_ORIGINS.Trim()
  } else {
    $publicFrontendOrigin
  }

Write-Host "[travel-platform-production] building frontend dist..."
Write-LauncherLog "building frontend dist"
Push-Location $frontendRoot
try {
  & npm.cmd run build
  if ($LASTEXITCODE -ne 0) {
    throw "frontend build failed"
  }
} finally {
  Pop-Location
}

Write-RuntimeConfig -RuntimeBackendOrigin 'same-origin'
Write-LauncherLog "runtime config written in same-origin mode"

if (-not (Test-BackendHealthy -BackendHealthUrl $backendHealthUrl)) {
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
    if ($env:TRAVEL_DB_PASSWORD -and $env:TRAVEL_DB_PASSWORD.Length -gt 0) {
      $env:TRAVEL_DB_PASSWORD
    } else {
      throw "TRAVEL_DB_PASSWORD is required for production startup."
    }

  Remove-Item $backendStdout, $backendStderr -Force -ErrorAction SilentlyContinue

  $backendCommand = "/c cd /d ""$backendRoot"" && set ""TRAVEL_REPOSITORY_MODE=database"" && set ""TRAVEL_BACKEND_PORT=$BackendPort"" && set ""TRAVEL_DB_URL=$databaseUrl"" && set ""TRAVEL_DB_DRIVER=$databaseDriver"" && set ""TRAVEL_DB_USER=$databaseUser"" && set ""TRAVEL_DB_PASSWORD=$databasePassword"" && set ""TRAVEL_ALLOWED_ORIGINS=$allowedOrigins"" && set ""TRAVEL_ALLOW_PRIVATE_NETWORK_ORIGINS=false"" && set ""TRAVEL_PUBLIC_FRONTEND_ORIGIN=$publicFrontendOrigin"" && set ""TRAVEL_PUBLIC_BACKEND_ORIGIN=$publicBackendOrigin"" && set ""TRAVEL_SESSION_COOKIE_SECURE=true"" && set ""TRAVEL_SESSION_COOKIE_SAMESITE=lax"" && set ""TRAVEL_AVATAR_UPLOAD_ROOT=$backendRoot\uploads\avatars"" && set ""TRAVEL_CONTENT_UPLOAD_ROOT=$backendRoot\uploads\content"" && set ""TRAVEL_FRONTEND_DIST_ROOT=$distDir"" && sbt --batch ""api-gateway / runMain com.typesafe.travel.api.Main"" 1>>""$backendStdout"" 2>>""$backendStderr"""
  $backendProcess = Start-BackgroundCommand -FilePath 'cmd.exe' -Arguments $backendCommand -WorkingDirectory $backendRoot
  Write-LauncherLog "backend process started pid=$($backendProcess.Id)"
} else {
  Write-LauncherLog "backend already healthy"
}

for ($attempt = 0; $attempt -lt 90; $attempt++) {
  if (Test-BackendHealthy -BackendHealthUrl $backendHealthUrl) {
    Write-Host "[travel-platform-production] backend ready at http://127.0.0.1:$BackendPort"
    Write-Host "[travel-platform-production] public frontend origin: $publicFrontendOrigin"
    Write-Host "[travel-platform-production] public backend origin: $publicBackendOrigin"
    Write-Host "[travel-platform-production] deploy Caddy with .\deploy\Caddyfile"
    Write-LauncherLog "ready localBackend=http://127.0.0.1:$BackendPort publicFrontend=$publicFrontendOrigin publicBackend=$publicBackendOrigin"
    exit 0
  }

  Start-Sleep -Seconds 2
}

Write-LauncherLog "timed out waiting for backend readiness"
throw "Backend did not become healthy within the expected time window."

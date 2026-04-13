param(
  [int]$BackendPort = 19095,
  [int]$FrontendPort = 5174
)

$templateRoot = Split-Path -Parent $PSScriptRoot
$stableScript = Join-Path $templateRoot 'scripts\start-travel-platform-stable.ps1'
$logDir = Join-Path $templateRoot '.launcher-logs'
$launcherLog = Join-Path $logDir 'launcher-stable.log'
$frontendLaunchUrl = "http://localhost:$FrontendPort/"
$backendHealthUrl = "http://127.0.0.1:$BackendPort/api/health"

New-Item -ItemType Directory -Path $logDir -Force | Out-Null

function Write-CleanLauncherLog {
  param([string]$Message)
  Add-Content -Path $launcherLog -Value "[travel-platform-clean] $(Get-Date -Format o) $Message"
}

function Test-UrlReady {
  param([string]$Url)
  try {
    $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 2
    return $response.StatusCode -ge 200
  } catch {
    return $false
  }
}

$createdNew = $false
$launcherMutex = New-Object System.Threading.Mutex($false, 'Global\TypesafeTravelPlatformLauncher', [ref]$createdNew)
$hasMutex = $false

try {
  $hasMutex = $launcherMutex.WaitOne(0)
  if (-not $hasMutex) {
    Write-CleanLauncherLog "another launcher is already running; waiting for readiness instead of starting a second copy"
    for ($attempt = 0; $attempt -lt 90; $attempt++) {
      if ((Test-UrlReady "http://127.0.0.1:$FrontendPort") -and (Test-UrlReady $backendHealthUrl)) {
        Write-CleanLauncherLog "existing launcher became ready; opening $frontendLaunchUrl"
        & cmd.exe /c start "" $frontendLaunchUrl | Out-Null
        exit 0
      }
      Start-Sleep -Seconds 2
    }

    Write-CleanLauncherLog "existing launcher did not become ready before timeout"
    exit 1
  }

  foreach ($port in @($BackendPort, $FrontendPort, 5173)) {
    Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue |
      Select-Object -ExpandProperty OwningProcess -Unique |
      Where-Object { $_ -and $_ -ne $PID } |
      ForEach-Object {
        Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue
      }
  }

  & powershell.exe -NoProfile -ExecutionPolicy Bypass -File $stableScript -BackendPort $BackendPort -FrontendPort $FrontendPort
  exit $LASTEXITCODE
} finally {
  if ($hasMutex) {
    $launcherMutex.ReleaseMutex() | Out-Null
  }
  $launcherMutex.Dispose()
}

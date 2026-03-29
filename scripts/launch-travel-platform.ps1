param(
  [int]$BackendPort = 19095,
  [int]$FrontendPort = 5173
)

$templateRoot = Split-Path -Parent $PSScriptRoot
$logDir = Join-Path $templateRoot '.launcher-logs'
$launcherLog = Join-Path $logDir 'frontend-launcher.log'

function Test-HttpReady {
  param(
    [Parameter(Mandatory = $true)]
    [string]$Url
  )

  try {
    $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 2
    return $response.StatusCode -ge 200
  } catch {
    return $false
  }
}

New-Item -ItemType Directory -Path $logDir -Force | Out-Null
Set-Content -Path $launcherLog -Value "[travel-platform] launcher started $(Get-Date -Format o)" -Encoding UTF8

$backendOrigin = "http://localhost:$BackendPort"
$frontendOrigin = "http://localhost:$FrontendPort"

for ($attempt = 0; $attempt -lt 120; $attempt++) {
  $frontendReady = Test-HttpReady $frontendOrigin
  $backendReady = Test-HttpReady "$backendOrigin/api/health"

  if ($frontendReady -and $backendReady) {
    Add-Content -Path $launcherLog -Value "[travel-platform] ready backend=$backendOrigin frontend=$frontendOrigin"
    & cmd.exe /c start "" $frontendOrigin | Out-Null
    exit 0
  }

  Start-Sleep -Seconds 2
}

Add-Content -Path $launcherLog -Value "[travel-platform] timed out waiting for backend/frontend readiness"
& cmd.exe /c start "" $frontendOrigin | Out-Null
exit 0

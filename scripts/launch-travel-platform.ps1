param(
  [int]$BackendPort = 8095
)

$templateRoot = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $templateRoot 'backend'
$frontendDir = Join-Path $templateRoot 'frontend'
$backendScript = Join-Path $backendDir 'scripts\start-backend.ps1'
$frontendScript = Join-Path $frontendDir 'scripts\serve-frontend.ps1'
$jdkHome = Join-Path $backendDir '.jdks\temurin-21-unpacked\jdk-21.0.10+7'
$backendOrigin = "http://localhost:$BackendPort"
$frontendOrigin = 'http://localhost:5173'
$logDir = Join-Path $templateRoot '.launcher-logs'

function Test-UrlReady {
  param(
    [Parameter(Mandatory = $true)]
    [string]$Url
  )

  try {
    $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 3
    return $response.StatusCode -ge 200
  } catch {
    return $false
  }
}

if (-not (Test-Path (Join-Path $jdkHome 'bin\java.exe'))) {
  Write-Host '[travel-platform] JDK 21 was not found at:'
  Write-Host $jdkHome
  exit 1
}

New-Item -ItemType Directory -Path $logDir -Force | Out-Null

if (-not (Test-UrlReady "$backendOrigin/api/health")) {
  Write-Host "[travel-platform] Starting backend with JDK 21 on port $BackendPort in database mode ..."
  Start-Job -Name 'travel-platform-backend' -ScriptBlock {
    param($scriptPath, $workingDirectory, $port, $logPath)
    $env:TRAVEL_BACKEND_PORT = "$port"
    Set-Location $workingDirectory
    & $scriptPath -RepositoryMode database *> $logPath
  } -ArgumentList $backendScript, $backendDir, $BackendPort, (Join-Path $logDir 'backend.log') | Out-Null
} else {
  Write-Host "[travel-platform] Backend is already running on port $BackendPort."
}

if (-not (Test-UrlReady $frontendOrigin)) {
  Write-Host '[travel-platform] Starting frontend server ...'
  Start-Job -Name 'travel-platform-frontend' -ScriptBlock {
    param($scriptPath, $workingDirectory, $origin, $logPath)
    $env:VITE_TRAVEL_BACKEND_ORIGIN = $origin
    Set-Location $workingDirectory
    & $scriptPath -Port 5173 *> $logPath
  } -ArgumentList $frontendScript, $frontendDir, $backendOrigin, (Join-Path $logDir 'frontend.log') | Out-Null
} else {
  Write-Host '[travel-platform] Frontend is already running.'
}

for ($attempt = 0; $attempt -lt 90; $attempt++) {
  $backendReady = Test-UrlReady "$backendOrigin/api/health"
  $frontendReady = Test-UrlReady $frontendOrigin

  if ($backendReady -and $frontendReady) {
    Write-Host '[travel-platform] Backend and frontend are ready. Opening the app ...'
    & cmd.exe /c start "" $frontendOrigin | Out-Null
    while ($true) {
      Start-Sleep -Seconds 3600
    }
  }

  Start-Sleep -Seconds 2
}

Write-Host '[travel-platform] Services are still starting. Opening the frontend URL now ...'
& cmd.exe /c start "" $frontendOrigin | Out-Null
while ($true) {
  Start-Sleep -Seconds 3600
}

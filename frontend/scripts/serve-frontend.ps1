param(
  [int]$Port = 19173,
  [string]$BackendOrigin = 'http://localhost:19095'
)

$frontendRoot = Split-Path -Parent $PSScriptRoot
$distDir = Join-Path $frontendRoot 'dist'
$templateRoot = Split-Path -Parent $frontendRoot
$logDir = Join-Path $templateRoot '.launcher-logs'
New-Item -ItemType Directory -Path $logDir -Force | Out-Null
$frontendScriptLog = Join-Path $logDir 'frontend-script.log'
Add-Content -Path $frontendScriptLog -Value "[frontend] serve script entered $(Get-Date -Format o) port=$Port backend=$BackendOrigin"

if (-not (Test-Path (Join-Path $distDir 'index.html'))) {
  Write-Host "[travel-platform] Frontend dist was not found at $distDir. Run npm.cmd run build once before using the shortcut."
  Add-Content -Path $frontendScriptLog -Value "[frontend] dist missing at $distDir"
  exit 1
}

[System.IO.File]::WriteAllText(
  (Join-Path $distDir 'runtime-config.js'),
  "window.__TRAVEL_BACKEND_ORIGIN__ = '$BackendOrigin';",
  [System.Text.Encoding]::UTF8
)

Add-Content -Path $frontendScriptLog -Value "[frontend] launching python http.server $(Get-Date -Format o) port=$Port"
Set-Location $distDir
& python -m http.server $Port --bind 0.0.0.0
$exitCode = if ($LASTEXITCODE -ne $null) { $LASTEXITCODE } else { 0 }
Add-Content -Path $frontendScriptLog -Value "[frontend] python http.server exited $(Get-Date -Format o) code=$exitCode"
exit $exitCode

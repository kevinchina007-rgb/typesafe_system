param(
  [int]$BackendPort = 19095,
  [int]$FrontendPort = 5174
)

$templateRoot = Split-Path -Parent $PSScriptRoot
$stableScript = Join-Path $templateRoot 'scripts\start-travel-platform-stable.ps1'

foreach ($port in @($BackendPort, $FrontendPort, 5173)) {
  Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue |
    Select-Object -ExpandProperty OwningProcess -Unique |
    Where-Object { $_ -and $_ -ne $PID } |
    ForEach-Object {
      Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue
    }
}

& powershell.exe -NoProfile -ExecutionPolicy Bypass -File $stableScript -BackendPort $BackendPort -FrontendPort $FrontendPort

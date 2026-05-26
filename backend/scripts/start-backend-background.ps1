param(
  [ValidateSet('database', 'in-memory')]
  [string]$RepositoryMode = 'database'
)

$backendRootPath = Split-Path -Parent $PSScriptRoot
$startScriptPath = Join-Path $backendRootPath 'scripts\start-backend.ps1'

Start-Process powershell.exe `
  -WorkingDirectory $backendRootPath `
  -ArgumentList @(
    '-NoProfile',
    '-ExecutionPolicy',
    'Bypass',
    '-NoExit',
    '-File',
    $startScriptPath,
    '-RepositoryMode',
    $RepositoryMode
  )

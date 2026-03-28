param(
  [ValidateSet('database', 'in-memory')]
  [string]$RepositoryMode = 'database'
)

$backendRootPath = 'E:\typesafe\template\backend'
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

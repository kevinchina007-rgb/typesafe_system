param(
  [ValidateSet('database', 'in-memory')]
  [string]$RepositoryMode = 'database'
)

$backendRootPath = 'E:\typesafe\template\backend'
$startScriptPath = Join-Path $backendRootPath 'scripts\start-backend.cmd'

Start-Process cmd.exe `
  -WorkingDirectory $backendRootPath `
  -ArgumentList @(
    '/k',
    "`"$startScriptPath`" $RepositoryMode"
  )

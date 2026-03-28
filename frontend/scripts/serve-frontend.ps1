param(
  [int]$Port = 5173
)

$frontendRoot = Split-Path -Parent $PSScriptRoot
$distDir = Join-Path $frontendRoot 'dist'

if (-not (Test-Path (Join-Path $distDir 'index.html'))) {
  Write-Host "[travel-platform] Frontend dist was not found at $distDir. Run npm.cmd run build once before using the shortcut."
  exit 1
}

$backendOrigin = if ([string]::IsNullOrWhiteSpace($env:VITE_TRAVEL_BACKEND_ORIGIN)) {
  'http://localhost:8080'
} else {
  $env:VITE_TRAVEL_BACKEND_ORIGIN
}

[System.IO.File]::WriteAllText(
  (Join-Path $distDir 'runtime-config.js'),
  "window.__TRAVEL_BACKEND_ORIGIN__ = '$backendOrigin';",
  [System.Text.Encoding]::UTF8
)

[System.Net.ServicePointManager]::Expect100Continue = $false
$listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, $Port)
$listener.Start()

try {
  while ($true) {
    $client = $listener.AcceptTcpClient()
    try {
      $stream = $client.GetStream()
      $reader = [System.IO.StreamReader]::new($stream)
      $requestLine = $reader.ReadLine()

      while ($true) {
        $headerLine = $reader.ReadLine()
        if ([string]::IsNullOrEmpty($headerLine)) {
          break
        }
      }

      if ([string]::IsNullOrWhiteSpace($requestLine)) {
        continue
      }

      $requestPath = ($requestLine -split ' ')[1].TrimStart('/')
      if ([string]::IsNullOrWhiteSpace($requestPath)) {
        $requestPath = 'index.html'
      }

      $resolvedPath = Join-Path $distDir $requestPath
      if ((-not (Test-Path $resolvedPath)) -or (Get-Item $resolvedPath).PSIsContainer) {
        $resolvedPath = Join-Path $distDir 'index.html'
      }

      $fileBytes = [System.IO.File]::ReadAllBytes($resolvedPath)
      $extension = [System.IO.Path]::GetExtension($resolvedPath).ToLowerInvariant()
      $contentType = switch ($extension) {
        '.html' { 'text/html; charset=utf-8' }
        '.js' { 'text/javascript; charset=utf-8' }
        '.css' { 'text/css; charset=utf-8' }
        '.json' { 'application/json; charset=utf-8' }
        '.svg' { 'image/svg+xml' }
        '.png' { 'image/png' }
        '.jpg' { 'image/jpeg' }
        '.jpeg' { 'image/jpeg' }
        '.ico' { 'image/x-icon' }
        default { 'application/octet-stream' }
      }

      $headerText = @(
        'HTTP/1.1 200 OK'
        "Content-Type: $contentType"
        "Content-Length: $($fileBytes.Length)"
        'Connection: close'
        ''
        ''
      ) -join "`r`n"

      $headerBytes = [System.Text.Encoding]::ASCII.GetBytes($headerText)
      $stream.Write($headerBytes, 0, $headerBytes.Length)
      $stream.Write($fileBytes, 0, $fileBytes.Length)
      $stream.Flush()
    } finally {
      $client.Close()
    }
  }
} finally {
  if ($listener.Server -ne $null) {
    $listener.Stop()
  }
}

# Shared helpers for local federated frontend start/stop.
# Dot-sourced by other scripts in this folder.

$ErrorActionPreference = 'Stop'

$Script:RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path

$Script:RemoteModules = @(
  @{ Name = 'home-module'; Port = 5010; Required = $true }
  @{ Name = 'user-module'; Port = 5020; Required = $false }
  @{ Name = 'content-module'; Port = 5030; Required = $false }
  @{ Name = 'billing-module'; Port = 5050; Required = $false }
  @{ Name = 'phishing-module'; Port = 5070; Required = $false }
  @{ Name = 'account-module'; Port = 5090; Required = $false }
  @{ Name = 'miscellaneous-module'; Port = 5140; Required = $false }
)

$Script:HostModule = @{ Name = 'host-module'; Port = 5000 }

function Start-ModuleWindow {
  param(
    [Parameter(Mandatory)][string]$ModuleName,
    [switch]$IsHost
  )

  $dir = Join-Path $Script:RepoRoot $ModuleName
  if (-not (Test-Path $dir)) {
    throw "Module folder not found: $dir"
  }

  if ($IsHost) {
    $cmd = 'npm install; if ($LASTEXITCODE -ne 0) { Write-Host "npm install failed" -ForegroundColor Red; pause; exit 1 }; npm run dev'
  }
  else {
    $cmd = 'npm install; if ($LASTEXITCODE -ne 0) { Write-Host "npm install failed" -ForegroundColor Red; pause; exit 1 }; npm run build; if ($LASTEXITCODE -ne 0) { Write-Host "build failed" -ForegroundColor Red; pause; exit 1 }; npm run serve'
  }

  Write-Host "Starting $ModuleName ..." -ForegroundColor Cyan
  Start-Process powershell -WorkingDirectory $dir -ArgumentList @(
    '-NoExit',
    '-Command',
    $cmd
  ) | Out-Null
}

function Test-RemoteEntryJs {
  param([Parameter(Mandatory)][int]$Port)

  try {
    $r = Invoke-WebRequest "http://localhost:$Port/assets/remoteEntry.js" -UseBasicParsing -TimeoutSec 3
    if ($r.StatusCode -ne 200) { return $false }
    $ct = [string]$r.Headers['Content-Type']
    $body = [string]$r.Content
    if ($body -match '(?i)^\s*<!doctype\s+html|^\s*<html') { return $false }
    if ($ct -match 'html') { return $false }
    return $true
  }
  catch {
    return $false
  }
}

function Wait-RemoteEntryJs {
  param(
    [Parameter(Mandatory)][int]$Port,
    [int]$TimeoutSec = 900
  )

  Write-Host "Waiting for http://localhost:$Port/assets/remoteEntry.js (JS, up to ${TimeoutSec}s) ..." -ForegroundColor Yellow
  $deadline = (Get-Date).AddSeconds($TimeoutSec)
  while ((Get-Date) -lt $deadline) {
    if (Test-RemoteEntryJs -Port $Port) {
      Write-Host "OK :$Port remoteEntry.js is JavaScript" -ForegroundColor Green
      return
    }
    Start-Sleep -Seconds 3
  }
  throw "Timed out waiting for JS remoteEntry.js on port $Port. Check the module window for build/serve errors."
}

function Start-FrontendStack {
  param(
    [Parameter(Mandatory)][string[]]$RemoteNames,
    [switch]$SkipHost
  )

  Write-Host "Repo: $Script:RepoRoot" -ForegroundColor DarkGray
  Write-Host "Remotes start with build+serve; host uses dev (started last)." -ForegroundColor DarkGray
  Write-Host ""

  foreach ($name in $RemoteNames) {
    $meta = $Script:RemoteModules | Where-Object { $_.Name -eq $name } | Select-Object -First 1
    if (-not $meta) { throw "Unknown remote module: $name" }
    Start-ModuleWindow -ModuleName $name
  }

  foreach ($name in $RemoteNames) {
    $meta = $Script:RemoteModules | Where-Object { $_.Name -eq $name } | Select-Object -First 1
    Wait-RemoteEntryJs -Port $meta.Port
  }

  if (-not $SkipHost) {
    Start-ModuleWindow -ModuleName $Script:HostModule.Name -IsHost
    Write-Host ""
    Write-Host "Host starting on http://localhost:5000" -ForegroundColor Green
    Write-Host "Open (after host finishes compiling): http://localhost:5000/auth/login" -ForegroundColor Green
  }
}

function Stop-FrontendPorts {
  $ports = @(5000, 5010, 5020, 5030, 5050, 5070, 5090, 5140)
  foreach ($p in $ports) {
    $conns = Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue |
      Select-Object -ExpandProperty OwningProcess -Unique
    foreach ($procId in $conns) {
      if (-not $procId) { continue }
      $proc = Get-Process -Id $procId -ErrorAction SilentlyContinue
      if (-not $proc) { continue }
      # Only stop node; never touch svchost etc.
      if ($proc.ProcessName -notin @('node', 'nodejs')) {
        Write-Host "Skip PID $procId ($($proc.ProcessName)) on :$p" -ForegroundColor DarkYellow
        continue
      }
      Write-Host "Stop PID $procId ($($proc.ProcessName)) on :$p" -ForegroundColor Magenta
      Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
    }
  }
  Write-Host "Done. Close leftover PowerShell windows if they are idle." -ForegroundColor DarkGray
}

function Show-FrontendStatus {
  $rows = @()
  foreach ($m in $Script:RemoteModules) {
    $ok = Test-RemoteEntryJs -Port $m.Port
    $rows += [pscustomobject]@{
      Module = $m.Name
      Port   = $m.Port
      Status = if ($ok) { 'OK (JS remoteEntry)' } else { 'DOWN / HTML / not ready' }
    }
  }
  $hostListening = $null -ne (
    Get-NetTCPConnection -LocalPort $Script:HostModule.Port -State Listen -ErrorAction SilentlyContinue |
      Select-Object -First 1
  )
  $rows += [pscustomobject]@{
    Module = $Script:HostModule.Name
    Port   = $Script:HostModule.Port
    Status = if ($hostListening) { 'LISTENING' } else { 'DOWN' }
  }
  $rows | Format-Table -AutoSize
}

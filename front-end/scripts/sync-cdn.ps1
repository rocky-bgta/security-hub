# Build MFE module(s) using the branch .env, then sync dist to web-static/{env}/
# Usage:
#   .\scripts\sync-cdn.ps1 -Environment development
#   .\scripts\sync-cdn.ps1 -Environment staging -ModuleDir home-module -S3Name home-module
# Requires: yarn, aws CLI credentials with s3 write to asatv2-media-bucket/web-static/*

param(
  [ValidateSet("development", "staging", "production")]
  [string]$Environment = "development",
  [string]$ModuleDir,
  [string]$S3Name,
  [string]$BucketPrefix
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot

if (-not (Test-Path (Join-Path $Root "host-module"))) {
  throw "host-module not found under repo root: $Root"
}

$EnvMap = @{
  development = @{
    Prefix    = "dev"
    SmokeHost = "content.aspireelearning.com"
  }
  staging     = @{
    Prefix    = "staging"
    SmokeHost = "content.aspireelearning.com"
  }
  production  = @{
    Prefix    = "prod"
    SmokeHost = "content.securityawarenesstraining.ai"
  }
}

$envCfg = $EnvMap[$Environment]
if (-not $BucketPrefix) {
  $BucketPrefix = "s3://asatv2-media-bucket/web-static/$($envCfg.Prefix)"
}
$SmokeHost = $envCfg.SmokeHost

$AllModules = @(
  @{ Dir = "host-module"; Prefix = "host" },
  @{ Dir = "home-module"; Prefix = "home-module" },
  @{ Dir = "user-module"; Prefix = "user-module" },
  @{ Dir = "content-module"; Prefix = "content-module" },
  @{ Dir = "miscellaneous-module"; Prefix = "miscellaneous-module" },
  @{ Dir = "billing-module"; Prefix = "billing-module" },
  @{ Dir = "account-module"; Prefix = "account-module" },
  @{ Dir = "phishing-module"; Prefix = "phishing-module" }
)

function Sync-Module {
  param(
    [Parameter(Mandatory = $true)][string]$Dir,
    [Parameter(Mandatory = $true)][string]$Prefix
  )

  $modPath = Join-Path $Root $Dir
  $dist = Join-Path $modPath "dist"
  if (-not (Test-Path $modPath)) {
    throw "Module directory not found: $modPath"
  }

  Write-Host "==== Building $Dir ====" -ForegroundColor Cyan
  Push-Location $modPath
  try {
    yarn install --frozen-lockfile
    if ($LASTEXITCODE -ne 0) {
      throw "yarn install failed for $Dir"
    }

    yarn build
    if ($LASTEXITCODE -ne 0) {
      throw "yarn build failed for $Dir"
    }

    if (-not (Test-Path $dist)) {
      throw "Build failed: missing dist for $Dir"
    }

    $s3 = "$BucketPrefix/$Prefix"
    Write-Host "==== Syncing $dist -> $s3 ====" -ForegroundColor Cyan
    aws s3 sync $dist $s3 `
      --exclude "index.html" `
      --exclude "assets/remoteEntry.js" `
      --cache-control "public, max-age=2592000, immutable" `
      --metadata-directive REPLACE
    if ($LASTEXITCODE -ne 0) {
      throw "aws s3 sync failed for $Dir"
    }

    $remoteEntry = Join-Path $dist "assets" "remoteEntry.js"
    if (Test-Path $remoteEntry) {
      aws s3 cp $remoteEntry "$s3/assets/remoteEntry.js" `
        --content-type "application/javascript" `
        --cache-control "no-cache, no-store, must-revalidate, max-age=0" `
        --metadata-directive REPLACE
      if ($LASTEXITCODE -ne 0) {
        throw "aws s3 cp remoteEntry.js failed for $Dir"
      }

      $smokeUrl = "https://$SmokeHost/web-static/$($envCfg.Prefix)/$Prefix/assets/remoteEntry.js"
      Write-Host "==== Smoke-test $smokeUrl ====" -ForegroundColor Cyan
      $curl = Get-Command curl.exe -ErrorAction SilentlyContinue
      if (-not $curl) {
        $curl = Get-Command curl -ErrorAction Stop
      }
      & $curl.Source -fsSI $smokeUrl | Out-Null
      if ($LASTEXITCODE -ne 0) {
        throw "Smoke-test failed for $smokeUrl"
      }
    }
  }
  finally {
    Pop-Location
  }
}

if ($ModuleDir) {
  if (-not $S3Name) {
    throw "-S3Name is required when -ModuleDir is set"
  }
  Sync-Module -Dir $ModuleDir -Prefix $S3Name
}
else {
  foreach ($m in $AllModules) {
    Sync-Module -Dir $m.Dir -Prefix $m.Prefix
  }
}

Write-Host "Done ($Environment). Smoke e.g. https://$SmokeHost/web-static/$($envCfg.Prefix)/home-module/assets/remoteEntry.js" -ForegroundColor Green

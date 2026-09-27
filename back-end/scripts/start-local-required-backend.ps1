$ErrorActionPreference = 'Stop'

$repo = Split-Path -Parent $PSScriptRoot
Set-Location $repo

$mongoLine = Get-Content .env | Where-Object { $_ -match '^MONGODB_URI=' } | Select-Object -First 1
$mongoUri = $mongoLine -replace '^MONGODB_URI=', ''

function Start-AsatService {
  param(
    [Parameter(Mandatory = $true)][string]$Name,
    [Parameter(Mandatory = $true)][string]$GradleTask,
    [Parameter(Mandatory = $true)][hashtable]$ExtraEnv
  )

  $envSetup = @(
    '$ErrorActionPreference = "Continue"',
    "Set-Location '$repo'",
    '$env:SPRING_PROFILES_ACTIVE = "local"',
    "`$env:MONGODB_URI = '$mongoUri'",
    '$env:REDIS_HOST = "localhost"',
    '$env:AC_ALLOW_ORIGINS = "http://localhost,http://localhost:5010,http://localhost:5030,http://127.0.0.1,http://127.0.0.1:5010,http://127.0.0.1:5030"'
  )

  foreach ($key in $ExtraEnv.Keys) {
    $envSetup += "`$env:$key = '$($ExtraEnv[$key])'"
  }

  $log = Join-Path $repo "$Name-local-run.log"
  $command = ($envSetup + ".\gradlew.bat $GradleTask *> '$log'") -join '; '

  Start-Process -FilePath 'powershell' `
    -ArgumentList @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-Command', $command) `
    -WindowStyle Hidden
}

Start-AsatService `
  -Name 'registration' `
  -GradleTask ':services:registration:service:bootRun' `
  -ExtraEnv @{
    SERVICE_CMS_URL = 'http://localhost:5050/cms/api/v1'
    SERVICE_AUTH_URL = 'http://localhost:9093/auth/api/v1/auth'
    CLIENT_NOTIFICATION_URL = 'http://localhost:5656/notification'
  }

Start-AsatService `
  -Name 'cms' `
  -GradleTask ':services:cms:service:bootRun' `
  -ExtraEnv @{
    REGISTRATION_SERVICE_URL = 'http://localhost:9090/registration/api/v1'
    BILLING_SERVICE_URL = 'http://localhost:6060/billing/api/v1'
    PHISHING_SERVICE_URL = 'http://localhost:5002/phishing/api/v1'
  }

Start-AsatService `
  -Name 'auth' `
  -GradleTask ':services:auth:service:bootRun' `
  -ExtraEnv @{
    SERVICE_CMS_URL = 'http://localhost:5050/cms/api/v1'
    SERVICE_REGISTRATION_URL = 'http://localhost:9090/registration/api/v1'
    SERVICE_BILLING_URL = 'http://localhost:6060/billing/api/v1'
  }

Start-Sleep -Seconds 5

Start-AsatService `
  -Name 'gateway' `
  -GradleTask ':services:gateway:service:bootRun' `
  -ExtraEnv @{
    SPRING_CLOUD_GATEWAY_ROUTES_0_URI = 'http://localhost:5050'
    SPRING_CLOUD_GATEWAY_ROUTES_1_URI = 'http://localhost:9090'
    SPRING_CLOUD_GATEWAY_ROUTES_2_URI = 'http://localhost:9093'
    SPRING_CLOUD_GATEWAY_ROUTES_3_URI = 'http://localhost:6060'
    SPRING_CLOUD_GATEWAY_ROUTES_4_URI = 'http://localhost:5656'
    SPRING_CLOUD_GATEWAY_ROUTES_5_URI = 'http://localhost:9099'
    SPRING_CLOUD_GATEWAY_ROUTES_6_URI = 'http://localhost:5002'
    SPRING_CLOUD_GATEWAY_ROUTES_7_URI = 'http://localhost:6080'
  }

Write-Host 'Started required local backend services.'

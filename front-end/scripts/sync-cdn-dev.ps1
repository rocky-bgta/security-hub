# Local convenience wrapper — syncs development CDN prefix using this branch's .env.
param(
  [string]$ModuleDir,
  [string]$S3Name
)

$script = Join-Path $PSScriptRoot "sync-cdn.ps1"
if ($ModuleDir) {
  & $script -Environment development -ModuleDir $ModuleDir -S3Name $S3Name
}
else {
  & $script -Environment development
}

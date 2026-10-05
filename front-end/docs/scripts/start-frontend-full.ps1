# One-click: all remotes (build+serve) then host (dev).

. "$PSScriptRoot\_frontend-common.ps1"
Start-FrontendStack -RemoteNames @(
  'home-module',
  'user-module',
  'content-module',
  'billing-module',
  'phishing-module',
  'account-module',
  'miscellaneous-module'
)

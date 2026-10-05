# One-click: home-module (build+serve) then host-module (dev).
# Enough for http://localhost:5000/auth/login

. "$PSScriptRoot\_frontend-common.ps1"
Start-FrontendStack -RemoteNames @('home-module')

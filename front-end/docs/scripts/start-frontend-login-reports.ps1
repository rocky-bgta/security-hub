# One-click: home + miscellaneous + host (login + User Report).

. "$PSScriptRoot\_frontend-common.ps1"
Start-FrontendStack -RemoteNames @('home-module', 'miscellaneous-module')

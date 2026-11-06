#Requires -Version 7.0
$ErrorActionPreference='Stop'
. (Join-Path $PSScriptRoot 'benchmark-power-events.ps1')
$script:mode='empty'
function Get-WinEvent {
    [CmdletBinding()]param([hashtable]$FilterHashtable)
    if($script:mode -eq 'empty'){Write-Error 'No events' -ErrorId 'NoMatchingEventsFound';return}
    if($script:mode -eq 'denied'){Write-Error 'Access denied' -ErrorId 'AccessDenied';return}
    [pscustomobject]@{TimeCreated=[datetime]'2026-11-06T12:00:00';Id=506;RecordId=1;Message='standby'}
}
$empty=Get-PublicationPowerAudit -StartTime (Get-Date).AddHours(-1)
if(-not $empty.querySucceeded -or $empty.events.Count -ne 0){throw 'No matching events should be a successful empty query'}
$script:mode='denied'
$denied=Get-PublicationPowerAudit -StartTime (Get-Date).AddHours(-1)
if($denied.querySucceeded -or -not $denied.error){throw 'Access denied must never mean zero confirmed events'}
$script:mode='event'
$event=Get-PublicationPowerAudit -StartTime (Get-Date).AddHours(-1)
if(-not $event.querySucceeded -or $event.events.Count -ne 1){throw 'Standby event must be preserved'}
Write-Output '3 power-log audit cases passed: empty, access denied, standby event.'

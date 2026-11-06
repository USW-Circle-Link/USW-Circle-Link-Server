# Distinguish an empty System log result from a query that could not be performed.
function Get-PublicationPowerAudit {
    param([Parameter(Mandatory)][datetime]$StartTime,[datetime]$EndTime=(Get-Date))
    $readErrors=@()
    $events=@(Get-WinEvent -FilterHashtable @{LogName='System';ProviderName='Microsoft-Windows-Kernel-Power';Id=42,506,507;StartTime=$StartTime;EndTime=$EndTime} -ErrorAction SilentlyContinue -ErrorVariable readErrors)
    $failures=@($readErrors | Where-Object {$_.FullyQualifiedErrorId -notlike 'NoMatchingEventsFound*'})
    [pscustomobject]@{
        querySucceeded=($failures.Count -eq 0)
        startTime=$StartTime.ToString('o')
        endTime=$EndTime.ToString('o')
        events=@($events | Select-Object @{n='time';e={$_.TimeCreated.ToString('o')}},Id,RecordId,Message)
        error=($failures | ForEach-Object {$_.ToString()}) -join [Environment]::NewLine
    }
}

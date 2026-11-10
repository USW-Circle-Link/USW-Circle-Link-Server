#Requires -Version 7.0
$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$container = 'circle-week11-outbox-mysql'
$names = & docker ps -a --format '{{.Names}}'
if ($LASTEXITCODE -ne 0) { throw 'Docker Desktop Linux engine must be running.' }
if ($names -contains $container) {
    $details = (& docker inspect $container | ConvertFrom-Json)[0]
    if ($details.Config.Image -ne 'mysql:8.0.35' -or
        $details.Config.Env -notcontains 'MYSQL_DATABASE=outbox_week11' -or
        $details.Config.Env -notcontains 'MYSQL_ROOT_PASSWORD=outbox-local-only') {
        throw 'Existing container is not the dedicated disposable Outbox fixture.'
    }
    & docker start $container | Out-Null
} else {
    & docker run --detach --name $container --publish '127.0.0.1::3306' --env MYSQL_ROOT_PASSWORD=outbox-local-only --env MYSQL_DATABASE=outbox_week11 mysql:8.0.35
    if ($LASTEXITCODE -ne 0) { throw 'Could not create dedicated MySQL fixture.' }
}
$ready = $false
for ($i = 0; $i -lt 45; $i++) {
    # mysqladmin ping can succeed against the temporary initialization server before DB creation.
    & docker exec $container mysql -uroot -poutbox-local-only --database=outbox_week11 --execute='SELECT 1' 2>$null | Out-Null
    if ($LASTEXITCODE -eq 0) { $ready = $true; break }
    Start-Sleep -Seconds 2
}
if (-not $ready) { throw 'MySQL readiness timeout.' }
$binding = & docker port $container 3306
if ($binding -notmatch '^127\.0\.0\.1:(\d+)$') { throw 'Expected loopback-only binding.' }
$port = $Matches[1]
$marker = '.outbox-path-' + [guid]::NewGuid().ToString('N')
try {
    Set-Content -LiteralPath (Join-Path $repo $marker) -Value 'mapping-check'
    if (-not (Test-Path ('W:\' + $marker))) { throw 'Map W: to this repository before running.' }
} finally { Remove-Item -LiteralPath (Join-Path $repo $marker) -ErrorAction SilentlyContinue }
# Reset only this table in the verified disposable fixture, never a configured application DB.
& docker exec $container mysql -uroot -poutbox-local-only outbox_week11 -e 'DROP TABLE IF EXISTS notification_outbox'
if ($LASTEXITCODE -ne 0) { throw 'Fixture reset failed.' }
$out = Join-Path $repo ('evidence/week11/outbox-storage/' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
New-Item -ItemType Directory -Force $out | Out-Null
$previous = $env:OUTBOX_MYSQL_URL
$env:OUTBOX_MYSQL_URL = "jdbc:mysql://127.0.0.1:${port}/outbox_week11?useSSL=false&allowPublicKeyRetrieval=true&connectionTimeZone=UTC"
try {
    Push-Location $repo
    & W:\gradlew.bat -p W:\ test --tests '*NotificationOutboxRepositoryTest' --rerun-tasks --console=plain 2>&1 | Tee-Object (Join-Path $out 'test.log')
    $code = $LASTEXITCODE
    Copy-Item (Join-Path $repo 'build/test-results/test/TEST-com.USWCicrcleLink.server.notification.outbox.NotificationOutboxRepositoryTest.xml') $out
    @{operator='AI or user: record separately'; container=$container; database='outbox_week11'; exitCode=$code; commit=(& git rev-parse HEAD)} |
        ConvertTo-Json | Set-Content (Join-Path $out 'run.json')
} finally {
    Pop-Location
    $env:OUTBOX_MYSQL_URL = $previous
}
Write-Host "Evidence: $out"
exit $code

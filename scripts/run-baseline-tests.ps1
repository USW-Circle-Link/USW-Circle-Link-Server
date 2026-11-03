param([string]$Drive = 'W:')
$ErrorActionPreference = 'Stop'
$repoPath = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$container = 'circle-baseline-week10'
if ($Drive -notmatch '^[A-Z]:$') { throw 'Drive must be one drive letter, for example W:.' }
$mapped = (& subst) | Where-Object { $_.StartsWith($Drive + '\:') }
if ($mapped) {
    # Avoid decoding subst's OEM output, which can corrupt Korean paths.
    $markerName = '.baseline-path-' + [guid]::NewGuid().ToString('N')
    $markerPath = Join-Path $repoPath $markerName
    try {
        [IO.File]::WriteAllText($markerPath, $markerName)
        if (-not (Test-Path ($Drive + '\' + $markerName))) { throw "$Drive already maps to another folder." }
    } finally { Remove-Item -LiteralPath $markerPath -ErrorAction SilentlyContinue }
} elseif (Test-Path ($Drive + '\')) { throw "$Drive is already occupied." }
else { & subst $Drive $repoPath; if ($LASTEXITCODE -ne 0) { throw 'subst failed' } }
& docker info --format '{{.ServerVersion}}'
if ($LASTEXITCODE -ne 0) { throw 'Start Docker Desktop Linux engine first.' }
$names = & docker ps -a --format '{{.Names}}'
if ($names -contains $container) {
    $details = (& docker inspect $container | ConvertFrom-Json)[0]
    if ($details.Config.Image -ne 'mysql:8.0.35' -or $details.Config.Env -notcontains 'MYSQL_DATABASE=baseline' -or $details.Config.Env -notcontains 'MYSQL_ROOT_PASSWORD=baseline-local-only') {
        throw 'Existing container does not match this disposable fixture. No data was changed.'
    }
    & docker start $container | Out-Null
} else {
    & docker run --detach --name $container --publish '127.0.0.1::3306' --env MYSQL_ROOT_PASSWORD=baseline-local-only --env MYSQL_DATABASE=baseline mysql:8.0.35
    if ($LASTEXITCODE -ne 0) { throw 'Could not create isolated MySQL container.' }
}
$ready = $false
for ($attempt = 0; $attempt -lt 30; $attempt++) {
    & docker exec $container mysqladmin ping -uroot -pbaseline-local-only --silent 2>$null | Out-Null
    if ($LASTEXITCODE -eq 0) { $ready = $true; break }
    Start-Sleep -Seconds 2
}
if (-not $ready) { throw 'MySQL readiness timeout.' }
$binding = & docker port $container 3306
if ($binding -notmatch '^127\.0\.0\.1:(\d+)$') { throw 'Expected loopback-only MySQL binding.' }
$previousUrl = $env:BASELINE_MYSQL_URL
$env:BASELINE_MYSQL_URL = "jdbc:mysql://127.0.0.1:$($Matches[1])/baseline?useSSL=false&allowPublicKeyRetrieval=true"
$logDir = Join-Path $repoPath 'evidence/week10/test-run'
New-Item -ItemType Directory -Force $logDir | Out-Null
try {
    & ($Drive + '\gradlew.bat') -p ($Drive + '\') test --rerun-tasks --console=plain 2>&1 | Tee-Object -FilePath (Join-Path $logDir 'reproduction-final.log')
    $testExit = $LASTEXITCODE
    $destination = Join-Path $logDir ('results-' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
    if (Test-Path (Join-Path $repoPath 'build/test-results/test')) {
        Copy-Item -LiteralPath (Join-Path $repoPath 'build/test-results/test') -Destination $destination -Recurse
    }
} finally { $env:BASELINE_MYSQL_URL = $previousUrl }
# Keep the dedicated container and drive alias for evidence reproduction. No automatic deletion.
exit $testExit

#Requires -Version 7.0
param([int[]]$Counts = @(50,100), [int]$Repeats = 3, [int]$Iterations = 30, [int]$Vus = 1, [switch]$Smoke)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'benchmark-power-events.ps1')
if ($Counts | Where-Object { $_ -notin @(50,100) }) { throw 'Counts must be 50 or 100.' }
if ($Repeats -lt 1 -or $Iterations -lt 1 -or $Iterations -gt 200 -or $Vus -ne 1) { throw 'This baseline runner supports VU 1 only.' }
if (-not $Smoke -and ($Iterations -lt 30 -or $Repeats -lt 3)) { throw 'Formal runs require at least 30 completions and 3 repetitions.' }
$repo = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
if ((Get-Item 'W:/gradlew.bat').FullName -ne 'W:\gradlew.bat') { throw 'Map W: to this repository first.' }
$marker = '.benchmark-' + [guid]::NewGuid().ToString('N')
try {
    Set-Content -LiteralPath (Join-Path $repo $marker) -Value 'mapping-check'
    if (-not (Test-Path ('W:/' + $marker))) { throw 'W: maps to another repository.' }
} finally { Remove-Item -LiteralPath (Join-Path $repo $marker) -ErrorAction SilentlyContinue }
$group = (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0,8)
$output = Join-Path $repo "evidence/week10/performance/$group"
New-Item -ItemType Directory -Force $output | Out-Null
$toolsDir = Join-Path $repo 'build/benchmark-tools'
New-Item -ItemType Directory -Force $toolsDir | Out-Null
$k6 = Join-Path $toolsDir 'k6-v2.3.0-windows-amd64/k6.exe'
if (-not (Test-Path $k6)) {
    $zip = Join-Path $toolsDir 'k6-v2.3.0-windows-amd64.zip'
    Invoke-WebRequest 'https://github.com/grafana/k6/releases/download/v2.3.0/k6-v2.3.0-windows-amd64.zip' -OutFile $zip
    Expand-Archive -LiteralPath $zip -DestinationPath $toolsDir -Force
}
$container = 'circle-week10-load-mysql'
$names = & docker ps -a --format '{{.Names}}'
if ($LASTEXITCODE -ne 0) { throw 'Docker is unavailable.' }
if ($names -contains $container) {
    $details = (& docker inspect $container | ConvertFrom-Json)[0]
    if ($details.Config.Image -ne 'mysql:8.0.35' -or $details.Config.Env -notcontains 'MYSQL_DATABASE=baseline_load' -or $details.Config.Env -notcontains 'MYSQL_ROOT_PASSWORD=benchmark-local-only') { throw 'Unexpected container. No data changed.' }
    & docker start $container | Out-Null
} else {
    & docker run -d --name $container --cpus 2 --memory 1g -p '127.0.0.1::3306' -e MYSQL_ROOT_PASSWORD=benchmark-local-only -e MYSQL_DATABASE=baseline_load mysql:8.0.35 | Out-Null
    if ($LASTEXITCODE -ne 0) { throw 'Could not create benchmark database.' }
}
$ready = $false
for ($i=0; $i -lt 45; $i++) {
    & docker exec $container mysqladmin ping -uroot -pbenchmark-local-only --silent 2>$null | Out-Null
    if ($LASTEXITCODE -eq 0) { $ready=$true; break }
    Start-Sleep -Seconds 2
}
if (-not $ready) { throw 'Benchmark database not ready.' }
$binding = & docker port $container 3306
if ($binding -notmatch '^127\.0\.0\.1:(\d+)$') { throw 'Database must bind only loopback.' }
$previousUrl=$env:BENCHMARK_MYSQL_URL; $previousOutput=$env:BENCHMARK_OUTPUT
$env:BENCHMARK_MYSQL_URL="jdbc:mysql://127.0.0.1:$($Matches[1])/baseline_load?useSSL=false&allowPublicKeyRetrieval=true"
$env:BENCHMARK_OUTPUT=$output
$manifest = @{
    startedAt=(Get-Date).ToString('o'); sourceCommit=(& git -C $repo rev-parse HEAD); branch=(& git -C $repo branch --show-current)
    scope='Current synchronous publication implementation; real loopback HTTP/controller/service/JPA/MySQL; fixture authentication, fake Google token, WireMock 200ms; no Redis/S3/mail integration.'
    counts=$Counts; repeats=$Repeats; iterations=$Iterations; vus=$Vus; smoke=[bool]$Smoke; heap='-Xms1g -Xmx1g'; hikariMax=10
    k6Version=(& $k6 version | Out-String).Trim(); k6Sha256=(Get-FileHash $k6 -Algorithm SHA256).Hash
    docker=(& docker info --format '{{json .}}' | ConvertFrom-Json | Select-Object ServerVersion,NCPU,MemTotal)
    db=(& docker inspect $container | ConvertFrom-Json | Select-Object -ExpandProperty HostConfig | Select-Object NanoCpus,Memory,PortBindings)
    mysqlBinding=$binding
    host=Get-CimInstance Win32_ComputerSystem | Select-Object NumberOfLogicalProcessors,TotalPhysicalMemory
    operator='AI (Codex) unless a human explicitly records their own execution.'
    data='Unique random UUIDs; deterministic half PASS/half FAIL; all WAIT and fake FCM tokens; independent club per request.'
}
$manifest | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath (Join-Path $output 'manifest.json') -Encoding utf8
& git -C $repo diff --binary | Set-Content -LiteralPath (Join-Path $output 'working-tree.patch') -Encoding utf8
$snapshot = Join-Path $output 'harness'
New-Item -ItemType Directory -Force $snapshot | Out-Null
foreach ($source in @('build.gradle','scripts/run-publication-benchmark.ps1','scripts/benchmark-power-events.ps1','scripts/k6/publication.js','src/test/java/com/USWCicrcleLink/server/baseline/PublicationBenchmarkServer.java','src/test/java/com/USWCicrcleLink/server/baseline/BaselineReproductionTest.java','src/test/resources/logback-benchmark.xml')) {
    Copy-Item -LiteralPath (Join-Path $repo $source) -Destination (Join-Path $snapshot ([IO.Path]::GetFileName($source)))
}
Get-ChildItem -LiteralPath $snapshot -File | Get-FileHash -Algorithm SHA256 | Select-Object @{n='file';e={Split-Path $_.Path -Leaf}},Hash | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $output 'harness-sha256.json') -Encoding utf8
$launcher = Join-Path $repo 'build/benchmark-launch.ps1'
Set-Content -LiteralPath $launcher -Value '& W:\gradlew.bat -p W:\ publicationBenchmark --no-daemon --console=plain; exit $LASTEXITCODE'
$server=$null; $base=$null; $failures=0
$lock = [IO.File]::Open((Join-Path $toolsDir 'running.lock'), [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
$awakeSet=$false
try {
    if (-not ('PublicationBenchmarkAwake' -as [type])) {
        Add-Type -TypeDefinition @'
using System;
using System.Threading;
using System.Runtime.InteropServices;
public static class PublicationBenchmarkAwake {
    [DllImport("kernel32.dll", SetLastError=true)] static extern uint SetThreadExecutionState(uint flags);
    static Thread worker;
    static ManualResetEventSlim ready, release;
    static uint result;
    public static uint Start() {
        ready=new ManualResetEventSlim(false); release=new ManualResetEventSlim(false);
        worker=new Thread(() => {
            result=SetThreadExecutionState(0x80000003u); // continuous + system + display
            ready.Set();
            try { release.Wait(); } finally { SetThreadExecutionState(0x80000000u); }
        });
        worker.IsBackground=true; worker.Name="publication-benchmark-awake"; worker.Start();
        if(!ready.Wait(TimeSpan.FromSeconds(5))) { release.Set(); throw new TimeoutException("Awake request did not initialize"); }
        return result;
    }
    public static void Stop() {
        if(release!=null) release.Set();
        if(worker!=null && !worker.Join(5000)) throw new TimeoutException("Awake request did not release");
    }
}
'@
    }
    if ([PublicationBenchmarkAwake]::Start() -eq 0) { [PublicationBenchmarkAwake]::Stop(); throw 'Could not request temporary automatic-sleep and display-off prevention.' }
    $awakeSet=$true
    @{requestedAt=(Get-Date).ToString('o'); systemRequired=$true; displayRequired=$true; holder='dedicated background thread'; permanentPowerPolicyChanged=$false; limitation='Does not prevent explicit lid-close, power-button sleep, or critical battery shutdown.'} | ConvertTo-Json | Set-Content (Join-Path $output 'awake-request.json') -Encoding utf8
    $server=Start-Process -FilePath (Join-Path $PSHOME 'pwsh.exe') -ArgumentList @('-NoProfile','-File','W:\build\benchmark-launch.ps1') -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $output 'server.log') -RedirectStandardError (Join-Path $output 'server-error.log')
    for ($i=0; $i -lt 180; $i++) {
        if (Test-Path (Join-Path $output 'ready.json')) { break }
        if ($server.HasExited) { throw "Server exited. See $output/server-error.log and server.log" }
        Start-Sleep -Seconds 1
    }
    if (-not (Test-Path (Join-Path $output 'ready.json'))) { throw 'Server startup timeout.' }
    $info=Get-Content (Join-Path $output 'ready.json') -Raw | ConvertFrom-Json
    $base="http://127.0.0.1:$($info.port)"; $headers=@{Authorization='Bearer benchmark-local-only'}
    if (-not $info.javaVersion.StartsWith('17.')) { throw 'Benchmark must run on Java 17.' }
    $unauth=Invoke-WebRequest -Method Post -Uri "$base/benchmark/start" -SkipHttpErrorCheck
    if ($unauth.StatusCode -ne 401) { throw 'Unauthenticated requests must be rejected.' }
    Write-Host "Output: $output"
    function Invoke-Experiment([int]$Count,[int]$N,[string]$Id,[bool]$Warmup) {
        $folder=Join-Path $output $Id; New-Item -ItemType Directory -Force $folder | Out-Null
        $dataset=Invoke-RestMethod -Method Post -Uri "$base/benchmark/prepare?count=$Count&batches=$N&runId=$Id" -Headers $headers -TimeoutSec 300
        $uuids=@($dataset.batches | ForEach-Object { $_.body | ForEach-Object { $_.aplictUUID } })
        if (($uuids | Select-Object -Unique).Count -ne ($Count*$N)) { throw 'Dataset reused applicants.' }
        $file=Join-Path $folder 'dataset.json'; $dataset | ConvertTo-Json -Depth 10 -Compress | Set-Content -LiteralPath $file -Encoding utf8NoBOM
        Invoke-RestMethod -Method Post -Uri "$base/benchmark/start" -Headers $headers | Out-Null
        $experimentStarted=Get-Date
        Write-Host "START $Id : applicants=$Count, requests=$N, VU=$Vus, warmup=$Warmup, $(Get-Date -Format o)"
        & $k6 run --quiet --no-color --env "DATASET=$file" --env "BASE_URL=$base" --env "VUS=$Vus" --env "ITERATIONS=$N" --out "json=$(Join-Path $folder 'raw.jsonl')" --summary-export (Join-Path $folder 'summary.json') (Join-Path $repo 'scripts/k6/publication.js') *> (Join-Path $folder 'k6.log')
        $code=$LASTEXITCODE
        $verification=$null; $verificationError=$null
        try { $verification=Invoke-RestMethod -Method Post -Uri "$base/benchmark/stop" -Headers $headers -TimeoutSec 120 }
        catch {
            $verificationError=$_.ToString()
            try { Invoke-RestMethod -Method Post -Uri "$base/benchmark/observations" -Headers $headers -TimeoutSec 30 | Out-Null }
            catch { Write-Warning "Partial observation capture failed: $_" }
        }
        $powerAudit=Get-PublicationPowerAudit -StartTime $experimentStarted
        $powerEvents=@($powerAudit.events)
        $powerAudit | ConvertTo-Json -Depth 5 | Set-Content (Join-Path $folder 'power-audit.json') -Encoding utf8
        ConvertTo-Json -InputObject $powerEvents -Depth 4 | Set-Content (Join-Path $folder 'power-events.json') -Encoding utf8
        $summary=Get-Content (Join-Path $folder 'summary.json') -Raw | ConvertFrom-Json
        $passed=($code -eq 0 -and $null -ne $verification -and $powerAudit.querySucceeded -and $powerEvents.Count -eq 0 -and $verification.db.published -eq ($Count*$N) -and $verification.db.members -eq ($Count*$N/2) -and $verification.fcmCalls -eq ($Count*$N))
        $record=@{runId=$Id; applicants=$Count; vus=$Vus; warmup=$Warmup; smoke=[bool]$Smoke; passed=$passed; k6Exit=$code; verificationError=$verificationError; powerLogQuerySucceeded=$powerAudit.querySucceeded; powerEventCount=$powerEvents.Count; minimumDurationMet=($Warmup -or [bool]$Smoke -or $summary.metrics.http_reqs.count/$summary.metrics.http_reqs.rate -ge 180); finishedAt=(Get-Date).ToString('o')}
        $record | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $folder 'experiment.json') -Encoding utf8
        if (-not $passed -or -not $record.minimumDurationMet) { throw "Experiment failed verification: $Id" }
        Write-Host "DONE $Id : p95=$($summary.metrics.http_req_duration.'p(95)') ms, published=$($verification.db.published), members=$($verification.db.members), FCM=$($verification.fcmCalls)"
    }
    if ($Smoke) { Invoke-Experiment 2 1 'smoke-2' $false }
    else {
        foreach ($count in $Counts) {
            for ($repeat=1; $repeat -le $Repeats; $repeat++) {
                $id="n${count}-vu${Vus}-r${repeat}"
                $warmupN=[int][Math]::Ceiling(60000/($count*200))*$Vus
                Invoke-Experiment $count $warmupN "$id-warmup" $true
                Invoke-Experiment $count $Iterations $id $false
            }
        }
    }
    Write-Host "All requested experiments verified. Output: $output"
} finally {
    try {
    if ($base) { try { Invoke-RestMethod -Method Post -Uri "$base/benchmark/shutdown" -Headers @{Authorization='Bearer benchmark-local-only'} | Out-Null } catch { Write-Warning $_ } }
    if ($server -and -not $server.WaitForExit(30000)) {
        # Only terminate the exact benchmark JVM recorded in this run's private output folder.
        $pidFile = Join-Path $output 'server-pid.json'
        if (Test-Path $pidFile) {
            $ownedPid = (Get-Content -LiteralPath $pidFile -Raw | ConvertFrom-Json).pid
            $owned = Get-CimInstance Win32_Process -Filter "ProcessId=$ownedPid"
            if ($owned -and $owned.CommandLine -match 'com\.USWCicrcleLink\.server\.baseline\.PublicationBenchmarkServer' -and $owned.CreationDate -ge $server.StartTime.AddSeconds(-2)) {
                Stop-Process -Id $ownedPid -Force
                Write-Warning "Graceful shutdown failed; stopped this run's benchmark JVM $ownedPid."
            }
        }
        if (-not $server.WaitForExit(10000)) {
            # --no-daemon gives this launcher its own Gradle process tree.
            & taskkill /PID $server.Id /T /F | Out-Null
            if ($LASTEXITCODE -ne 0) { Write-Warning "Could not stop owned launcher PID $($server.Id)." }
        }
    }
    } finally {
        $env:BENCHMARK_MYSQL_URL=$previousUrl; $env:BENCHMARK_OUTPUT=$previousOutput
        $lock.Dispose()
        if ($awakeSet) {
            [PublicationBenchmarkAwake]::Stop()
            @{releasedAt=(Get-Date).ToString('o')} | ConvertTo-Json | Set-Content (Join-Path $output 'awake-released.json') -Encoding utf8
        }
    }
}

"""Per-run API trends only. Control HTTP durations must never enter API percentiles."""
import csv
import importlib.util
import json
import statistics
import sys
from pathlib import Path

spec = importlib.util.spec_from_file_location('baseline', Path(__file__).with_name('summarize-publication-benchmark.py'))
baseline = importlib.util.module_from_spec(spec)
spec.loader.exec_module(baseline)
read = baseline.read_json


def summarize(folder):
    experiment = read(folder / 'experiment.json')
    if experiment['warmup'] or experiment['smoke']:
        return None
    metrics = read(folder / 'summary.json')['metrics']
    verification = read(folder / 'verification.json')
    dataset = read(folder / 'dataset.json')
    targets = baseline.verify_targets(dataset, read(folder / 'fcm.json'))
    (folder / 'target-integrity.json').write_text(json.dumps(targets, indent=2), encoding='utf-8')
    points = [json.loads(line) for line in (folder / 'raw.jsonl').read_text(encoding='utf-8').splitlines() if line.strip()]
    n = sum(p.get('type') == 'Point' and p.get('metric') == 'publication_duration' for p in points)
    if n != len(dataset['batches']) or n < 30:
        raise ValueError('Need >=30 fresh API responses; exclude control requests')
    durations = metrics['publication_duration']
    elapsed = metrics['http_reqs']['count'] / metrics['http_reqs']['rate']
    resources = [json.loads(line) for line in (folder / 'resources.jsonl').read_text(encoding='utf-8').splitlines()]
    return dict(run_id=experiment['runId'], applicants=experiment['applicants'], samples=n,
                p50_ms=durations['med'], p95_ms=durations['p(95)'], p99_ms=durations['p(99)'],
                api_failure_rate=metrics['publication_failed']['value'],
                cycle_p95_ms=metrics['publication_delivery_cycle']['p(95)'],
                max_task_completion_ms=verification['delivery']['maxCompletionMs'],
                measured_api_rps=n/elapsed, fcm_per_second=verification['fcmCalls']/elapsed,
                pool_active_peak=max(s['active'] for s in resources), pool_waiting_peak=max(s['waiting'] for s in resources),
                acquisition_p95_ms=verification['acquisition']['p95Ms'],
                heap_peak_mb=max(s['heapUsedBytes'] for s in resources)/1024**2,
                published=verification['db']['published'], members=verification['db']['members'],
                sent=verification['delivery']['sent'], fcm_calls=verification['fcmCalls'],
                passed=experiment['passed'] and experiment['minimumDurationMet'] and verification['delivery']['verified'] and targets['passed'],
                evidence=folder.name)


def main(group):
    rows = [r for folder in sorted(group.iterdir()) if folder.is_dir() and (folder/'experiment.json').exists()
            if (r := summarize(folder)) is not None]
    if not rows or not all(r['passed'] for r in rows):
        raise ValueError('Incomplete or invalid measurements; no success report generated')
    with (group/'summary.csv').open('w', newline='', encoding='utf-8-sig') as f:
        writer = csv.DictWriter(f, fieldnames=rows[0].keys()); writer.writeheader(); writer.writerows(rows)
    comparisons = []
    repo = Path(__file__).resolve().parents[1]
    for count in sorted({r['applicants'] for r in rows}):
        after = [r['p95_ms'] for r in rows if r['applicants']==count]
        before_group = '20261105-203107-2f3a38ac' if count==50 else '20261106-125044-5fb3dc5d'
        before = [read(repo/f'evidence/week10/performance/{before_group}/n{count}-vu1-r{i}/summary.json')['metrics']['http_req_duration']['p(95)'] for i in range(1,4)]
        comparisons.append(dict(applicants=count, before_p95_ms=before, after_p95_ms=after,
                                before_run_p95_mean_ms=statistics.mean(before), after_run_p95_mean_ms=statistics.mean(after),
                                run_p95_mean_reduction_percent=100*(1-statistics.mean(after)/statistics.mean(before)),
                                all_runs_below_200ms=all(v<200 for v in after)))
    (group/'comparison.json').write_text(json.dumps(dict(runs=rows, comparisons=comparisons,
        caveats=['Run p95 means are not a pooled percentile.', 'Control request durations excluded.',
                 'One batch in flight; not saturation throughput.', 'Fake Google bearer and local WireMock200ms.',
                 'Before and After measured on different dates; same configured limits, shared host contention uncontrolled.']), indent=2), encoding='utf-8')
    print(json.dumps(comparisons, indent=2))


if __name__=='__main__':
    main(Path(sys.argv[1]))

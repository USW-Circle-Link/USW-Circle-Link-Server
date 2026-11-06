"""Summarize each independent k6 run, retaining errors and small-sample caveats."""
import argparse
import csv
import json
import statistics
from collections import Counter, defaultdict
from pathlib import Path


def read_json(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))


def request_counts(points):
    result = dict(attempts=0, http_failures=0, business_failures=0, timeouts=0)
    for point in points:
        if point.get('type') != 'Point':
            continue
        metric, data = point['metric'], point['data']
        if metric == 'http_reqs':
            result['attempts'] += int(data['value'])
        elif metric == 'http_req_failed':
            result['http_failures'] += int(data['value'])
            if str(data.get('tags', {}).get('error_code')) == '1050':
                result['timeouts'] += 1
        elif metric == 'publication_failed':
            result['business_failures'] += int(data['value'])
    return result


def sample_label(n):
    return 'insufficient: fewer than 30 responses' if n < 30 else f'p99 reference only; n={n}'


def verify_targets(dataset, journal):
    expected = {f"{dataset['runId']}-{batch}-{i}" for batch in range(len(dataset['batches'])) for i in range(dataset['count'])}
    targets = Counter(json.loads(entry['body'])['message']['token'] for entry in journal)
    missing = len(expected - targets.keys())
    unexpected = sum(n for token, n in targets.items() if token not in expected)
    duplicates = sum(n - 1 for n in targets.values() if n > 1)
    failed = sum(entry['status'] != 200 for entry in journal)
    return dict(expected=len(expected), observed=len(journal), unique_targets=len(targets), missing=missing,
                unexpected_calls=unexpected, duplicate_calls=duplicates, non_200=failed,
                passed=missing == unexpected == duplicates == failed == 0 and len(journal) == len(expected))


def group_statistics(rows):
    groups = defaultdict(list)
    for row in rows:
        groups[row['applicants'], row['vus']].append(row)
    result = []
    for (count, vus), runs in sorted(groups.items()):
        valid = [run for run in runs if run.get('passed', True) and run.get('minimum_duration_met', True)]
        values = [run['p95_ms'] for run in valid]
        result.append(dict(applicants=count, vus=vus, runs=len(valid), excluded_invalid_runs=len(runs)-len(valid),
                           run_p95_mean_ms=statistics.mean(values) if values else None,
                           run_p95_min_ms=min(values) if values else None, run_p95_max_ms=max(values) if values else None))
    return result


def summarize(folder):
    experiment = read_json(folder / 'experiment.json')
    if experiment['warmup'] or experiment['smoke']:
        return None
    summary = read_json(folder / 'summary.json')['metrics']
    verified = read_json(folder / 'verification.json')
    targets = verify_targets(read_json(folder / 'dataset.json'), read_json(folder / 'fcm.json'))
    (folder / 'target-integrity.json').write_text(json.dumps(targets, indent=2), encoding='utf-8')
    resources = [json.loads(line) for line in (folder / 'resources.jsonl').read_text(encoding='utf-8').splitlines()]
    with (folder / 'raw.jsonl').open(encoding='utf-8') as stream:
        counts = request_counts(json.loads(line) for line in stream if line.strip())
    total = counts['attempts']
    if total != summary['http_reqs']['count']:
        raise ValueError(f'{folder}: raw and summary request counts differ')
    elapsed = total / summary['http_reqs']['rate']
    duration = summary['http_req_duration']
    cpu = [sample['processCpuLoad'] for sample in resources if sample['processCpuLoad'] >= 0]
    row = dict(run_id=experiment['runId'], applicants=experiment['applicants'], vus=experiment['vus'],
               elapsed_seconds=elapsed, **counts, failure_percent=100 * counts['business_failures'] / total,
               p50_ms=duration['med'], p95_ms=duration['p(95)'], p99_ms=duration['p(99)'],
               rps=summary['http_reqs']['rate'], published=verified['db']['published'], members=verified['db']['members'],
               fcm_calls=verified['fcmCalls'], applicants_per_second=verified['db']['published'] / elapsed,
               fcm_calls_per_second=verified['fcmCalls'] / elapsed,
               pool_active_peak=max(sample['active'] for sample in resources), pool_waiting_peak=max(sample['waiting'] for sample in resources),
               acquisition_p95_ms=verified['acquisition']['p95Ms'], acquisition_max_ms=verified['acquisition']['maxMs'],
               cpu_mean_percent=100 * statistics.mean(cpu) if cpu else None,
               heap_peak_mb=max(sample['heapUsedBytes'] for sample in resources) / 1024**2,
               missing_fcm_targets=targets['missing'], duplicate_fcm_calls=targets['duplicate_calls'],
               passed=experiment['passed'] and targets['passed'], minimum_duration_met=experiment['minimumDurationMet'],
               sample_note=sample_label(total), evidence=str(folder.name))
    return row


def write_csv(path, rows):
    if not rows:
        return
    with path.open('w', encoding='utf-8-sig', newline='') as stream:
        writer = csv.DictWriter(stream, fieldnames=list(rows[0]))
        writer.writeheader()
        writer.writerows(rows)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('directory', type=Path)
    args = parser.parse_args()
    rows = []
    seen_applications = set()
    for path in sorted(args.directory.glob('*/experiment.json')):
        row = summarize(path.parent)
        if row:
            dataset = read_json(path.parent / 'dataset.json')
            identifiers = [item['aplictUUID'] for batch in dataset['batches'] for item in batch['body']]
            if len(set(identifiers)) != len(identifiers) or seen_applications.intersection(identifiers):
                raise ValueError(f'{path.parent}: application UUID reuse detected')
            seen_applications.update(identifiers)
            rows.append(row)
    if not rows:
        raise SystemExit('No completed formal experiments found')
    rows.sort(key=lambda row: (row['applicants'], row['vus'], row['run_id']))
    write_csv(args.directory / 'summary.csv', rows)
    write_csv(args.directory / 'condition-summary.csv', group_statistics(rows))
    (args.directory / 'summary.json').write_text(json.dumps(rows, ensure_ascii=False, indent=2), encoding='utf-8')
    print('run, p50(ms), p95(ms), p99(ms), failure(%), members, FCM, validated')
    for row in rows:
        print(f"{row['run_id']}, {row['p50_ms']:.2f}, {row['p95_ms']:.2f}, {row['p99_ms']:.2f}, {row['failure_percent']:.2f}, {row['members']}, {row['fcm_calls']}, {row['passed']}")
    print('Each percentile belongs to its own run. p99 is a small-sample reference; no pooled percentile is computed.')


if __name__ == '__main__':
    main()

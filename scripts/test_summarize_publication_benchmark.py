import importlib.util
import pathlib
import unittest

spec = importlib.util.spec_from_file_location('benchmark_summary', pathlib.Path(__file__).with_name('summarize-publication-benchmark.py'))
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class SummaryTests(unittest.TestCase):
    def test_report_generators_reject_counts_that_do_not_match_their_text(self):
        for filename in ('write-performance-report.py', 'write-performance-weekly-report.py'):
            report_spec = importlib.util.spec_from_file_location('report', pathlib.Path(__file__).with_name(filename))
            report = importlib.util.module_from_spec(report_spec)
            report_spec.loader.exec_module(report)
            rows = [dict(run_id=f'n{count}-vu1-r{repeat}', applicants=count, vus=1, attempts=30,
                         passed=True, minimum_duration_met=True, http_failures=0, business_failures=0, timeouts=0)
                    for count in (50, 100) for repeat in (1, 2, 3)]
            self.assertTrue(report.valid_report_rows(rows))
            rows[0]['attempts'] = 31
            self.assertFalse(report.valid_report_rows(rows))
            self.assertFalse(report.valid_report_rows(rows[1:]))

    def test_timeouts_are_in_failure_denominator(self):
        raw = [
            {'type': 'Point', 'metric': 'http_reqs', 'data': {'value': 1}},
            {'type': 'Point', 'metric': 'http_reqs', 'data': {'value': 1}},
            {'type': 'Point', 'metric': 'http_req_failed', 'data': {'value': 0, 'tags': {'status': '200'}}},
            {'type': 'Point', 'metric': 'http_req_failed', 'data': {'value': 1, 'tags': {'status': '0', 'error_code': '1050'}}},
            {'type': 'Point', 'metric': 'publication_failed', 'data': {'value': 0}},
            {'type': 'Point', 'metric': 'publication_failed', 'data': {'value': 1}},
        ]
        self.assertEqual(module.request_counts(raw), {'attempts': 2, 'http_failures': 1, 'business_failures': 1, 'timeouts': 1})

    def test_business_error_with_http_200_is_still_failure(self):
        raw = [{'type': 'Point', 'metric': 'publication_failed', 'data': {'value': 1}}]
        self.assertEqual(module.request_counts(raw)['business_failures'], 1)

    def test_run_percentiles_are_reported_as_run_statistics(self):
        rows = [{'applicants': 100, 'vus': 1, 'p95_ms': 100}, {'applicants': 100, 'vus': 1, 'p95_ms': 300}]
        self.assertEqual(module.group_statistics(rows)[0]['run_p95_mean_ms'], 200)
        self.assertNotIn('overall_p95_ms', module.group_statistics(rows)[0])

    def test_small_sample_is_explicit(self):
        self.assertEqual(module.sample_label(3), 'insufficient: fewer than 30 responses')
        self.assertEqual(module.sample_label(30), 'p99 reference only; n=30')

    def test_same_total_calls_cannot_hide_missing_and_duplicate_targets(self):
        dataset = {'runId': 'sample', 'count': 2, 'batches': [{}]}
        journal = [{'status': 200, 'body': '{"message":{"token":"sample-0-0"}}'}] * 2
        result = module.verify_targets(dataset, journal)
        self.assertFalse(result['passed'])
        self.assertEqual(result['missing'], 1)
        self.assertEqual(result['duplicate_calls'], 1)

    def test_one_successful_call_per_target(self):
        dataset = {'runId': 'sample', 'count': 2, 'batches': [{}]}
        journal = [{'status': 200, 'body': '{"message":{"token":"sample-0-' + str(i) + '"}}'} for i in range(2)]
        self.assertTrue(module.verify_targets(dataset, journal)['passed'])

    def test_failed_or_short_runs_do_not_enter_condition_statistics(self):
        rows = [
            {'applicants': 50, 'vus': 1, 'p95_ms': 100, 'passed': True, 'minimum_duration_met': True},
            {'applicants': 50, 'vus': 1, 'p95_ms': 1, 'passed': False, 'minimum_duration_met': True},
            {'applicants': 50, 'vus': 1, 'p95_ms': 2, 'passed': True, 'minimum_duration_met': False},
        ]
        summary = module.group_statistics(rows)[0]
        self.assertEqual(summary['runs'], 1)
        self.assertEqual(summary['excluded_invalid_runs'], 2)
        self.assertEqual(summary['run_p95_mean_ms'], 100)


if __name__ == '__main__':
    unittest.main()

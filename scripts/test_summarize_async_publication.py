import importlib.util
import json
import tempfile
import unittest
from pathlib import Path

spec = importlib.util.spec_from_file_location('async_summary', Path(__file__).with_name('summarize-async-publication.py'))
summary = importlib.util.module_from_spec(spec)
spec.loader.exec_module(summary)


class AsyncSummaryTest(unittest.TestCase):
    def test_control_requests_do_not_enter_api_statistics_and_duplicate_target_fails(self):
        with tempfile.TemporaryDirectory() as temp:
            folder = Path(temp)
            def put(name, data):
                (folder/name).write_text(json.dumps(data), encoding='utf-8')
            put('experiment.json', dict(warmup=False, smoke=False, runId='test', applicants=2,
                                        passed=True, minimumDurationMet=True))
            put('dataset.json', dict(count=2, batches=[{}]*30, runId='test'))
            journal = [dict(status=200, body=json.dumps(dict(message=dict(token=f'test-{b}-{i}'))))
                       for b in range(30) for i in range(2)]
            put('fcm.json', journal)
            put('summary.json', dict(metrics=dict(
                publication_duration={'med':10,'p(95)':12,'p(99)':13},
                http_req_duration={'p(95)':9999},
                http_reqs=dict(count=60, rate=2), publication_failed=dict(value=0),
                publication_delivery_cycle={'p(95)':3000})))
            put('verification.json', dict(db=dict(published=60,members=30),fcmCalls=60,
                delivery=dict(maxCompletionMs=3000,sent=60,verified=True),acquisition=dict(p95Ms=1)))
            (folder/'raw.jsonl').write_text('\n'.join(json.dumps(dict(type='Point',metric=m))
                for _ in range(30) for m in ['publication_duration','http_reqs','http_reqs']),encoding='utf-8')
            (folder/'resources.jsonl').write_text(json.dumps(dict(active=1,waiting=0,heapUsedBytes=1024)),encoding='utf-8')
            row=summary.summarize(folder)
            self.assertEqual(row['samples'],30)
            self.assertEqual(row['p95_ms'],12)
            self.assertEqual(row['measured_api_rps'],1)
            self.assertTrue(row['passed'])
            journal[-1]=journal[0]
            put('fcm.json',journal)
            self.assertFalse(summary.summarize(folder)['passed'])


if __name__=='__main__':
    unittest.main()

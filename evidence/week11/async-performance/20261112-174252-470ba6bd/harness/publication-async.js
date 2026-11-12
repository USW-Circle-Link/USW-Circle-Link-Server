import http from 'k6/http';
import { check } from 'k6';
import exec from 'k6/execution';
import { Trend, Rate } from 'k6/metrics';
const data = JSON.parse(open(__ENV.DATASET));
const api = new Trend('publication_duration', true);
const cycle = new Trend('publication_delivery_cycle', true);
const failed = new Rate('publication_failed');
export const options = {
  scenarios: { publication: { executor: 'shared-iterations', vus: 1, iterations: Number(__ENV.ITERATIONS), maxDuration: '60m' } },
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(95)', 'p(99)'],
  thresholds: { publication_failed: ['rate==0'], checks: ['rate==1'], http_req_failed: ['rate==0'] },
};
export default function () {
  const batch = data.batches[exec.scenario.iterationInTest];
  if (!batch) exec.test.abort('Fresh dataset exhausted');
  const started = Date.now();
  const headers = { Authorization: 'Bearer benchmark-local-only', 'Content-Type': 'application/json' };
  const response = http.post(`${__ENV.BASE_URL}/clubs/${batch.clubUUID}/applicants/notifications`, JSON.stringify(batch.body), {
    headers, timeout: '120s', tags: { name: 'POST publication' },
  });
  api.add(response.timings.duration);
  let body = {}; try { body = response.json(); } catch (_) {}
  const ok = check(response, { 'publication HTTP 200': r => r.status === 200, 'new publication': () => body.message === '지원 결과 처리 완료' });
  failed.add(!ok);
  // Control endpoint is deliberately OUTSIDE publication_duration. Single batch in flight.
  const delivery = http.post(`${__ENV.BASE_URL}/benchmark/await-delivery`, null, {
    headers, timeout: '120s', tags: { name: 'CONTROL await delivery' },
  });
  check(delivery, { 'all tasks delivered': r => r.status === 200 && r.json('completed') === true });
  cycle.add(Date.now() - started);
}

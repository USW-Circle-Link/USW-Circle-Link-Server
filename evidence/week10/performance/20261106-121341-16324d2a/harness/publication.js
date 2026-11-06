import http from 'k6/http';
import { check } from 'k6';
import exec from 'k6/execution';
import { Rate } from 'k6/metrics';
const data = JSON.parse(open(__ENV.DATASET));
const businessFailed = new Rate('publication_failed');
export const options = {
  scenarios: { publication: { executor: 'shared-iterations', vus: Number(__ENV.VUS || 1), iterations: Number(__ENV.ITERATIONS || 30), maxDuration: '60m' } },
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(95)', 'p(99)'],
  thresholds: { http_req_failed: ['rate==0'], publication_failed: ['rate==0'], checks: ['rate==1'] },
};
export default function () {
  const batch = data.batches[exec.scenario.iterationInTest];
  if (!batch) exec.test.abort('Fresh dataset exhausted; never reuse an application');
  const response = http.post(`${__ENV.BASE_URL}/clubs/${batch.clubUUID}/applicants/notifications`, JSON.stringify(batch.body), {
    headers: { Authorization: 'Bearer benchmark-local-only', 'Content-Type': 'application/json' }, timeout: '120s',
    tags: { name: 'POST publication', applicants: String(data.count) },
  });
  let body = {}; try { body = response.json(); } catch (_) {}
  const passed = check(response, { 'HTTP 200': r => r.status === 200, 'new publication completed': () => body.message === '지원 결과 처리 완료' });
  businessFailed.add(!passed);
}

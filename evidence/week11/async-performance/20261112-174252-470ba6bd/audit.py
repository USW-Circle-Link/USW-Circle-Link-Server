"""Independent raw-point, fresh-data, delivery and pacing audit. Run after measurements stop."""
import hashlib
import json
import math
from collections import defaultdict
from datetime import datetime
from pathlib import Path

root = Path(__file__).resolve().parent
read = lambda path: json.loads(path.read_text(encoding='utf-8-sig'))
all_ids = []
runs = []
boundaries = 0

def percentile(values, p):
    ordered = sorted(values)
    index = (len(ordered)-1)*p
    lo = math.floor(index)
    hi = math.ceil(index)
    return ordered[lo] + (ordered[hi]-ordered[lo])*(index-lo)

for count in (50,100):
    for repeat in (1,2,3):
        folder=root/f'n{count}-vu1-r{repeat}'
        experiment=read(folder/'experiment.json')
        assert experiment['passed'] and experiment['minimumDurationMet'] and experiment['k6Exit']==0
        assert not experiment['warmup'] and not experiment['smoke']
        dataset=read(folder/'dataset.json')
        assert dataset['count']==count and len(dataset['batches'])==30
        all_ids += [a['aplictUUID'] for b in dataset['batches'] for a in b['body']]
        points=[json.loads(line) for line in (folder/'raw.jsonl').read_text(encoding='utf-8').splitlines()]
        api=[p['data']['value'] for p in points if p.get('type')=='Point' and p.get('metric')=='publication_duration']
        failures=[p['data']['value'] for p in points if p.get('type')=='Point' and p.get('metric')=='publication_failed']
        assert len(api)==len(failures)==30 and sum(failures)==0
        trend=read(folder/'summary.json')['metrics']['publication_duration']
        for label,p in [('med',.5),('p(95)',.95),('p(99)',.99)]:
            assert math.isclose(percentile(api,p),trend[label],rel_tol=1e-9)
        verify=read(folder/'verification.json')
        assert verify['db']=={'published':count*30,'members':count*15}
        assert verify['fcmCalls']==verify['delivery']['tasks']==verify['delivery']['sent']==count*30
        assert read(folder/'target-integrity.json')['passed']
        deliveries=read(folder/'deliveries.json')
        assert len(deliveries)==count*30 and all(d['status']=='SENT' and d['attempts']==1 for d in deliveries)
        batches=defaultdict(list)
        for row in deliveries:
            batches[int(row['token'].rsplit('-',2)[1])].append(row)
        assert set(batches)==set(range(30)) and all(len(v)==count for v in batches.values())
        for i in range(1,30):
            previous=max(datetime.fromisoformat(d['sentAt']) for d in batches[i-1])
            current=min(datetime.fromisoformat(d['createdAt']) for d in batches[i])
            assert current>=previous, f'Overlapping batches: {folder.name}/{i}'
            boundaries+=1
        runs.append(dict(run=folder.name,responses=len(api),api_p95_ms=percentile(api,.95),
                         published=count*30,members=count*15,sent=count*30,failures=sum(failures)))

assert len(all_ids)==len(set(all_ids))==13500
for item in read(root/'harness-sha256.json'):
    actual=hashlib.sha256((root/'harness'/item['file']).read_bytes()).hexdigest()
    assert actual.upper()==item['Hash'].upper()
result=dict(status='passed',operator='AI',sourceCommit=read(root/'manifest.json')['sourceCommit'],
            runs=runs,apiRequests=180,freshApplications=13500,members=6750,sent=13500,
            apiFailures=0,missingTargets=0,duplicateTargets=0,pacingBoundariesVerified=boundaries,
            rawPercentilesMatchSummary=True,harnessHashesMatch=True,
            scope='Normal 200 / WireMock200ms / fake Google bearer / VU1 with delivery wait; no handset test')
(root/'verification-audit.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
print(json.dumps(result,indent=2))

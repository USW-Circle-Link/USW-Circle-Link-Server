from pathlib import Path
import json
from collections import Counter
from hashlib import sha256
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]
ev = root / "evidence/week10/reproduction"
suites = []
for file in (root / "build/test-results/test").glob("TEST-*.xml"):
    s = ET.parse(file).getroot()
    suites.append({k:s.get(k) for k in ["name","tests","failures","errors","skipped","timestamp","time"]})
def load(name):
    return json.loads((ev / name).read_text(encoding="utf-8"))
def races(prefix):
    return Counter((x["snapshot"]["a1"],x["snapshot"]["members"],tuple(r["http"] for r in x["responses"]))
                   for x in (load(f.name) for f in ev.glob(prefix+"-*.json")))
concurrent = [load(f.name) for f in ev.glob("PUB-06-*.json")]
apply = load("APPLY-03.json")
auth = load("AUTH-matrix.json")
manifest = {"baselineCommit":"794306c2b48c5d3e92a8a8018b2701e32a4c14dd", "suites":suites,
    "codeSha256":{str(p.relative_to(root)).replace("\\","/"):sha256(p.read_bytes()).hexdigest() for p in [
        root/"src/test/java/com/USWCicrcleLink/server/baseline/BaselineReproductionTest.java",
        root/"src/main/java/com/USWCicrcleLink/server/club/leader/service/FcmServiceImpl.java",root/"build.gradle"]}}
(ev/"run-manifest.json").write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding="utf-8")
total=sum(int(s["tests"]) for s in suites)
failed=sum(int(s["failures"])+int(s["errors"]) for s in suites)
skipped=sum(int(s["skipped"]) for s in suites)
lines=["# 10주차 상세 시나리오 실행 결과", "", "실행일: 2026-11-03 (Asia/Seoul). 브랜치: `refactor/baseline`.",
    f"전체 {total}개, 실패 {failed}개, 건너뜀 {skipped}개. 기존 단위 테스트 5개와 새 재현 테스트를 함께 실행했다.",
    "테스트 통과는 현재 결함을 포함한 Before 재현의 성공이며 개선 후 합격 기준 달성을 뜻하지 않는다.", "",
    "## 실행 환경과 변경 범위", "",
    "- Gradle 8.8, JDK 17 toolchain, 전용 MySQL 8.0.35 컨테이너, Hikari 최대 10개, WireMock 3.9.1.",
    "- MockMvc → 실제 컨트롤러/서비스/예외 처리 → 실제 JPA 트랜잭션/MySQL → 로컬 FCM HTTP 대역.",
    "- JWT 공급자는 mock이다. 보안 행렬은 실제 SecurityFilterChain/JwtFilter/권한 규칙을 사용하지만 실제 JWT 서명·Redis·로그인 경로는 검증하지 않는다.",
    "- 무관한 S3 의존성은 mock이며 운영 서버는 실행하지 않았다. 기본 FCM 지연 200ms, ERR-DELAY 3000ms.",
    "- FCM URL 외부화, 인증·transport 교체용 protected 메서드만 추가했다. 순차 호출·트랜잭션·기존 오류 처리는 유지했다.",
    "- Google 인증 비용은 제외됐다. 전용 DB는 테스트 시작/종료에 create-drop으로 초기화된다.",
    "- 재현 파일에 합성 토큰·프로필만 사용했다. 실제 Firebase 키를 사용하지 않았다.", "",
    "## 관측 결과", "",
    f"- PUB-06: 10개 동시 발표 × {len(concurrent)}회. 회원 행 수 분포: {dict(Counter(x['snapshot']['members'] for x in concurrent))}. FCM 요청 수 분포: {dict(Counter(len(x['fcm']) for x in concurrent))}.",
    f"- APPLY-03: 최종 지원서 {apply['applications']}행·답변 {apply['answers']}행. HTTP 분포: {dict(Counter(r['http'] for r in apply['responses']))}. 실제 MySQL DDL에 uk_aplict_active 존재 확인.",
    "- PUB-04/05: 순차 재발표·본문 중복 각각 회원 1행을 유지하지만 FCM 2회 호출.",
    "- ERR-400/404/429/500: API 500, 앞선 200 FCM 호출은 남지만 결과 공개·신규 회원은 롤백. 프로필 토큰은 유지.",
    "- ERR-IO: 인증 공급 단계 IOException을 삼킨 뒤 API 200, 결과·회원 커밋, 두 번째 FCM 호출 없음.",
    "- ERR-DELAY: 3초 지연 후 커밋. ERR-READ-TIMEOUT: 테스트 전용 1초 read timeout에서 API 500·롤백.",
    "- ERR-CLIENT-TIMEOUT: 1초 클라이언트 timeout 뒤에도 처리 완료·커밋 관측. JDK HTTP bridge → MockMvc 구성으로, 실제 Tomcat 취소 동작의 검증은 아니다.",
    "- RACE-02: 공개 후 FAIL 수정이 200이며 최종 FAIL/PASS, 회원 1행, PASS 알림으로 불일치.",
    f"- 보안 행렬: {len(auth)}개 조합 중 기대 HTTP와 일치 {sum(x['http']==x['expected'] for x in auth)}개.",
    "- APPLY-07: 타 동아리 질문·다른 질문 옵션 연결이 저장됨. optionIds가 optionId보다 우선함.",
    "- 빈 qnaList 제출은 200, 없는 질문·옵션은 500. 삭제일이 있는 기존 지원서도 재지원은 400.",
    "- 잘못된 목록 query/경로 타입은 현재 일반 예외 처리로 500을 반환함.", "",
    "### 경합 관측 분포", "", "상태 표기는 private/public. 각 응답 코드는 발표·수정 또는 해당 JSON의 responses 순서다.", "",
    "| 실험 | 상태 | 회원 행 | HTTP 조합 | 횟수 |", "|---|---|---:|---|---:|"]
for prefix in ["RACE-03","RACE-04"]:
    for (state,members,codes),count in races(prefix).items():
        lines.append(f"| {prefix} | {state} | {members} | {codes} | {count} |")
lines += ["", "RACE-03은 FCM 응답 수신 직후 transport interceptor에서 발표 반환을 보류한다. 원래 문서의 대역 응답 보류 대신 같은 트랜잭션 경계에서 클라이언트 반환을 통제했다.",
    "상태 변경 API가 200인데 최종 private가 PASS라면 FAIL 변경의 덮어쓰기이며, 안전한 직렬 처리의 증거가 아니다.", "",
    "## 원시 증거와 재실행", "",
    "```powershell", ".\\scripts\\run-baseline-tests.ps1", "```", "",
    "PowerShell 7 사용 권장. Docker Desktop Linux 엔진이 필요하다. 스크립트는 전용 컨테이너를 검증하고 동적 포트를 찾아 환경변수를 설정한다. W:가 다른 경로면 중단한다.",
    "- `evidence/week10/test-run/reproduction-final.log`: 최종 Gradle 로그.",
    "- `evidence/week10/test-run/results-*`: 실행별 JUnit XML 보존본.",
    "- `evidence/week10/reproduction/*.json`: HTTP 응답·DB 결과·FCM 요청 기록. 기록되지 않은 세부 assertion은 JUnit XML에서 확인.",
    "- `evidence/week10/reproduction/mysql-aplict-ddl.txt`: 실제 테이블 DDL.",
    "- `evidence/week10/reproduction/run-manifest.json`: 기준 커밋·코드 SHA256·JUnit 집계.",
    "- `src/test/java/com/USWCicrcleLink/server/baseline/BaselineReproductionTest.java`: 시나리오 구현.", "",
    "## 준비 과정 문제와 한계", "",
    "- 컴파일/프록시/MVC 설정과 보안 fixture 누락을 수정한 중간 실행 로그도 보존했다. 이는 서비스 결함으로 집계하지 않았다.",
    "- 초기 대역 URL이 @Value 초기화로 덮어써져 가짜 토큰으로 실제 FCM에 요청, 401을 받았다. 실행을 중단하고 기본 URL을 로컬 차단값으로 고정한 뒤 setup에서 대역 URL을 적용했다. 이후 정상·오류 테스트는 WireMock journal로 확인했다.",
    "- wiremock journal의 200은 대역 접수 성공이지 단말 수신이 아니다. read timeout 뒤 대역 응답 상태는 서버 시도이며 클라이언트가 응답을 받았다는 뜻이 아니다.",
    "- 테스트는 순차 실행이고 fixture별 UUID/동아리를 분리했다. 동시 테스트는 10회 관측이며 모든 스케줄을 증명하지 않는다.",
    "- API/DB 오류·경합 재현을 수행했다. k6 부하 측정, p95/RPS/커넥션 기준선, 워커 재시도·복구 및 운영 전체 연동은 아직 수행하지 않았다.",
    "- 구현한 테스트는 대표 하위 조건을 묶어 검증한다. 각 인증 조합의 오류 본문 전체 필드 고정, 모든 status/query 조합의 교차곱, 초기/최종 전체 DB 덤프까지 포괄한 것은 아니다.", ""]
(root/"docs/week10/reproduction-results.md").write_text("\n".join(lines),encoding="utf-8")
print(total,failed,skipped)

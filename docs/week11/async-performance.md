# 비동기 발표 성능 측정

## 현재 상태

2026-11-12: 측정 도구 준비 중. **아직 After 실측값 없음.**
Docker Desktop 내부 소켓 초기화 오류로 MySQL 컨테이너에 접근할 수 없다.
임시 소켓 정리 시도는 자동 승인 검토에서 차단되어 사용자의 Docker 정상 실행을 기다린다.
DB 데이터 삭제나 운영 서비스 접근은 수행하지 않았다.

## 재현 명령

저장소를 W:로 매핑하고 PowerShell7에서 실행한다. 이 명령은 전용 테스트 DB만 사용한다.

```powershell
.\scripts\run-publication-benchmark.ps1 -Mode async -Smoke
.\scripts\run-publication-benchmark.ps1 -Mode async -Counts 50,100 -Repeats 3 -Iterations 30
# 완료 후 출력된 결과 디렉터리 지정
python .\scripts\summarize-async-publication.py evidence/week11/async-performance/<실행번호>
```

## 비교 조건

50명/100명, VU1, 새 지원서 30회, 조건별 3회. 매 회차 워밍업은 집계에서 제외한다.
Java17, heap1GB, Hikari최대10, MySQL8.0.35/CPU2/메모리1GB,
Google 인증 제외, 동일 JVM의 WireMock200ms, 실제 로컬 HTTP/컨트롤러/서비스/JPA.
권한 인증은 기존 측정과 같은 테스트 관리자 대역이다. 운영 환경 수치가 아니다.

Outbox 모드와 실제 단일 폴링 작업자를 사용한다. 한 번에 최대10개를 순차 전송하고
폴링 사이에 기본1초를 기다리는 현재 코드를 그대로 측정한다. FCM은 실제 HTTP 클라이언트에
가짜 Bearer만 주입한다. Google 키 파일/인증 서버에는 접근하지 않는다.

발표 응답 후 해당 배치의 SENT 커밋 완료를 기다리고 다음 배치를 제출한다.
대기용 HTTP 요청은 **API 응답 시간 집계에서 제외**한다. 단일 배치가 진행되는 실험이며,
API를 쉬지 않고 계속 호출하여 알림 적체를 만드는 포화 부하 시험이 아니다.
관측용 Store는 실제 complete()의 DB 커밋 후 메모리 카운터만 증가시킨다.
대기 중 DB 반복 조회로 Hikari 지표를 오염시키지 않는다.

## 읽을 결과

- `publication_duration`: 발표 API만의 p50/p95/p99. **기본 http_req_duration을 비교하면 안 된다.**
- `publication_delivery_cycle`: API 호출 시작부터 해당 배치 전체 SENT 확인까지. 100ms 관측 간격 포함.
- `deliveries.json`: 각 작업 생성/FCM 접수 성공 기록 시각, 시도 횟수, 상태. 단말 수신 시간이 아니다.
- `verification.json`, `fcm.json`, `target-integrity.json`: 공개/회원/SENT 수, 대상별 누락/중복 호출.
- `resources.jsonl`: 동일 JVM의 API/작업자/대역 서버 자원 사용. API 전용 CPU 수치가 아니다.
- `raw.jsonl`, `summary.json`: k6 원시값과 실행별 요약. 기본 http_reqs는 대기 요청까지 포함하여 발표 수의 두 배다.
- `measured_api_rps`: 발표 수/전체 실험 시간. 배치 완료를 기다린 속도이므로 최대 API 처리량으로 해석하지 않는다.

성공 판정에는 공개 수=대상 수, 회원 수=절반, 작업 수=SENT 수=FCM 고유 대상 수=대상 수가 필요하다.
실패 응답도 지연 집계에서 제외하지 않는다. 실패율과 함께 해석한다.

Before 출처: Week10 `20261105-203107-2f3a38ac`(50명), `20261106-125044-5fb3dc5d`(100명).
실행별 p95를 그대로 비교하고 평균은 **실행별 p95의 평균**으로 표기한다.
서로 다른 실행의 p95를 합쳐 전체 p95라고 쓰지 않는다. 각30표본의 p99는 참고값이다.
날짜/호스트의 다른 작업/시작 DB 누적량은 완전히 동일하지 않으므로 엄격한 인과 추정에는 한계가 있다.

## 수행 구분

AI: 측정 도구 작성 및 자동 검증. 본인 직접 실행/판단: 아직 확인되지 않음.
After 실측 및 비교 결과는 Docker 복구 후 이 문서에 추가한다.

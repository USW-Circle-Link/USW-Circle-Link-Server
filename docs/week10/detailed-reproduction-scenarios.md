# 10주차 — 오류·경합·초기 데이터 상세 시나리오

기준: `794306c2b48c5d3e92a8a8018b2701e32a4c14dd`, `refactor/baseline`.
상위 명세: [API 계약 및 시나리오](api-contract-and-scenarios.md).
상태: 설계 후 테스트를 구현·실행했다. [실행 결과와 범위](reproduction-results.md)를 함께 읽는다. 아래 Before는 설계 당시 코드에 근거한 예상이며, 실제 관측값은 결과 문서와 JSON에 기록한다.

## 1. 공통 초기값과 판정 규칙

각 테스트는 독립된 MySQL 데이터셋에서 실행한다. 시작 전 이전 요청·트랜잭션이 모두 종료됐는지 확인하고 WireMock 요청 기록·시나리오 상태를 초기화한다.
테스트 전체를 하나의 롤백 트랜잭션으로 감싸지 않는다. 준비 데이터는 먼저 커밋하고, 관측은 요청 종료 후 새 트랜잭션에서 한다.

| 식별자 | 초기값 |
|---|---|
| C1 / C2 | 서로 다른 동아리, 각 회장 L1 / L2 |
| ADMIN / U1 | 유효한 관리자 / 일반 사용자 인증. U1은 P1 프로필 보유 |
| A1 | C1·P1 지원서, private PASS / public WAIT, deleteDate null |
| A2 | C1·P2 지원서, private FAIL / public WAIT, deleteDate null |
| A3 | C1·P3 지원서, private WAIT / public WAIT, deleteDate null |
| A4 | C2·P4 지원서, private PASS / public WAIT, deleteDate null |
| AX | DB에 없는 고정 UUID |
| 회원 | 기본값: C1의 P1/P2/P3 회원 행 0개 |
| FCM | 기본값: 프로필별 서로 다른 가짜 토큰 T1/T2/T3/T4, 공급자는 고정 가짜 인증 토큰 반환 |
| 대역 응답 | 기본값: 유효한 FCM 성공 JSON과 HTTP 200, 지연 200ms |

표의 회원 수는 전체 테이블 수가 아닌 **해당 (club, profile)의 행 수**다.
FCM 요청 수는 WireMock이 받은 HTTP 요청, 접수 성공 수는 대역이 200을 반환한 수다. 실제 단말 수신은 측정하지 않는다.
상태 표기는 `private/public`이며 모든 테스트는 L1으로 호출하되 권한 테스트는 별도 지정한다.
현재는 Outbox가 없으므로 작업 수·재시도 수를 측정했다고 기록하지 않는다.

### 1.1 기본 시나리오의 정확한 기대값

| 상위 ID | 초기값 변경 / 요청 | Before 종료 상태 | 회원 수 | FCM 요청 / 접수 성공 |
|---|---|---|---|---|
| PUB-01 | [A1,A2] 순서 발표 | A1 PASS/PASS, A2 FAIL/FAIL | P1=1, P2=0 | 2 / 2 |
| PUB-02 | [A3] 발표 | A3 WAIT/WAIT | P3=0 | 0 / 0 |
| PUB-03 | P1 토큰 null; [A1] | A1 PASS/PASS | P1=1 | 0 / 0 |
| PUB-04 | [A1]을 순차 2회 | A1 PASS/PASS | P1=1 | 2 / 2 |
| PUB-05 | 단일 본문 [A1,A1] | A1 PASS/PASS | P1=1 | 2 / 2 |
| PUB-MEMBER | P1 회원 1개 미리 생성; [A1] | A1 PASS/PASS | P1=1 | 1 / 1 |
| PUB-09A | [A1,AX] | A1 PASS/WAIT (롤백 예상) | P1=0 | 1 / 1 |
| PUB-09B | [AX,A1] | A1 PASS/WAIT | P1=0 | 0 / 0 |
| PUB-09C | [A1,A4] | A1 PASS/WAIT, A4 PASS/WAIT | P1=0 | 1 / 1 |
| STATUS-01 | A3에 PASS→FAIL→WAIT PATCH | 각 단계 public WAIT, 최종 WAIT/WAIT | P3=0 | 0 / 0 |
| STATUS-02 | A1 PASS/PASS·회원 1개로 시작; FAIL PATCH | A1 FAIL/PASS | P1=1 | 0 / 0 |

정상 발표 응답은 200, PUB-09A/B/C는 404 APT-202로 예상한다. 각 테스트에서 실제 HTTP·예외 코드를 함께 보존한다.

### 1.2 목록·제출 데이터

- LIST-01: C1에 서로 다른 프로필을 사용해 private WAIT/PASS/FAIL × public WAIT/PASS/FAIL의 9행을 준비한다. 필터 없음 9행, status만 지정 3행, 공개 true만 6행, false만 3행, status+true 2행, status+false 1행이다. C2의 행은 어떤 결과에도 포함하지 않는다. 순서는 비교하지 않는다.
- LIST-02: C1 지원서 0행으로 시작해 200과 빈 배열을 확인한다.
- APPLY-01: 별도 데이터셋에서 U1의 C1 지원서·회원 0행으로 시작한다. C1 소속 텍스트 질문 1개, 단일선택 질문 1개(옵션 1개 선택), 다중선택 질문 1개(옵션 2개 선택)를 준비한다. 제출 후 지원서 1행, WAIT/WAIT, 답변 4행, 회원 증가 0, FCM 0회를 확인한다.
- APPLY-02: APPLY-01의 커밋된 상태에서 같은 요청을 재전송한다. 400 APT-205, 지원서 1행·답변 4행 유지가 예상된다.
- APPLY-03: 지원서·회원 0행에서 동일 사용자 10개 요청을 시작 장벽으로 동시에 제출한다. 종료 후 지원서 1행·답변 4행을 기대하되 성공/실패 응답 분포는 실측한다. 실제 Unique 인덱스를 먼저 확인한다.
- APPLY-04: 기존 지원서 없이 회원 중복, 다른 프로필 회원의 전화번호 중복, 학번 중복을 각각 독립 데이터셋으로 구성한다. 한 번에 하나의 거절 조건만 활성화하고 지원서 증가 0을 확인한다.

모든 거절 시나리오는 응답뿐 아니라 관련 지원서·답변·회원 행 수와 FCM 증가량 0을 확인한다. 단, PUB-09처럼 선행 외부 호출을 관측하는 결함 재현은 예외다.

## 2. 오류별 상세 시나리오

공통 요청은 [A1,A2]. T1에는 정상 200을 반환하고 T2에만 지정 오류를 주어 순서를 고정한다.
전역 호출 순서만으로 매핑하지 않고 **message.token**으로 구분한다. ERR-IO만 인증 공급자 2번째 호출에서 IOException을 주입한다.
이 테스트는 기존 순차 전송·예외 처리를 유지한 대역 연결이 전제다.

| ID | T2 장애 설정 | Before 예상 API | Before 예상 DB / 회원 | FCM 요청 / 접수 성공 |
|---|---|---|---|---|
| ERR-400 | HTTP 400, FCM INVALID_ARGUMENT | 500 NO_CATCH_ERROR | A1 PASS/WAIT, A2 FAIL/WAIT, P1=0 | 2 / 1 |
| ERR-404 | HTTP 404, FCM UNREGISTERED 상세 코드 | 500 NO_CATCH_ERROR | 위와 같음 | 2 / 1 |
| ERR-429 | HTTP 429, Retry-After: 2 | 500 NO_CATCH_ERROR | 위와 같음 | 2 / 1 |
| ERR-500 | HTTP 500, INTERNAL | 500 NO_CATCH_ERROR | 위와 같음 | 2 / 1 |
| ERR-DISCONNECT | 요청을 받은 후 연결 종료 | 500 NO_CATCH_ERROR 예상 | 위와 같음 | 2 / 1 |
| ERR-DELAY | HTTP 200, T2만 3000ms 지연 | 200, 지연 증가 | A1 PASS/PASS, A2 FAIL/FAIL, P1=1 | 2 / 2 |
| ERR-IO | 인증 공급자 2번째 호출 IOException | 200 예상 | A1 PASS/PASS, A2 FAIL/FAIL, P1=1 | 1 / 1 |

RestTemplate 기본 오류 처리와 현재 IOException catch의 차이를 검증한다. HTTP 400/401/404 뒤 토큰 삭제 분기가 실제 실행되는지 단정하지 않는다.
각 오류에서 P1/P2의 토큰 값이 초기값과 같은지 별도 확인한다. 현재 HTTP 오류 경로에서는 삭제 코드에 도달하지 않을 가능성이 있으며 실제 결과를 기록한다.
ERR-429는 2초 이상 추가 관측해 자동 재시도가 없는지 요청 기록을 확인한다. Retry-After는 대역 오류 재현용 값이며 향후 운영 재시도 설정의 권장값이 아니다.

### 2.1 응답 지연과 타임아웃 분리

현재 코드에는 명시적 read timeout이 없으므로 3초 지연을 timeout이라고 부르지 않는다.

- ERR-CLIENT-TIMEOUT: 대역 T2 지연 3000ms, 테스트 클라이언트 timeout 1000ms로 요청한다. 클라이언트 timeout 시각과 서버 종료 시각을 따로 기록한다. 서버가 끝난 뒤에는 ERR-DELAY와 같은 DB 결과가 나올 수 있다. 클라이언트 연결 종료가 서버 트랜잭션 취소를 보장한다고 가정하지 않는다.
- ERR-READ-TIMEOUT: 테스트 전용 HTTP transport에서 read timeout 1000ms를 설정하고 T2를 3000ms 지연시킨다. 운영 기본값과 다른 **장애 주입 변형 실험**으로 표시한다. 런타임 통신 예외에 의한 롤백을 확인한다. 요청 2회, 200 접수 1회가 예상되지만 응답 완료 전 단절 처리 방식은 대역 로그로 확정한다.
- transport 교체가 필요하면 테스트 전용 구성으로 제한한다. 이 변형 결과를 수정 전 기본 transport의 성능 측정값과 섞지 않는다.

### 2.2 후속 개선 판정

- 어떤 FCM 오류도 이미 커밋된 결과·회원 반영을 되돌리지 않아야 한다.
- INVALID_ARGUMENT는 메시지 오류, UNREGISTERED는 토큰 무효, 인증 오류는 설정 문제로 구분한다. 상태 코드만으로 모든 토큰을 삭제하지 않는다.
- 429·일시 장애는 재처리 대상으로 기록하며, 정상 복구 후 수렴 여부는 워커 구현 이후 검증한다.
- ERR-IO처럼 API 성공과 전송 누락이 공존하는 상황에는 추적 가능한 작업 상태·실패 사유가 필요하다.

## 3. 상태 변경과 발표의 경합

대상은 A1 한 개이며 초기 private PASS / public WAIT, P1 회원 0개, 토큰 T1이다.
두 실행 스레드는 각각 독립 트랜잭션과 L1 SecurityContext를 사용한다. 종료 시 context를 정리한다.
PATCH 요청은 `{"status":"FAIL"}`, 발표 요청은 [A1]이다.

| ID | 실행 순서 | Before에서 확인할 결과 |
|---|---|---|
| RACE-01 | PATCH FAIL 완료 후 발표 | 둘 다 200, FAIL/FAIL, 회원 0, FAIL FCM 1회 |
| RACE-02 | 발표 완료 후 PATCH FAIL | 둘 다 200, FAIL/PASS, 회원 1, PASS FCM 1회 |
| RACE-03 | 발표가 PASS를 읽은 뒤 FCM 응답 대기, PATCH 시작, 대역 응답 해제 | UPDATE/INSERT·flush 시점에 따라 PATCH 대기 또는 상태 덮어쓰기 가능. 최종 private/public·회원·알림·응답을 관측 |
| RACE-04 | PATCH와 발표를 시작 장벽으로 동시에 시작 | 최종 조합과 발생 횟수 기록; 특정 경합 결과를 단정하지 않음 |

### 3.1 순서 통제와 실행 방법

1. RACE-01/02는 HTTP 요청 완료를 기다린 뒤 다음 요청을 보내는 순차 대조군이다.
2. RACE-03은 WireMock이 T1 요청을 받았다는 이벤트를 기다려 발표의 상태 읽기가 끝났음을 확인한다. 대역 응답은 테스트 latch로 보류한다.
3. PATCH를 별도 스레드에서 시작한다. PATCH 완료를 기다려야만 대역을 해제하는 구조는 사용하지 않는다. DB 락 때문에 PATCH가 대기하면 테스트 자체가 교착될 수 있다.
4. PATCH 시작 확인 후 대역을 해제하고 두 요청 종료를 각각 기다린다. HTTP 요청 순서는 통제하되 PATCH의 DB 쓰기 완료 순서는 로그로 관측한다.
5. 최대 대기 30초를 두고 초과하면 타임아웃·DB 락 상태를 저장한다. 요청이 끝나기 전에 다음 테스트 데이터를 초기화하지 않는다.
6. RACE-03/04를 새 데이터로 각 10회 반복한다. sleep만으로 순서를 맞추지 않는다. 정확한 읽기/쓰기 경계가 추가로 필요하면 테스트 전용 repository spy를 사용하는 서비스 통합 테스트를 별도로 둔다.

### 3.2 수집 결과와 판정

실행별로 PATCH·발표 시작/종료 시각, 각 HTTP/예외, private/public, 회원 수, FCM payload의 PASS/FAIL, 대역 도착 시각, DB 대기/SQL 로그를 기록한다.
현재 코드가 낸 결과를 억지로 단일 기대값에 맞추지 않는다. `FAIL/PASS + 회원 1 + PASS 알림` 또는 `FAIL/FAIL + 회원 1 + PASS 알림` 같은 불일치가 관측되면 결함 증거로 남긴다.
private 상태가 다시 PASS가 되는 덮어쓰기 가능성도 함께 확인한다.

후속 개선에서는 아래 두 직렬 실행 중 하나와 동등해야 한다.

- 상태 변경이 먼저 확정: FAIL/FAIL, 회원 0, FAIL 알림 작업 1개.
- 발표가 먼저 확정: PASS/PASS, 회원 1, PASS 알림 작업 1개. 다른 상태 PATCH는 409.

알림 작업에 저장된 결과와 공개 결과가 일치해야 하며, 동시 실행 중 어떤 조합이든 허용하는 것을 합격 기준으로 삼지 않는다.

## 4. 결과 기록 형식

각 결과에 다음 필드를 남긴다. 미실행 값은 빈칸 대신 `NOT_RUN`, 미관측 값은 `NOT_OBSERVED`로 표시한다.

```text
scenario_id, run_id, baseline_commit, harness_commit, profile,
fixture_id, initial_private, initial_public, initial_member_count,
fault_type, fault_delay_ms, client_timeout_ms, read_timeout_ms,
request_order, http_status, error_code, duration_ms,
final_private, final_public, final_member_count,
fcm_request_count, fcm_accepted_count, token_changed,
expected_match, observation, evidence_path
```

경합 결과는 두 HTTP 결과와 요청별 시각을 JSON으로 추가 보관한다. API 요청·응답, 새 트랜잭션의 DB 조회 결과, WireMock journal을 같은 run_id로 연결한다.
회원·지원서 수는 초기값과 최종값을 모두 보존한다. 실행 전후 snapshot은 가짜 토큰과 합성 개인정보만 사용한다.

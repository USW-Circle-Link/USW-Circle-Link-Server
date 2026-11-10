# Outbox 알림 작업자와 재시도

## 이번에 구현한 것

발표 API가 저장한 알림 대기 작업을 읽고, 별도 작업자가 FCM에 보내도록 연결했다. Kafka는 사용하지 않는다. API는 결과·회원·작업 저장 후 응답하며 알림 완료까지 기다리지 않는다.

브랜치 `refactor/publication-outbox`, 기준 `07774727`. 기존 사용자 Word·테스트 경로 수정은 이번 커밋에 포함하지 않는다. 구현·실행자는 AI이며 사용자 직접 검증과 구분한다.

| 상태 | 의미 |
|---|---|
| PENDING | 발송 대기 |
| PROCESSING | 작업자가30초 임대로 처리 중 |
| RETRY | 일시 실패, 다음 시각에 재시도 |
| SENT | FCM HTTP200 접수 성공. 휴대폰 수신 완료가 아님 |
| FAILED | 영구 오류 또는 최대 시도 초과. 자동 재시도 종료 |
| SKIPPED | 토큰 없음 등으로 전송 생략 |

## 동작과 실패 정책

1. 짧은 DB 트랜잭션에서1건을 `FOR UPDATE SKIP LOCKED`로 선점하고 임대 토큰을 발급한다.
2. 트랜잭션을 커밋하고 DB 연결을 반환한 뒤 인증·FCM 요청을 보낸다.
3. 별도 짧은 트랜잭션에서 같은 임대 토큰인지 확인하고 SENT·RETRY·FAILED로 기록한다.

- 1초 간격, 최대10건을 한 건씩 처리한다. 여러 작업자가 같은 작업을 동시에 선점하지 않게 한다.
- 최대5회 시도. 기본 대기2·4·8·16초이며429 Retry-After가 더 길면 그 시간을 기다린다. 초·HTTP 날짜 형식을 지원하고 비정상 값이나1일 초과 값은 기본 간격을 쓴다.
- 429·5xx·401·403·인증 IOException·연결/응답 오류는 재시도한다. 나머지4xx(400·404 포함)는 즉시 FAILED다.
- 키가 서비스 계정 형식이어야 한다. OAuth 인증과 FCM 요청 모두 연결2초·읽기5초 제한이며, 인증 SDK 내부 재시도는 꺼서 Outbox가 횟수를 관리한다. DNS 등을 포함한 전체 작업의 절대 제한 시간은 아니다.
- PROCESSING 상태에서 중단된 작업은30초 뒤 재선점할 수 있다. 이전 작업자의 늦은 완료는 새 임대 토큰과 달라 무시한다. 다섯 번째 시도 후 임대만 만료된 작업도 FAILED로 남긴다.
- 외부 FCM 접수 성공 후 DB 기록 전에 중단되면 다시 전송될 수 있다. at-least-once 방식으로 단말 알림의 정확히1회 수신을 보장하지 않는다.
- 토큰·알림 본문·키 파일·원격 오류 본문은 일반 작업자 로그에 남기지 않는다. 이전 스냅샷의 오류로 현재 프로필 토큰을 삭제하지 않는다.

## 설정과 SQL

기본 작업자는 비활성화다. 이번 단계는 전용 테스트 프로필에서만 켤 수 있으며 prod 프로필은 함께 지정해도 거절한다. 기존 운영 동기 전송 설정과 배포 DB는 변경하지 않았다.

```properties
spring.profiles.active=outbox-test
notification.publication.outbox-enabled=true
notification.outbox.worker.enabled=true
notification.outbox.worker.poll-interval-ms=1000
notification.outbox.worker.initial-delay-ms=1000
```

새 DB에는 아래 SQL을 순서대로, 기존 Outbox DB에는 두 번째 ALTER를 적용해야 한다. 이번 작업에서는 전용 테스트 DB에만 적용했다. 구버전 작업자와의 동시 운영·다운 마이그레이션은 이번 범위가 아니다.

1. `scripts/sql/20261110_create_notification_outbox.sql`
2. `scripts/sql/20261110_add_outbox_delivery.sql`

## 검증 명령과 범위

PowerShell7·Docker Desktop·W: 저장소 매핑에서 다음을 실행한다.

```powershell
.\scripts\run-outbox-worker-tests.ps1
.\scripts\run-publication-outbox-tests.ps1
.\scripts\run-baseline-tests.ps1
```

첫 명령은 전용 `circle-week11-outbox-mysql/outbox_week11`만 사용하며 해당 Outbox 테이블을 초기화한다. 제공한 CREATE+ALTER SQL 적용 후 Hibernate validate를 검사한다. 두 번째 명령은 별도 `baseline_outbox_week11` DB의 API 경로를 검사한다. fixture 이외의 DB 주소를 넣어 실행하지 않는다.

| 검사 | 증거 |
|---|---|
| 정상 전송·전송 중 DB 연결0 | 실제 MySQL/Hikari 상태와 HTTP200 |
| 처음2회500 후 정상 | 실제 시간으로3개 작업 모두60초 안에 SENT, FCM총5회 |
| 429·Retry-After | 제어한 시계로 지정 시각 전 미전송 확인 |
| 400·404 | 즉시 FAILED, 추가 호출 없음 |
| 401·403·인증 IOException·연결 단절·응답 지연 | RETRY 상태와 사유 |
| 5회 제한·2/4/8/16초 간격 | 시도 횟수·다음 시각 검사 |
| 임대 복구·늦은 완료 무시 | DB 작업을 유지한 채 작업자 인스턴스 재생성 |
| 작업자2개 동시 처리 | FCM1회·SENT1건 |
| 가짜 Google 인증 서버 | 발급 성공·읽기 제한·500단일 호출·키 파일 없음 |
| 기본 설정/잘못된 활성화 | 기본 비활성화·prod 거절·실제 스케줄러 polling |
| API부터 전체 처리 | 작업자 수동 실행 전 발표 응답에서 PENDING2·FCM0, 실행 후 SENT2·FCM2 |

인증 테스트의 RSA 키는 테스트마다 임시 파일로 만들고 삭제한다. 실제 Firebase·Google 인증 서버·S3·메일은 호출하지 않는다. 장애 JSON의 configuredStatus는 대역 서버 설정값이며 연결 단절·타임아웃 시 클라이언트가 해당 응답을 받았다는 뜻이 아니다.

통합 시험은 작업자를 수동 실행하는 순서로 검증했다. 실제 스케줄러는 DB 커밋 뒤 바로 작업을 처리할 수 있으므로 API 응답 시점에 반드시 PENDING 상태라는 계약은 아니다. 스케줄러가 실제 주기로 호출되는지는 별도 설정 테스트에서 검사한다.

## 지표와 한계

- `created_at`·`sent_at`으로 생성부터 SENT 기록까지 시간을 보관한다. FCM 성공 후 DB 기록 지연도 포함한다.
- `oldestUnfinishedAgeMillis()`로 PENDING·RETRY·PROCESSING 중 가장 오래된 작업의 대기시간을 조회하고, 스케줄러 DEBUG 로그에 남긴다. 외부 모니터링 대시보드는 아직 추가하지 않았다.
- 재시도 간격 검사는 제어한 Clock을 사용하고,500 복구 실험과 API 통합 흐름은 실제 시간을 사용한다.
- 복구 검사는 작업자 인스턴스 재생성과 임대 만료를 사용했다. OS 프로세스를 강제 종료하는 실험은 아직 하지 않았다.
- API100명 성능 재측정은 별도 작업이다. 이번 결과만으로 p95 개선 달성이나 운영 최대 처리량을 주장하지 않는다.

## 검증 결과와 Evidence

- 최종 작업자·설정·인증·기존 저장 테스트: `20261110-151634`, 28개 실패0·건너뜀0.
- API 연결·기존 동시 발표·설정·전체 발송 흐름: `publication-outbox/20261110-150519`, 23개 실패0·건너뜀0.
- 기본 동기 경로의 기존89개도 모두 통과했고 bootJar가 성공했다. 작업자/저장28개·API통합23개·기존89개를 각각 검증했다. 전체 회귀 실행에서는 DB 환경변수가 없는 전용 검사를 건너뛰고 위 전용 실행으로 따로 확인했으므로, 중복 실행된 설정·인증 검사를 다시 합산하지 않는다.
- 500을 처음2회 반환한 실제 시간 실험에서 작업3건이 모두 SENT였다. FCM 대역 호출은500·500·200·200·200이며 가장 늦은 생성~SENT 기록은 약2.48초였다. 정상 대역 추가 지연0ms의 장애 복구 검증으로,100명 성능 비교값이 아니다.
- 첫 날짜 헤더 실험은 짧은 테스트용 제한 시간에서 실패했다. 일반 성공 실험은 운영과 같은2초/5초 설정으로 분리하고, 짧은 제한은 타임아웃 전용 실험에만 사용한다. 첫 실패의 정확한 원인을 확정하지는 않았다.
- 정확한 간격 검사에서는 Java 나노초와 MySQL 마이크로초 저장 정밀도 차이를 확인했다. 제어한 테스트 시계를 마이크로초로 맞춘 뒤2·4·8·16초를 완화 없이 검증했다. 개발 중 실패 로그도 보존했다.
- 리뷰에서 발견한 OAuth 제한 시간 누락을 수정하고, 로컬 인증 서버의 성공·500·정상 토큰의 지연 응답을 검사했다. SDK의 숨은 재시도가 없음을 호출1회로 확인했다. 후속 독립 리뷰에서 추가 중요 결함은 발견되지 않았다.

대표 Evidence:

- [작업자·저장 테스트 로그](../../evidence/week11/outbox-worker/20261110-151634/test.log)
- [500두번 후 복구](../../evidence/week11/outbox-worker/20261110-151634/two500ResponsesThenSuccessWithin60Seconds.json)
- [임대 만료·이전 작업자 완료 무시](../../evidence/week11/outbox-worker/20261110-151634/expiredClaimRecoversAndIgnoresStaleCompletion.json)
- [API부터 발송 완료까지](../../evidence/week11/publication-outbox/20261110-150519/WORKER-E2E.json)
- [전체 검증 요약](../../evidence/week11/outbox-worker/verification.json)

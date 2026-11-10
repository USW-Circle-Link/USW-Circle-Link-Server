# 발표 API와 Outbox의 트랜잭션 연결

이 문서는 저장 연결 단계의 기록이다. 이후 [알림 작업자·재시도](outbox-worker.md)를 추가했으며 운영 활성화는 아직 하지 않았다.

## 목표와 변경 범위

결과 공개·합격자 회원 추가·알림 작업을 같은 트랜잭션에서 저장한다. 하나라도 실패하면 요청 전체의 DB 변경을 취소한다. 기존 Endpoint·DTO·권한·DB 락·회원 고유 키·중복 발표 응답을 유지한다.

기본 실행은 기존 FCM 동기 전송이다. Outbox 경로는 `outbox-test` 프로필과 `notification.publication.outbox-enabled=true`를 함께 지정한 전용 테스트에서만 사용한다. 다른 프로필에서 플래그를 켜면 기동을 거절한다. 운영 YAML과 배포 DB는 수정하지 않는다.

Outbox 경로에서는 FCM을 호출하거나 Google 인증 토큰을 발급받지 않는다. 성공 응답은 결과 반영과 작업 저장 완료이며 알림 전송 완료가 아니다. 전송 작업자·재시도·성능 개선 검증은 이번 작업 범위 밖이다.

## 처리 정책

- `ClubLeaderService`의 기존 트랜잭션·지원서 락 안에서 결과 공개·회원 추가·작업 저장을 처리한다.
- `PublicationOutboxWriter`는 MANDATORY 전파를 사용한다. 호출 트랜잭션 없이 실행하면 거절하고, 별도 트랜잭션을 생성하지 않는다.
- 이미 발표한 지원자·본문 UUID 중복은 추가 작업 없이 기존 응답을 유지한다. WAIT는 작업을 생성하지 않는다.
- 토큰이 없으면 결과·회원은 반영하고 SKIPPED / NO_FCM_TOKEN 작업을 남긴다.
- 잘못된 UUID·타 동아리 지원서·작업 저장 오류는 전체 DB 변경을 롤백한다. 고유 키 오류를 잡아서 성공으로 바꾸지 않는다.
- 기존 FCM과 같은 제목·합불 문구를 작업에 저장한다. 과거에 발표된 지원자에게 작업을 소급 생성하지 않는다.

## 실행 방법

Docker Desktop Linux 엔진, Java17, 저장소의 W: 매핑이 필요하다. PowerShell7에서 실행한다.

```powershell
.\scripts\run-publication-outbox-tests.ps1
```

전용 컨테이너 `circle-week11-publication-mysql`·DB `baseline_outbox_week11`만 사용한다. Hibernate create-drop으로 이 테스트 DB를 초기화하므로 다른 DB 주소를 지정해서 실행하지 않는다. 이미지·DB명·전용 비밀번호·loopback 포트를 확인한 후 시작한다.

기존 주간 테스트의 fixture·MockMvc·트랜잭션 프록시·실제 MySQL·가짜 FCM 서버를 재사용한다. 실제 로그인/JWT 전 구간 테스트는 아니며, 서비스 권한 계약을 검증한다. DB 상태는 요청 종료 후 새 트랜잭션에서 조회한다.

## 검증 시나리오

| 시나리오 | 기대 결과 |
|---|---|
| PASS·FAIL | 공개2·회원1·PENDING2·FCM0 |
| WAIT | 공개·회원·작업·FCM 변경0 |
| 토큰 없음 | 공개1·회원1·SKIPPED1·FCM0 |
| 본문 중복·순차 재요청 | 회원·작업 각각1, 두 번째는 이미 발표 응답 |
| 동일 발표10개 동시 요청, 10회 반복 | 매회 완료1·이미 발표9·회원1·작업1·FCM0·5xx0 |
| 두 번째 작업 고유 키 충돌 | HTTP500, 첫 번째 작업·전체 결과 공개·회원 추가 롤백, 사전 작업만 유지 |
| 유효+잘못된 UUID·타 동아리 지원서 | HTTP404, 전체 DB 변경0·FCM0 |
| 일반 사용자·타 동아리 회장 | HTTP403, 전체 DB 변경0·FCM0 |
| 관리자 | 기존 허용 계약 유지 |
| 호출 트랜잭션 없이 작업 저장 | IllegalTransactionStateException, 저장0건 |

각 JSON에는 요청 본문·요청 전후 DB 상태·HTTP 응답·작업 상태·FCM 호출 수를 기록한다. fixture의 T1·T2 등 가짜 토큰만 사용하며 실제 토큰·개인정보는 저장하지 않는다. 생성된 테스트 데이터는 실제 지원자 통계가 아니다.

## 증거와 남은 작업

Evidence는 `evidence/week11/publication-outbox`에 저장한다. 실행자는 AI이며 본인의 직접 검증으로 자동 기록하지 않는다. 이전 실패 로그도 보존한다.

다음 작업은 전송 작업자·실패 재시도·재시작 복구다. 작업자가 완성되기 전에는 Outbox 경로를 운영에 활성화하지 않는다.

### 2026년 11월 10일 검증 기록

- 연결 전 실행 `20261110-142446`: 18개 중16개가 예상대로 실패했다. FCM을 직접 호출하던 코드가 작업 저장 요구사항을 충족하지 못하는 것을 확인한 RED 기록이다.
- 연결 후 실행 `20261110-142730`: 통합19개·설정 보호3개가 모두 통과했다.
- 요청 Evidence 포함 실행 `20261110-142943`: 통합19개·설정 보호3개, 실패0·건너뜀0. 동시 요청은 시작 장벽을 사용해10회 반복했다.
- 기본 경로의 기존 회귀89개와 설정 보호3개가 통과했다(`regression-summary.json`). 이 실행에서는 전용 환경변수가 없는 새 통합·저장 테스트를 건너뛰고, 각각 전용 실행으로 별도 검증한다. 독립 읽기 전용 리뷰에서 중요한 결함은 발견되지 않았다.
- 기존 Outbox 저장 테스트6개도 `outbox-storage/20261110-143314`에서 다시 모두 통과했다. bootJar 성공. 새22개·기존89개·기존 저장6개를 각 실행의 XML로 확인했다(`verification.json`). 설정 보호3개는 회귀 실행에서도 반복되므로 두 실행을 더해 중복 집계하지 않는다.

대표 JSON의 `PASS/WAIT`는 내부 심사 합격/아직 미공개이며, `PASS/PASS`는 합격 결과까지 공개됐다는 뜻이다. `PENDING`은 알림 발송 대기이며 실제 전송 성공이 아니다. 롤백 실험에서는 오류를 유발하려고 미리 저장한 두 번째 지원자의 작업1건만 남아야 한다.

- [정상 결과·회원·작업 저장](../../evidence/week11/publication-outbox/20261110-143547/OUTBOX-01-normal.json)
- [동시 발표10개 요청 결과](../../evidence/week11/publication-outbox/20261110-143547/OUTBOX-05-concurrent-1.json)
- [두 번째 작업 실패 시 전체 롤백](../../evidence/week11/publication-outbox/20261110-143547/OUTBOX-06-rollback.json)
- [권한 없는 사용자](../../evidence/week11/publication-outbox/20261110-143547/OUTBOX-08-user.json)
- [최종 테스트 로그](../../evidence/week11/publication-outbox/20261110-143547/test.log)
- [20개 요청 시나리오 요약CSV](../../evidence/week11/publication-outbox/20261110-143547/scenario-summary.csv)
- [기본 동기 경로 회귀 결과](../../evidence/week11/publication-outbox/regression-summary.json)
- [전체 검증 요약](../../evidence/week11/publication-outbox/verification.json)

최종 경로 독립성 확인 실행 `20261110-143547`에서22개를 다시 모두 통과했다. 새 테스트는 기존 fixture의 미커밋 경로 변경을 사용하지 않고 자체 Evidence 경로를 사용한다.

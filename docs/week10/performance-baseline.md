# 발표 API 성능 기준선과 타임아웃 재측정 결과

지원 결과 발표는 동아리 회장이 합격·불합격 결과를 공개하고 지원자에게 알림을 보내는 기능이다. 현재는 한 명씩 외부 알림 응답을 기다린다. 이번 작업은 이때 회장이 얼마나 기다리는지 측정하고, 이전 실험 중단의 원인을 확인하는 작업이다.

**100명 조건을 별도 환경에서3회 다시 측정했다.** 아래 표는 이전에 검증된50명3회와 새100명3회를 구분해 제시한다. 이전 타임아웃은 삭제하지 않았다. 이 문서는 성능 개선 효과가 아니라 현재 동기 방식의 기준선이다.

## 이전 실패의 원인 분리

실패 요청의 시작 추정 시각은 2026-11-05 21:31:06이며, Windows Kernel-Power는 21:31:08 Power Button, 21:31:09 Lid로 대기 모드 진입을 기록했다. 최종 복귀는 11월6일11:56:48이다. Hikari의 약14시간 지연 경고도 복귀 시각과 맞는다. 따라서 이전 마지막 실행은 PC 대기 전환으로 오염된 측정으로 분류한다. 정상 서버 부하의 성능 한계라고 해석하지 않으며 모든 앱 결함을 배제했다는 뜻도 아니다.

- [조사 과정](timeout-investigation.md)
- [Windows 이벤트 증거](../../evidence/week10/performance/20261105-203107-2f3a38ac/previous-failure-power-events.json), [요청 시각 대조](../../evidence/week10/performance/20261105-203107-2f3a38ac/standby-diagnosis.json)
- [실패 포함 최초 전체 시도 CSV](../../evidence/week10/performance/20261105-203107-2f3a38ac/all-attempts.csv), [실패 기록](../../evidence/week10/performance/20261105-203107-2f3a38ac/n100-vu1-r3/failure.json)

첫 재측정에서는 정식1회가 정상 완료됐지만 다음 워밍업에서 화면 유휴에 따른 대기 진입을 감지해 중단했다. 그 결과도 보존하고, 화면 꺼짐 요구까지 보완한 이번 전체3회만 새100명 기준선으로 채택한다.

- [화면 유휴 전환이 감지된 워밍업 기록](../../evidence/week10/performance/20261106-123104-a7710ab4/n100-vu1-r2-warmup/experiment.json), [전원 이벤트](../../evidence/week10/performance/20261106-123104-a7710ab4/n100-vu1-r2-warmup/power-events.json)

## 측정 날짜와 환경 차이

| 항목 | 조건 |
|---|---|
| 대상 | POST /clubs/{clubUUID}/applicants/notifications |
| 발표 | 신규 지원자50/100명, PASS·FAIL 절반, 모두 가짜 FCM 토큰 |
| 부하 | VU1, 조건별3회, 실행당30요청, 매번 새 동아리·지원서 |
| 외부 지연 | WireMock HTTP200·200ms, Google 인증 비용 제외 |
| JVM·DB | Java17·heap1GB·Hikari10, MySQL8.0.35·CPU2·메모리1GB |
| 워밍업 | 각 실행 전 별도 새 데이터로60초 이상, 정식 집계 제외 |
| 실행 경로 | 실제 로컬 HTTP·컨트롤러·서비스·트랜잭션·JPA·MySQL, 인증은 테스트 관리자 대역 |

한 실행은 지원자100명에게 발표하는 요청을30번 보내는 실험이다. 매 요청에는 아직 발표하지 않은 새 지원자100명을 배정한다. 이 실행을3회 반복했다. 50명 결과도 같은 방식으로 측정했다.

50명은11월5일에 정상 완료한3회 결과, 100명은11월6일에 새로 실행한3회 결과다. 이전에는50명 뒤100명을 실행했으나 이번에는 빈 DB에서100명만 시작했다. 날짜·초기 DB 상태가 다르므로 두 규모의 차이가 지원자 수만으로 발생했다고 단정하거나 운영 최대 처리량으로 설명하지 않는다. 향후100명 개선 비교는 이번과 동일한 빈DB·100명 단독·3회 순서로 진행한다. 한 측정 안에서는 앞선 실행의 데이터 행을 종료까지 보존한다.

앱과 WireMock은 같은 Windows JVM이며 CPU·heap도 이 공유 JVM 값이다. k6도 같은 호스트, MySQL은 로컬 Docker다. 실제 Firebase·인터넷·Google 인증·로그인·Redis·S3·메일 전체 통합 성능은 아니다. 50명·100명은 선택한 시험 규모이며 운영 통계가 아니다.

재측정 중 자동 절전을 임시 억제하고 끝나면 해제했다. 덮개·전원 버튼 동작은 변경하지 않았다. 캠페인 전체 Windows 전원 로그의 조회 성공 및 대기 사건0건도 별도로 확인했다.

- [새 환경](../../evidence/week10/performance/20261106-125044-5fb3dc5d/manifest.json), [실제 JVM](../../evidence/week10/performance/20261106-125044-5fb3dc5d/ready.json), [전원 로그 감사](../../evidence/week10/performance/20261106-125044-5fb3dc5d/power-log-audit.json)
- [이전50명 구간의 전원 로그 감사](../../evidence/week10/performance/20261105-203107-2f3a38ac/selected-50-power-audit.json) — 조회 성공·대기 사건0건
- [절전 억제 요청](../../evidence/week10/performance/20261106-125044-5fb3dc5d/awake-request.json), [해제](../../evidence/week10/performance/20261106-125044-5fb3dc5d/awake-released.json)

## 실행별 응답 시간

| 지원자 | 출처 | 반복 | p50초 | p95초 | p99초 | 실패/시도 | 요청/초 |
|---|---|---|---|---|---|---|---|
| 50 | 원 측정11/5 | 1 | 11.622 | 11.867 | 11.926 | 0/30 | 0.0861 |
| 50 | 원 측정11/5 | 2 | 11.393 | 11.792 | 11.795 | 0/30 | 0.0874 |
| 50 | 원 측정11/5 | 3 | 11.361 | 11.453 | 11.485 | 0/30 | 0.0880 |
| 100 | 재측정11/6 | 1 | 22.292 | 22.441 | 24.721 | 0/30 | 0.0446 |
| 100 | 재측정11/6 | 2 | 22.246 | 22.364 | 22.419 | 0/30 | 0.0450 |
| 100 | 재측정11/6 | 3 | 22.245 | 22.383 | 22.426 | 0/30 | 0.0449 |

표의 정상6회는 실행마다30개 표본이며 p99는 참고값이다. 실행별 백분위수를 합친 전체p95는 만들지 않았다. 이번100명 재측정의90요청은 실패0건이며, 이전 타임아웃1건은 위 실패 기록에 별도로 남아 있다.

## DB와 알림 대상 검증

| 지원자 | 반복 | 공개 | 회원 행 | FCM 요청 | 누락/중복 | 활성/대기 최대 | 획득p95 ms |
|---|---|---|---|---|---|---|---|
| 50 | 1 | 1500 | 750 | 1500 | 0/0 | 1/0 | 0.3507 |
| 50 | 2 | 1500 | 750 | 1500 | 0/0 | 1/0 | 0.0358 |
| 50 | 3 | 1500 | 750 | 1500 | 0/0 | 1/0 | 0.0319 |
| 100 | 1 | 3000 | 1500 | 3000 | 0/0 | 1/0 | 0.0157 |
| 100 | 2 | 3000 | 1500 | 3000 | 0/0 | 1/0 | 0.0142 |
| 100 | 3 | 3000 | 1500 | 3000 | 0/0 | 1/0 | 1.3217 |

가입 회원 행은PASS수와 대조했다. FCM은 가짜 서버 HTTP 접수 수이며 실제 휴대폰 수신 수가 아니다. 지원서 UUID 재사용과 대상 토큰별 누락·중복도 검사했다. VU1에서 대기 연결이 없다는 결과로 동시 부하의 안전성을 보장하지 않는다.

## 산출물과 남은 작업

- [코호트 출처를 포함한 비교CSV](../../evidence/week10/performance/20261106-125044-5fb3dc5d/comparison-summary.csv), [동일 내용JSON](../../evidence/week10/performance/20261106-125044-5fb3dc5d/comparison-summary.json)
- [중단·미채택 실행도 포함한 전체 정식 시도 이력](../../evidence/week10/performance/20261106-125044-5fb3dc5d/history-attempts.csv) — 환경과 날짜가 다른 이력이므로 전체p95로 합치지 않는다
- [새100명 원시 요약CSV](../../evidence/week10/performance/20261106-125044-5fb3dc5d/summary.csv)
- [측정 계획·명령](performance-test-plan.md)
- 실행자AI. 사용자의 직접 코드 확인·재실행·판단 기록은 별도로 남아 있다.
- p95 200ms미만은 향후 목표다. 비동기 개선 후에는API응답과 전체 알림FCM접수까지 시간·누락·중복을 함께 검증해야 한다.

- 50명 n50-vu1-r1 (20261105-203107-2f3a38ac): [원시요청](../../evidence/week10/performance/20261105-203107-2f3a38ac/n50-vu1-r1/raw.jsonl), [DB검증](../../evidence/week10/performance/20261105-203107-2f3a38ac/n50-vu1-r1/verification.json), [FCM대상대조](../../evidence/week10/performance/20261105-203107-2f3a38ac/n50-vu1-r1/target-integrity.json), [자원시계열](../../evidence/week10/performance/20261105-203107-2f3a38ac/n50-vu1-r1/resources.jsonl)
- 50명 n50-vu1-r2 (20261105-203107-2f3a38ac): [원시요청](../../evidence/week10/performance/20261105-203107-2f3a38ac/n50-vu1-r2/raw.jsonl), [DB검증](../../evidence/week10/performance/20261105-203107-2f3a38ac/n50-vu1-r2/verification.json), [FCM대상대조](../../evidence/week10/performance/20261105-203107-2f3a38ac/n50-vu1-r2/target-integrity.json), [자원시계열](../../evidence/week10/performance/20261105-203107-2f3a38ac/n50-vu1-r2/resources.jsonl)
- 50명 n50-vu1-r3 (20261105-203107-2f3a38ac): [원시요청](../../evidence/week10/performance/20261105-203107-2f3a38ac/n50-vu1-r3/raw.jsonl), [DB검증](../../evidence/week10/performance/20261105-203107-2f3a38ac/n50-vu1-r3/verification.json), [FCM대상대조](../../evidence/week10/performance/20261105-203107-2f3a38ac/n50-vu1-r3/target-integrity.json), [자원시계열](../../evidence/week10/performance/20261105-203107-2f3a38ac/n50-vu1-r3/resources.jsonl)
- 100명 n100-vu1-r1 (20261106-125044-5fb3dc5d): [원시요청](../../evidence/week10/performance/20261106-125044-5fb3dc5d/n100-vu1-r1/raw.jsonl), [DB검증](../../evidence/week10/performance/20261106-125044-5fb3dc5d/n100-vu1-r1/verification.json), [FCM대상대조](../../evidence/week10/performance/20261106-125044-5fb3dc5d/n100-vu1-r1/target-integrity.json), [자원시계열](../../evidence/week10/performance/20261106-125044-5fb3dc5d/n100-vu1-r1/resources.jsonl)
- 100명 n100-vu1-r2 (20261106-125044-5fb3dc5d): [원시요청](../../evidence/week10/performance/20261106-125044-5fb3dc5d/n100-vu1-r2/raw.jsonl), [DB검증](../../evidence/week10/performance/20261106-125044-5fb3dc5d/n100-vu1-r2/verification.json), [FCM대상대조](../../evidence/week10/performance/20261106-125044-5fb3dc5d/n100-vu1-r2/target-integrity.json), [자원시계열](../../evidence/week10/performance/20261106-125044-5fb3dc5d/n100-vu1-r2/resources.jsonl)
- 100명 n100-vu1-r3 (20261106-125044-5fb3dc5d): [원시요청](../../evidence/week10/performance/20261106-125044-5fb3dc5d/n100-vu1-r3/raw.jsonl), [DB검증](../../evidence/week10/performance/20261106-125044-5fb3dc5d/n100-vu1-r3/verification.json), [FCM대상대조](../../evidence/week10/performance/20261106-125044-5fb3dc5d/n100-vu1-r3/target-integrity.json), [자원시계열](../../evidence/week10/performance/20261106-125044-5fb3dc5d/n100-vu1-r3/resources.jsonl)

# 현재 발표 API 성능 측정 계획

2026-11-05 사용자 요청에 따라 `codex/publication-performance` 브랜치에서 실행한다.
이 계획이 앞선 상세 계획의 Before 커밋 선택을 대체한다. 현재 수정본 `0abdb14e`를 **향후 비동기 개선 전 성능 기준선**으로 사용한다. 중복 방지 수정 전 코드와 비교하는 실험은 이번 범위에 포함하지 않는다.

| 항목 | 이번 실행 |
|---|---|
| 대상 | POST /clubs/{clubUUID}/applicants/notifications |
| 지원자 | 요청당 50명 또는 100명, 절반 PASS·절반 FAIL |
| 부하 | VU 1, 완료 후 다음 신규 발표 요청 |
| 반복 | 지원자 수별 3회, 실행당 30개 요청 |
| 워밍업 | 매 실행 전 신규 데이터로 60초 이상, 통계 제외 |
| FCM | WireMock 200ms 고정 지연 후 200, Google 인증은 고정 가짜 토큰 |
| JVM | Java 17, heap 1GB, Hikari 최대 10개 |
| DB | MySQL 8.0.35, 전용 baseline_load, Docker CPU 2개·메모리 1GB |
| HTTP | 실제 로컬 Tomcat·컨트롤러·서비스·JPA·MySQL, 로그인 필터 대신 테스트 인증 |
| 데이터 | 요청마다 독립된 동아리·지원서·프로필, 모든 지원자 미발표·가짜 토큰 보유 |
| 실패 | HTTP 오류·타임아웃(120초)·완료 문구 불일치 모두 집계 |

재학생 수에서 동시 요청 수를 추정하지 않는다. 50명·100명은 사용자가 선택한 시험 규모이며 운영 통계가 아니다. 이번 VU 1 연속 실행은 일괄 발표 성능 비교용이며 실제 발표 빈도를 주장하지 않는다. 동시 3개·5개 발표 및 장애 주입은 별도 후속 실험이다.

## 검증과 증거

- 소량 검증: 지원자 2명 발표 후 공개 2건·회원 1행·FCM HTTP 요청 2회.
- 정식 실행당 기대값: 50명은 공개 1,500건·회원 750행·FCM 1,500회. 100명은 공개 3,000건·회원 1,500행·FCM 3,000회.
- k6 원시 JSONL, 실행별 p50/p95/p99·RPS·실패율, FCM 요청 내용·응답 기록, 종료 후 DB 상태 보관.
- 100ms 간격 연결 풀 활성·대기, JVM CPU·heap과 연결 획득 시간 집계. CPU·heap은 **앱과 WireMock이 함께 동작하는 JVM**의 값이다.
- 실행별 결과를 별도로 제시한다. p95 평균은 실행별 p95의 평균이며 전체 요청을 합친 p95가 아니다. 표본 30개의 p99는 참고값이다.
- 실제 서버·Firebase·Google 인증·Redis·S3·메일 통합 성능이라고 설명하지 않는다.
- 측정 시점 소스와 실행 도구는 각 결과 폴더의 harness 사본·SHA256·working-tree.patch·manifest로 보존한다.
- 캠페인 시작 시 스키마는 초기화한다. 같은 JVM에서 50명 3회→100명 3회 순서로 실행하며, 요청별 데이터는 새로 생성하지만 이전 실행 행은 캠페인 종료까지 보존된다. 비교 시 같은 초기화·실행 순서를 유지한다.
- 향후 비동기 개선 시 API 응답뿐 아니라 전체 알림 FCM 접수까지 시간·누락·중복도 함께 비교한다.

## 실행 명령

Docker Desktop과 PowerShell 7을 사용한다. 한글 경로의 기존 Gradle 문제를 피하기 위해 W:를 이 저장소에 연결한다. 다른 위치에 이미 연결된 W:는 덮어쓰지 않는다.

```powershell
# W:가 비어 있을 때만 실행
subst W: "$PWD"

# 소량 사전 검증
.\scripts\run-publication-benchmark.ps1 -Smoke

# 정식 실행: 50·100명, VU 1, 조건별 3회
.\scripts\run-publication-benchmark.ps1

# 한 조건만 다시 측정
.\scripts\run-publication-benchmark.ps1 -Counts 100

# 완료 후 결과 폴더를 지정해 요약 (Python 3)
python .\scripts\summarize-publication-benchmark.py .\evidence\week10\performance\<실행폴더>
```

이 스크립트는 전용 DB 스키마를 생성·종료 시 제거한다. 운영·개발 DB는 사용하지 않는다. 로그가 없는 실행 또는 중간 중단된 실행은 정상 성적에 포함하지 않는다.

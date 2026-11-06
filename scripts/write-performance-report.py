"""Create an evidence-linked Korean report only after all six formal runs verify."""
import argparse
import json
from pathlib import Path


def valid_report_rows(rows):
    expected = {f'n{count}-vu1-r{repeat}' for count in (50, 100) for repeat in (1, 2, 3)}
    return (len(rows) == 6 and {row['run_id'] for row in rows} == expected and
            all(row['passed'] and row['minimum_duration_met'] and row['attempts'] == 30 and row['vus'] == 1 and
                row['applicants'] in (50, 100) and row['run_id'].startswith(f"n{row['applicants']}-vu1-r") and
                row['http_failures'] == row['business_failures'] == row['timeouts'] == 0 for row in rows))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('directory', type=Path)
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[1]
    folder = args.directory.resolve()
    rows = json.loads((folder / 'summary.json').read_text(encoding='utf-8'))
    if not valid_report_rows(rows):
        raise SystemExit('Report template requires exactly 50/100 applicants, VU1, 3 runs each, 30 requests each, all verified without failure.')
    relative = folder.relative_to(root).as_posix()
    manifest = json.loads((folder / 'manifest.json').read_text(encoding='utf-8-sig'))
    ready = json.loads((folder / 'ready.json').read_text(encoding='utf-8'))
    lines = [
        '# 발표 API 성능 기준선 측정 결과', '',
        '지원자 50명·100명을 한 번에 발표하는 현재 동기 처리 코드를 실제 로컬 HTTP 요청으로 측정했다. '
        '두 조건을 각각 3회 실행하고, 정식 실행마다 신규 발표 30개 요청의 응답 시간·실패율과 종료 후 DB·FCM 호출 수를 대조했다.', '',
        f'- 측정일: {manifest["startedAt"][:10]} (Asia/Seoul)',
        f'- 브랜치: `{manifest["branch"]}`',
        f'- 애플리케이션 기준 커밋: `{manifest["sourceCommit"]}` — 중복 방지 적용 후, 비동기 개선 전.',
        '- 성능 개선 코드를 구현한 실험이 아니다. 이번 값이 향후 개선과 비교할 기준선이다.',
        '- 실행자: AI(Codex). 사용자는 설명 이해까지만 확인했으며 직접 코드 검토·재실행은 아직 미확인이다.', '',
        '## 측정 조건', '',
        '| 항목 | 조건 |', '|---|---|',
        '| API | POST /clubs/{clubUUID}/applicants/notifications |',
        '| 데이터 | 요청마다 새 동아리와 50명·100명 미발표 지원자, PASS/FAIL 절반, 모두 가짜 FCM 토큰 |',
        '| 부하 | VU 1, 조건별 3회, 실행당 30개 요청, 타임아웃 120초 |',
        '| 워밍업 | 실행 전 별도 데이터로 최소 60초, 정식 통계 제외 |',
        '| 외부 알림 | WireMock HTTP 200, 고정 지연 200ms, Google 인증 비용 제외 |',
        f'| JVM | Java {ready["javaVersion"]}, heap 1GB, Hikari 최대 10개 |',
        '| DB | MySQL 8.0.35, 별도 Docker DB, CPU 2개·메모리 1GB |',
        f'| 도구 | {manifest["k6Version"]} |',
        '| 실행 위치 | 앱·WireMock은 같은 Windows JVM, k6는 같은 Windows 호스트, MySQL은 로컬 Docker |',
        '| 로그 | SQL 상세 로그 비활성화, benchmark 전용 WARN 로그 |', '',
        '한 캠페인은 빈 전용 스키마에서 시작하며 같은 JVM에서 50명 3회 다음 100명 3회 순서로 실행했다. '
        '각 실행의 지원서·동아리는 분리했지만 이전 실행의 행은 캠페인 종료까지 남아 있다. '
        '매 반복마다 DB와 JVM을 완전히 새로 만든 실험은 아니다. 이후 비교에서도 빈 스키마에서 시작해 같은 실행 순서를 유지한다.', '',
        '50명·100명은 사용자가 선택한 시험 규모다. 실제 운영 지원자 통계나 재학생 전체 동시 접속을 뜻하지 않는다. '
        'VU 1의 연속 발표는 비교용 부하이며 실제 이용 빈도라는 주장이 아니다.', '',
        '## 실행별 응답 시간과 실패율', '',
        '| 지원자 | 반복 | 완료/시도 | p50(초) | p95(초) | p99(초) | 실패율 | 요청/초 |',
        '|---|---|---|---|---|---|---|---|',
    ]
    for row in rows:
        lines.append(f'| {row["applicants"]} | {row["run_id"][-1]} | {row["attempts"]-row["http_failures"]}/{row["attempts"]} | '
                     f'{row["p50_ms"]/1000:.3f} | {row["p95_ms"]/1000:.3f} | {row["p99_ms"]/1000:.3f} | '
                     f'{row["failure_percent"]:.2f}% | {row["rps"]:.4f} |')
    lines += ['', '각 백분위수는 실행 하나의 30개 표본에서 산출했다. p99는 참고값이며, 서로 다른 실행의 백분위수를 합친 전체 p95는 계산하지 않았다.', '',
              '## 처리 결과와 DB 연결', '', '| 실행 | 공개 건수 | 회원 행 | FCM 요청 | 활성 연결 최대 | 대기 연결 최대 | 연결 획득 p95(ms) | 검증 |',
              '|---|---|---|---|---|---|---|---|']
    for row in rows:
        lines.append(f'| {row["run_id"]} | {row["published"]} | {row["members"]} | {row["fcm_calls"]} | '
                     f'{row["pool_active_peak"]} | {row["pool_waiting_peak"]} | {row["acquisition_p95_ms"]:.4f} | 일치 |')
    lines += ['', '회원 행은 PASS 지원자 수와 대조했다. FCM 요청은 가짜 서버의 HTTP 접수 수이며 휴대폰 수신 수가 아니다. '
              '입력 지원서 UUID의 실행 간 재사용이 없고, 각 지원자의 가짜 FCM 토큰에 정확히 한 번씩 호출됐는지도 대조했다. '
              'CPU·heap 및 연결 풀은 100ms를 목표 간격으로 표본 수집했으며 실제 시각은 원시 파일에 보존했다. '
              'CPU·heap은 앱과 WireMock이 함께 사용하는 JVM 값이다. 낮은 VU 1 부하에서 대기가 없더라도 동시 부하의 안전성을 보장하지 않는다.', '',
              '## 해석과 남은 작업', '',
              '- 현재 코드는 DB 트랜잭션 안에서 FCM 응답을 한 명씩 기다린다. 고정 지연만 계산해도 50명은 10초, 100명은 20초다. 이 계산은 응답 시간 실측과 구분한다.',
              '- 정상 조건의 실패율과 DB·FCM 건수가 맞는지 검증했다. 장애 복구·전체 알림 완료 시간·동시 3/5개 발표는 이번 실험에서 검증하지 않았다.',
              '- 로그인/JWT 필터 대신 테스트 관리자 인증을 사용했다. 실제 컨트롤러·서비스·트랜잭션·JPA·MySQL·FCM HTTP 전송은 사용했지만 전체 운영 앱 통합 성능은 아니다.',
              '- WireMock 지연은 시험 조건이다. 실제 Firebase 지연·Google 인증·S3·메일·Redis·인터넷 구간 비용을 포함하지 않는다.',
              '- p95 200ms 미만은 향후 목표이며 이번 동기 기준선의 합격 조건이 아니다. 비동기 전환 뒤 API 응답과 알림 전체 FCM 접수까지 시간·누락·중복을 함께 비교해야 한다.',
              '- 로컬 공유 PC에서 측정했으므로 전용 운영 서버 성능으로 일반화하지 않는다. 이후 비교에서는 같은 환경과 시험 조건을 유지한다.', '',
              '## 원시 증거와 재실행', '',
              f'- [실행 환경](../../{relative}/manifest.json)',
              f'- [실제 JVM 정보](../../{relative}/ready.json)',
              f'- [실행별 CSV](../../{relative}/summary.csv)',
              f'- [조건별 실행 p95 요약 CSV](../../{relative}/condition-summary.csv)',
              '- [측정 계획과 실행 명령](performance-test-plan.md)', '']
    # This file is in docs/week10, so evidence links need two parent traversals.
    for row in rows:
        run = row['run_id']
        lines.append(f'- `{run}`: [요청 원시값](../../{relative}/{run}/raw.jsonl), '
                     f'[DB·FCM 검증](../../{relative}/{run}/verification.json), '
                     f'[FCM 대상 대조](../../{relative}/{run}/target-integrity.json), '
                     f'[FCM 호출 내용](../../{relative}/{run}/fcm.json), [자원 시계열](../../{relative}/{run}/resources.jsonl)')
    lines += ['', '소스·도구 사본은 실행 폴더의 `harness/`, 해시는 `harness-sha256.json`, '
              '측정 시작 시 tracked 차이는 `working-tree.patch`에 보관했다. 측정 중 추가한 종료 정리 보완은 실행 중 JVM에 적용되지 않았고 성능 경로는 변경하지 않았다.', '']
    target = root / 'docs/week10/performance-baseline.md'
    target.write_text('\n'.join(lines), encoding='utf-8')
    print(target)


if __name__ == '__main__':
    main()

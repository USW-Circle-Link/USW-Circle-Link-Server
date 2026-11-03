# 10주차 기존 테스트 실행 결과

실행일: 2026-11-03 (Asia/Seoul)
기준 커밋: `794306c2b48c5d3e92a8a8018b2701e32a4c14dd`
브랜치: `refactor/baseline`

## 수행 내용과 결과

| 실행 | 결과 |
|---|---|
| `gradlew.bat clean test --console=plain` | 컴파일 성공, 테스트 클래스 로딩 단계에서 ClassNotFoundException |
| `gradlew.bat bootJar --console=plain` | 성공, 12초 |
| `W:\gradlew.bat -p W:\ test --rerun-tasks --console=plain` | 성공, 25초, 총 5개 통과·실패 0·건너뜀 0 |

통과한 테스트: ClubLeaderService 접근 권한 4개, S3 업로드 Mockito 테스트 1개.
S3 테스트는 mock 사용이며 실제 S3 업로드 성공을 뜻하지 않는다.

## 환경과 문제 해결

- 기본 Java: Temurin 21.0.5. Gradle toolchain의 테스트 실행 JDK는 캐시된 17.0.12.
- Gradle Wrapper: 8.8.
- 테스트 class 파일이 생성됐지만 한글·공백을 포함한 원래 경로에서 클래스 로딩 실패.
- `subst W:`로 동일 저장소를 영문 드라이브 경로에 연결한 뒤 재실행하니 통과했다. 경로가 실행에 영향을 준 것으로 관측했으며 정확한 내부 원인은 추가 조사 대상이다.
- 애플리케이션·빌드·테스트 소스는 수정하지 않았다.
- Docker CLI는 설치됐지만 Docker Desktop Linux 엔진 파이프에 연결할 수 없었다.

재현 방법 (W 드라이브가 비어 있을 때):

```powershell
subst W: 'C:\Users\izim5\OneDrive\문서\ChatGPT\동구라미 2\USW-Circle-Link-Server'
W:\gradlew.bat -p W:\ test --rerun-tasks --console=plain
```

W:는 같은 폴더의 별칭이며 파일 복사본이 아니다. 이번 실행 후 재현을 위해 연결을 유지했다.

## 증거와 검증 범위

- `evidence/week10/test-run/gradle-test.log`: 최초 실패 로그.
- `evidence/week10/test-run/initial-test-results/`: 최초 실행 XML·바이너리 결과.
- `evidence/week10/test-run/gradle-bootJar.log`: JAR 빌드 결과.
- `evidence/week10/test-run/gradle-test-ascii-path.log`: 재실행 성공 로그.
- `evidence/week10/test-run/passed-test-results/`: 성공 실행 결과.
- `build/reports/tests/test/index.html`: 성공 실행 HTML 보고서 (다음 실행으로 덮어쓸 수 있음).

작성한 PUB/STATUS/LIST/APPLY·ERR·RACE 상세 시나리오는 테스트 코드 미구현으로 이번 실행에 포함되지 않았다.
HTTP API·MySQL 정합성·FCM 장애 복구·동시성·부하 성능이 검증됐다고 해석하지 않는다.

## 주간실습보고서 기입용 기록

수행: 기존 코드 기준 빌드·테스트 실행, 실패 원인 조사, 영문 경로 별칭으로 재실행, 원시 로그 보존.
성과: 실행 JAR 생성 성공, 활성화된 기존 단위 테스트 5개 통과.
문제 및 조치: 원래 경로에서 테스트 클래스 로딩 실패 → 동일 저장소를 W:로 연결해 통과 확인.
미완료: 상세 재현 시나리오 구현·실행, Docker 엔진 기동, FCM 대역 구성, MySQL 기반 검증, Before 성능 측정.
보고서 DOCX 양식에는 아직 옮기지 않았다. 이 기록을 반영할 때 원본 양식과 실행 증거를 함께 사용한다.

# 🔀 Develop → Main 브랜치 머지 실행 계획

**작성일**: 2025-11-05  
**대상 브랜치**: `develop` → `main`  
**공통 조상 커밋**: `ddd4ece` (PR #618 이전)

---

## 📊 현재 상황 분석

### 브랜치 상태
- **main 최신**: `59ebd69` - "Merge pull request #624" (이벤트 인증 API 제거)
- **develop 최신**: `e2948f1` - "Merge pull request #553" (AWS 설정 변경)

### 변경 통계
```
총 커밋 수: develop 49개 | main 10개
변경 파일: 96개
추가: +1,731줄 | 삭제: -893줄
```

### 🔥 주요 변경사항 (develop)

#### 1. 인프라 변경 ⚙️
- ✅ **Flyway 마이그레이션 도입** (V1~V3)
  - `V1__Init_db.sql`: 초기 DB 스키마
  - `V2__Add_college_and_major_tables.sql`: College/Major 테이블 추가
  - `V3__Insert_college_and_major.sql`: 초기 데이터
- ✅ Docker Compose 개선 (Redis, MySQL 설정)
- ✅ AWS IAM 프로파일 설정 변경

#### 2. Redis 전환 🔴
- ✅ `EmailToken` → Redis 기반 토큰으로 전환
- ✅ `SignupToken` 서비스 추가 (Redis)
- ✅ `AuthToken`, `WithdrawalToken` Redis 적용
- ✅ Redis 설정 강화 (`RedisConfig`)

#### 3. 도메인 재구조화 📦
- ✅ **Profile 패키지 분리**
  - `profile/profile/` (기존 Profile)
  - `profile/major/` (새로운 Major/College)
- ✅ Major/College 엔티티 및 API 추가

#### 4. 보안 및 검증 강화 🔒
- ✅ 입력값 검증 로직 강화 (비밀번호, 프로필)
- ✅ Security Context 인터페이스 분리 (`AuthContext`)
- ✅ UUID Provider 추상화
- ✅ IP 유틸 추가 (`IpUtil`)
- ✅ JWT 필터 개선

#### 5. 서비스 리팩토링 🔧
- ✅ `ClubService` 대규모 리팩토링
- ✅ `EmailTokenService` Redis 전환
- ✅ DTO 검증 조건 개선

### ⚠️ 충돌 예상 파일 (우선순위)

| 우선순위 | 파일 | 이유 | 해결 전략 |
|---------|------|------|----------|
| 🔴 HIGH | `AplictService.java` | 양쪽 수정 | main 로직 + develop 검증 병합 |
| 🔴 HIGH | `ClubService.java` | develop 대규모 리팩토링 | develop 우선, main 추가 기능 병합 |
| 🟡 MED | `build.gradle` | Flyway 의존성 추가 | 양쪽 의존성 모두 포함 |
| 🟡 MED | `SecurityConfig.java` | 경로 변경 | develop 우선 |
| 🟢 LOW | `docker-compose.yml` | 설정 변경 | develop 우선 |

---

## 🎯 머지 실행 계획 (5단계)

### ✅ Phase 1: 사전 준비 (30분)

#### 1-1. 백업 생성
```bash
# 현재 상태 태그
git tag backup-before-merge-$(date +%Y%m%d-%H%M%S)
git push origin --tags

# 백업 브랜치
git branch backup-main origin/main
git branch backup-develop origin/develop
```

#### 1-2. 최신 상태 동기화
```bash
git fetch --all --prune
git checkout main
git pull origin main
```

#### 1-3. 팀원 공지
```
📢 공지 사항
- develop → main 머지 작업 시작
- 예상 소요: 2-3시간
- 이 시간 동안 main, develop 브랜치 작업 중지 요청
```

---

### ✅ Phase 2: 테스트 머지 실행 (1시간)

#### 2-1. 머지 테스트 브랜치 생성
```bash
# 테스트 브랜치 생성
git checkout -b merge/develop-to-main origin/main

# develop 머지 시도 (커밋하지 않음)
git merge origin/develop --no-ff --no-commit
```

#### 2-2. 충돌 확인
```bash
# 충돌 파일 목록
git status | grep "both modified"

# 예상 충돌 파일
# - src/main/java/com/USWCicrcleLink/server/aplict/service/AplictService.java
# - src/main/java/com/USWCicrcleLink/server/club/club/service/ClubService.java
# - build.gradle
```

#### 2-3. 충돌 해결 가이드

**A. AplictService.java 충돌 해결**
```bash
# 파일 확인
git diff HEAD:src/main/java/com/USWCicrcleLink/server/aplict/service/AplictService.java origin/develop:src/main/java/com/USWCicrcleLink/server/aplict/service/AplictService.java

# 해결 방향:
# 1. main의 최신 비즈니스 로직 유지
# 2. develop의 검증 강화 코드 병합
# 3. import 문 정리
```

**B. ClubService.java 충돌 해결**
```bash
# 해결 방향:
# develop의 리팩토링 버전을 기본으로 사용
# main의 추가 기능이 있다면 병합
```

**C. build.gradle 충돌 해결**
```gradle
// 양쪽 의존성 모두 포함
dependencies {
    // develop의 Flyway
    implementation 'org.flywaydb:flyway-core'
    implementation 'org.flywaydb:flyway-mysql'
    
    // 기존 의존성들 유지
    // ...
}
```

#### 2-4. 충돌 해결 후 스테이징
```bash
# 해결된 파일 추가
git add <resolved-file>

# 모든 충돌 해결 확인
git status

# 충돌이 모두 해결되면
git commit -m "Merge branch 'develop' into main

주요 변경사항:
- Flyway 마이그레이션 시스템 도입 (V1~V3)
- Redis 기반 인증 토큰 시스템 전환
- Profile 도메인 재구조화 (Major/College 추가)
- 입력값 검증 로직 강화
- ClubService 리팩토링
- Security Context 인터페이스 분리
- AWS IAM 설정 업데이트

Breaking Changes:
- EmailToken 엔티티 제거 (Redis 전환)
- Profile 패키지 구조 변경
- 이벤트 인증 DELETE API 제거 (main에서 이미 적용됨)

Conflicts Resolved:
- AplictService.java: main 로직 + develop 검증 병합
- ClubService.java: develop 리팩토링 우선
- build.gradle: Flyway 의존성 추가
- SecurityConfig.java: develop 경로 설정 적용
"
```

---

### ✅ Phase 3: 빌드 및 테스트 (1시간)

#### 3-1. 로컬 빌드
```bash
# 클린 빌드
./gradlew clean build

# 빌드 실패 시 로그 확인
./gradlew build --stacktrace
```

#### 3-2. 테스트 실행
```bash
# 단위 테스트
./gradlew test

# 테스트 결과 확인
cat build/reports/tests/test/index.html
```

#### 3-3. 애플리케이션 실행 테스트
```bash
# 로컬 프로파일로 실행
./gradlew bootRun --args='--spring.profiles.active=local'

# 헬스 체크
curl http://localhost:8080/health
```

#### 3-4. 주요 기능 수동 테스트 체크리스트
```
□ Flyway 마이그레이션 확인 (로그에서 V1~V3 실행 확인)
□ 회원가입 (Redis 토큰 확인)
□ 이메일 인증 (Redis 저장 확인)
□ 로그인/로그아웃
□ 비밀번호 찾기 (Redis 토큰)
□ 전공 선택 API (/majors)
□ 동아리 조회
□ 동아리 지원
□ Swagger 문서 확인 (http://localhost:8080/swagger-ui.html)
```

---

### ✅ Phase 4: Pull Request 및 리뷰 (4시간 - 1일)

#### 4-1. PR 생성
```bash
# 머지 브랜치 푸시
git push origin merge/develop-to-main
```

#### 4-2. PR 템플릿
```markdown
## 🔀 [MERGE] Develop → Main (2025.11.05)

### 📝 요약
develop 브랜치의 49개 커밋을 main에 병합합니다.

### 🎯 주요 변경사항

#### 1. Flyway 마이그레이션 도입 ✅
- DB 스키마 버전 관리 시스템 적용
- V1: 초기 스키마, V2: College/Major 테이블, V3: 초기 데이터

#### 2. Redis 전환 ✅
- 이메일 인증 토큰 → Redis
- 비밀번호 찾기 토큰 → Redis
- 회원 탈퇴 토큰 → Redis

#### 3. 도메인 재구조화 ✅
- Profile 패키지 분리 (profile, major)
- Major/College 엔티티 및 API 추가

#### 4. 보안 강화 ✅
- 입력값 검증 강화
- Security Context 인터페이스화
- JWT 필터 개선

#### 5. 코드 개선 ✅
- ClubService 리팩토링
- DTO 검증 조건 개선

### ⚠️ Breaking Changes

#### 1. EmailToken 테이블 제거
**Before**: DB 기반 `EMAIL_TOKEN` 테이블
**After**: Redis 기반 토큰 저장

**마이그레이션 필요 없음**: Flyway가 자동으로 스키마 관리

#### 2. Profile 패키지 구조 변경
**Before**: `com.USWCicrcleLink.server.profile.*`
**After**: 
- `com.USWCicrcleLink.server.profile.profile.*`
- `com.USWCicrcleLink.server.profile.major.*`

**영향**: import 경로 변경 (이미 develop에서 처리됨)

### 🧪 테스트 완료

- [x] 로컬 빌드 성공
- [x] 단위 테스트 통과 (x개)
- [x] Flyway 마이그레이션 확인
- [x] Redis 연결 확인
- [x] 주요 기능 수동 테스트
- [x] Swagger 문서 확인

### 🔍 충돌 해결 내역

1. **AplictService.java**: main의 비즈니스 로직 + develop의 검증 로직 병합
2. **ClubService.java**: develop의 리팩토링 버전 우선 적용
3. **build.gradle**: Flyway 의존성 추가
4. **SecurityConfig.java**: develop의 경로 설정 적용

### 📊 변경 통계
- 96 files changed
- 1,731 insertions(+)
- 893 deletions(-)

### 🚀 배포 계획
1. PR 승인 후 main 머지
2. 스테이징 환경 배포 및 검증
3. 운영 환경 배포

### 📋 리뷰 체크리스트
- [ ] 충돌 해결이 올바른가?
- [ ] Breaking Changes가 문서화되었는가?
- [ ] 테스트가 통과하는가?
- [ ] Flyway 마이그레이션이 안전한가?
- [ ] Redis 설정이 올바른가?

### 👥 리뷰어
@team-lead @backend-dev-1 @backend-dev-2

---

**긴급 롤백 방법**
```bash
git reset --hard backup-main
git push origin main --force-with-lease
```
```

#### 4-3. 리뷰 진행
```
□ 충돌 해결 코드 리뷰
□ Flyway 마이그레이션 SQL 검토
□ Redis 설정 확인
□ Breaking Changes 영향도 분석
□ 최소 2명 이상 승인
```

---

### ✅ Phase 5: 배포 (4시간)

#### 5-1. PR 승인 후 main 머지
```bash
# GitHub에서 PR 승인 후
# Squash and merge 또는 Merge commit 선택
# (Merge commit 권장 - 히스토리 보존)

# 또는 로컬에서 직접 머지
git checkout main
git pull origin main
git merge --no-ff merge/develop-to-main
git push origin main

# 릴리스 태그 생성
git tag v1.x.0-$(date +%Y%m%d)
git push origin --tags
```

#### 5-2. 스테이징 배포
```bash
# 스테이징 서버에서 실행
git checkout staging
git pull origin main

# Docker Compose로 실행 (Redis 포함)
docker-compose up -d

# 로그 확인
docker-compose logs -f

# Flyway 마이그레이션 확인
grep "Migrating schema" logs/application.log
```

#### 5-3. 스테이징 검증 체크리스트
```
□ 서버 정상 기동 (30초 이내)
□ DB 연결 성공
□ Redis 연결 성공
□ Flyway 마이그레이션 성공 (V1~V3)
□ Swagger API 문서 접근 가능
□ 회원가입 플로우 (이메일 인증)
□ 로그인/로그아웃
□ 전공 선택 API
□ 동아리 조회/지원
□ 관리자 기능
□ 에러 로그 없음
```

#### 5-4. 운영 배포
```bash
# 운영 서버 배포 전 체크리스트
□ 스테이징 검증 완료
□ 데이터베이스 백업 완료
□ Redis 설정 확인
□ 모니터링 대시보드 준비
□ 팀원 대기 상태

# 무중단 배포 스크립트 실행
cd /home/ubuntu/app
./scripts/run_new_was.sh

# 헬스 체크
./scripts/health.sh

# 트래픽 전환
./scripts/switch.sh
```

#### 5-5. 배포 후 모니터링 (1시간 집중)
```
□ 서버 응답 시간
□ 에러율 (< 0.1%)
□ Redis 메모리 사용량
□ DB 커넥션 풀
□ Flyway 마이그레이션 상태
□ API 응답률
□ 사용자 피드백
```

---

## 🚨 트러블슈팅

### 문제 1: Flyway 마이그레이션 실패
```bash
# 원인: 기존 스키마와 충돌
# 해결: Flyway baseline 설정
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0
```

### 문제 2: Redis 연결 실패
```bash
# 원인: Redis 서버 미실행
# 해결:
docker-compose up -d redis

# 또는
redis-server
```

### 문제 3: Profile 패키지 import 에러
```bash
# 원인: 패키지 구조 변경
# 해결: IDE에서 자동 import 정리
# IntelliJ: Ctrl+Alt+O
```

### 문제 4: 빌드 실패 (Flyway 의존성)
```bash
# 원인: 의존성 충돌
# 해결: build.gradle 확인
./gradlew dependencies --configuration compileClasspath
```

---

## 🔄 롤백 계획

### 롤백 트리거 조건
- 🔴 치명적 버그 (회원가입/로그인 불가)
- 🔴 Flyway 마이그레이션 실패
- 🔴 Redis 연결 실패로 서비스 중단
- 🔴 에러율 10% 이상
- 🔴 다운타임 5분 이상

### 긴급 롤백 절차 (5분 이내)
```bash
# 1. 이전 버전 복구
git checkout main
git reset --hard backup-main
git push origin main --force-with-lease

# 2. 서버 재배포
./scripts/run_new_was.sh

# 3. 상태 확인
./scripts/health.sh

# 4. 팀 공지
```

### Flyway 롤백 (필요시)
```sql
-- 마이그레이션 히스토리 확인
SELECT * FROM flyway_schema_history;

-- 수동 롤백 (필요한 경우만)
-- V3 롤백: DELETE FROM major; DELETE FROM college;
-- V2 롤백: DROP TABLE major; DROP TABLE college;
```

---

## 📋 최종 체크리스트

### 머지 전
- [ ] 백업 생성 완료
- [ ] 팀원 공지 완료
- [ ] 테스트 머지 성공
- [ ] 충돌 해결 완료
- [ ] 로컬 빌드 성공
- [ ] 테스트 통과

### 머지 후
- [ ] PR 생성 및 승인
- [ ] main 브랜치 머지 완료
- [ ] 릴리스 태그 생성

### 배포 전
- [ ] 스테이징 검증 완료
- [ ] DB 백업 완료
- [ ] 롤백 스크립트 준비

### 배포 후
- [ ] 운영 배포 완료
- [ ] 헬스 체크 통과
- [ ] 1시간 모니터링 완료
- [ ] 24시간 안정화 확인

---

## 📞 연락처

- **작업 책임자**: [이름]
- **기술 리드**: [이름]
- **DevOps**: [이름]
- **긴급 연락처**: [전화번호]

---

## ⏰ 예상 일정

| 단계 | 소요 시간 | 시작 시간 | 종료 시간 |
|------|----------|-----------|-----------|
| Phase 1: 사전 준비 | 30분 | - | - |
| Phase 2: 테스트 머지 | 1시간 | - | - |
| Phase 3: 빌드/테스트 | 1시간 | - | - |
| Phase 4: PR 리뷰 | 4시간~1일 | - | - |
| Phase 5: 배포 | 4시간 | - | - |

**총 예상 기간**: 1-2일 (실제 작업: 6-7시간)

---

## 📚 참고 문서

- [Flyway 공식 문서](https://flywaydb.org/documentation/)
- [Spring Boot Redis 설정](https://spring.io/projects/spring-data-redis)
- [무중단 배포 가이드](./scripts/README.md)

---

**작성자**: Cursor AI  
**최종 업데이트**: 2025-11-05  
**버전**: 1.0

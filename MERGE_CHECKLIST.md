# ✅ Develop → Main 머지 체크리스트

**작성일**: 2025-11-05  
**목표**: develop 브랜치를 main 브랜치에 안전하게 머지

---

## 🎯 빠른 시작

```bash
# 1단계: 사전 준비
./merge-develop-to-main.sh prepare

# 2단계: 머지 실행
./merge-develop-to-main.sh merge

# 3단계: 테스트
./merge-develop-to-main.sh test

# 4단계: PR 생성 및 배포
git push origin merge/develop-to-main
```

---

## 📋 Phase 1: 사전 준비 (30분)

### 팀 커뮤니케이션
- [ ] 팀원에게 머지 작업 공지 (최소 1시간 전)
- [ ] develop, main 브랜치 작업 중지 요청
- [ ] 긴급 연락망 확인

### 백업
- [ ] 백업 태그 생성 확인
- [ ] 백업 브랜치 생성 확인
- [ ] 로컬 환경 동기화 완료

### 실행
```bash
./merge-develop-to-main.sh prepare
```

---

## 📋 Phase 2: 머지 실행 (1시간)

### 머지 전 확인
- [ ] main 브랜치 최신 상태
- [ ] develop 브랜치 최신 상태
- [ ] 작업 디렉토리 clean 상태

### 머지 실행
```bash
./merge-develop-to-main.sh merge
```

### 충돌 발생 시
- [ ] 충돌 파일 목록 확인: `git status`
- [ ] 각 파일 충돌 해결
  - [ ] `AplictService.java` - main 로직 + develop 검증
  - [ ] `ClubService.java` - develop 리팩토링 우선
  - [ ] `build.gradle` - 양쪽 의존성 병합
  - [ ] `SecurityConfig.java` - develop 설정 우선
- [ ] 해결된 파일 스테이징: `git add <file>`
- [ ] 머지 커밋: `git commit`

---

## 📋 Phase 3: 테스트 (1시간)

### 빌드 및 테스트
```bash
./merge-develop-to-main.sh test
```

### 테스트 항목
- [ ] 빌드 성공
- [ ] 단위 테스트 통과
- [ ] Flyway 마이그레이션 확인
  ```bash
  # 로그에서 확인
  grep "Migrating schema" logs/spring.log
  ```

### 로컬 실행 테스트
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

- [ ] 애플리케이션 정상 기동
- [ ] Swagger 접근: http://localhost:8080/swagger-ui.html
- [ ] Redis 연결 확인
- [ ] DB 연결 확인

### 기능 테스트
- [ ] 회원가입 (Redis 토큰)
- [ ] 이메일 인증
- [ ] 로그인/로그아웃
- [ ] 비밀번호 찾기
- [ ] 전공 선택 API (`/majors`)
- [ ] 동아리 조회
- [ ] 동아리 지원

---

## 📋 Phase 4: Pull Request (4시간 - 1일)

### PR 생성
```bash
# 브랜치 푸시
git push origin merge/develop-to-main
```

- [ ] GitHub에서 PR 생성
- [ ] PR 템플릿 작성 (MERGE_PLAN.md 참조)
- [ ] 변경사항 요약 작성
- [ ] Breaking Changes 명시
- [ ] 스크린샷/로그 첨부

### 코드 리뷰
- [ ] 충돌 해결 코드 리뷰
- [ ] Flyway SQL 스크립트 검토
- [ ] Redis 설정 확인
- [ ] Breaking Changes 영향도 분석
- [ ] 최소 2명 승인 받기

---

## 📋 Phase 5: 배포 (4시간)

### 배포 전 체크
- [ ] PR 승인 완료
- [ ] 스테이징 환경 준비
- [ ] 데이터베이스 백업
- [ ] Redis 설정 확인
- [ ] 모니터링 도구 준비

### 스테이징 배포
```bash
# 스테이징 서버에서
git checkout staging
git merge merge/develop-to-main
docker-compose up -d
```

### 스테이징 검증
- [ ] 서버 정상 기동 (30초 이내)
- [ ] DB 연결 성공
- [ ] Redis 연결 성공
- [ ] Flyway 마이그레이션 성공
  ```bash
  # DB에서 확인
  SELECT * FROM flyway_schema_history;
  ```
- [ ] Swagger 문서 접근
- [ ] 주요 API 동작 확인
- [ ] 에러 로그 없음

### 운영 배포
```bash
# main 브랜치 머지
git checkout main
git merge --no-ff merge/develop-to-main
git push origin main

# 릴리스 태그
git tag v1.x.0-$(date +%Y%m%d)
git push origin --tags

# 무중단 배포
cd scripts
./run_new_was.sh
./health.sh
./switch.sh
```

### 배포 후 모니터링 (1시간 집중)
- [ ] 서버 응답 시간 정상
- [ ] 에러율 < 0.1%
- [ ] Redis 메모리 사용량 정상
- [ ] DB 커넥션 풀 정상
- [ ] API 응답률 > 99%
- [ ] 사용자 피드백 없음

---

## 🚨 긴급 롤백

### 롤백 조건
- 🔴 치명적 버그 발견
- 🔴 서비스 다운타임 5분 이상
- 🔴 에러율 10% 이상
- 🔴 데이터 손실

### 롤백 실행
```bash
# 자동 롤백 스크립트
./merge-develop-to-main.sh rollback

# 또는 수동 롤백
git checkout main
git reset --hard backup-main-[TIMESTAMP]
git push origin main --force-with-lease
```

### 롤백 후
- [ ] 서버 재배포
- [ ] 헬스 체크
- [ ] 팀 공지
- [ ] 문제 원인 분석
- [ ] 재시도 계획 수립

---

## 📊 최종 확인

### 머지 완료 체크
- [ ] main 브랜치에 머지 완료
- [ ] 릴리스 태그 생성
- [ ] 운영 배포 완료
- [ ] 모니터링 정상
- [ ] 백업 보관 완료

### 정리
- [ ] 머지 브랜치 삭제 (선택)
  ```bash
  git branch -d merge/develop-to-main
  git push origin --delete merge/develop-to-main
  ```
- [ ] 문서 업데이트
- [ ] 팀에 완료 공지
- [ ] 회고 미팅 일정 잡기

---

## 📈 주요 변경사항 요약

### ✅ 추가된 기능
- Flyway 마이그레이션 시스템
- Redis 기반 인증 토큰
- Major/College 도메인 및 API
- 입력값 검증 강화
- Security Context 인터페이스

### ⚠️ Breaking Changes
- EmailToken 테이블 제거 (Redis 전환)
- Profile 패키지 구조 변경
- 이벤트 인증 DELETE API 제거 (main에 이미 적용)

### 📊 변경 통계
- 커밋: 49개
- 파일: 96개
- 추가: +1,731줄
- 삭제: -893줄

---

## 🔗 관련 문서

- [상세 머지 계획](MERGE_PLAN.md)
- [자동화 스크립트](merge-develop-to-main.sh)
- [프로젝트 구조 분석](README.md)

---

## 📞 긴급 연락처

- **개발 팀장**: [이름] - [전화번호]
- **DevOps**: [이름] - [전화번호]
- **인프라 담당**: [이름] - [전화번호]

---

**마지막 업데이트**: 2025-11-05  
**작성자**: Cursor AI

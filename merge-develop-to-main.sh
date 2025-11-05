#!/bin/bash

# Develop → Main 브랜치 머지 자동화 스크립트
# 사용법: ./merge-develop-to-main.sh [phase]
# Phase: prepare | merge | test | rollback

set -e  # 에러 발생 시 스크립트 중단

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 로깅 함수
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 현재 날짜 및 시간
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
BACKUP_TAG="backup-before-merge-${TIMESTAMP}"
MERGE_BRANCH="merge/develop-to-main"

# Phase 1: 사전 준비
phase_prepare() {
    log_info "Phase 1: 사전 준비 시작..."
    
    # 1. 백업 태그 생성
    log_info "백업 태그 생성: ${BACKUP_TAG}"
    git tag "${BACKUP_TAG}"
    git push origin --tags
    log_success "백업 태그 생성 완료"
    
    # 2. 백업 브랜치 생성
    log_info "백업 브랜치 생성..."
    git branch "backup-main-${TIMESTAMP}" origin/main || log_warning "백업 브랜치 이미 존재"
    git branch "backup-develop-${TIMESTAMP}" origin/develop || log_warning "백업 브랜치 이미 존재"
    log_success "백업 브랜치 생성 완료"
    
    # 3. 최신 상태 동기화
    log_info "원격 브랜치 동기화..."
    git fetch --all --prune
    log_success "동기화 완료"
    
    # 4. main 브랜치로 이동
    log_info "main 브랜치로 전환..."
    git checkout main
    git pull origin main
    log_success "main 브랜치 최신화 완료"
    
    # 5. 변경사항 통계 출력
    log_info "변경사항 분석..."
    echo ""
    echo "========================================="
    echo "📊 변경 통계"
    echo "========================================="
    echo "Commits in develop not in main:"
    git log origin/main..origin/develop --oneline | wc -l
    echo ""
    echo "Files changed:"
    git diff --stat origin/main...origin/develop | tail -1
    echo "========================================="
    echo ""
    
    log_success "Phase 1 완료! 다음 명령어로 머지를 시작하세요:"
    echo "  ./merge-develop-to-main.sh merge"
}

# Phase 2: 테스트 머지
phase_merge() {
    log_info "Phase 2: 테스트 머지 시작..."
    
    # 1. 기존 머지 브랜치 확인
    if git rev-parse --verify "${MERGE_BRANCH}" 2>/dev/null; then
        log_warning "머지 브랜치가 이미 존재합니다. 삭제하시겠습니까? (y/N)"
        read -r response
        if [[ "$response" =~ ^[Yy]$ ]]; then
            git branch -D "${MERGE_BRANCH}"
            log_success "기존 브랜치 삭제 완료"
        else
            log_error "머지 브랜치를 먼저 삭제하거나 다른 이름을 사용하세요."
            exit 1
        fi
    fi
    
    # 2. 머지 브랜치 생성
    log_info "머지 브랜치 생성: ${MERGE_BRANCH}"
    git checkout -b "${MERGE_BRANCH}" origin/main
    log_success "브랜치 생성 완료"
    
    # 3. develop 머지 시도
    log_info "develop 브랜치 머지 시도..."
    if git merge origin/develop --no-ff --no-commit; then
        log_success "머지 성공 (충돌 없음)!"
        
        # 자동 커밋
        log_info "머지 커밋 생성 중..."
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

Timestamp: ${TIMESTAMP}"
        
        log_success "머지 커밋 생성 완료!"
    else
        log_warning "⚠️  충돌이 발생했습니다!"
        echo ""
        echo "========================================="
        echo "📋 충돌 파일 목록"
        echo "========================================="
        git status | grep "both modified"
        echo "========================================="
        echo ""
        log_info "다음 단계:"
        echo "  1. 충돌을 수동으로 해결하세요"
        echo "  2. 해결된 파일을 스테이징: git add <file>"
        echo "  3. 모든 충돌 해결 후: git commit"
        echo "  4. 테스트 실행: ./merge-develop-to-main.sh test"
        exit 1
    fi
    
    log_success "Phase 2 완료! 다음 명령어로 테스트를 실행하세요:"
    echo "  ./merge-develop-to-main.sh test"
}

# Phase 3: 빌드 및 테스트
phase_test() {
    log_info "Phase 3: 빌드 및 테스트 시작..."
    
    # 1. 현재 브랜치 확인
    current_branch=$(git branch --show-current)
    if [ "$current_branch" != "$MERGE_BRANCH" ]; then
        log_warning "현재 브랜치가 ${MERGE_BRANCH}가 아닙니다."
        log_info "${MERGE_BRANCH} 브랜치로 전환 중..."
        git checkout "$MERGE_BRANCH"
    fi
    
    # 2. 클린 빌드
    log_info "클린 빌드 시작..."
    if ./gradlew clean build; then
        log_success "빌드 성공!"
    else
        log_error "빌드 실패. 로그를 확인하세요."
        exit 1
    fi
    
    # 3. 테스트 실행
    log_info "테스트 실행 중..."
    if ./gradlew test; then
        log_success "모든 테스트 통과!"
    else
        log_error "테스트 실패. build/reports/tests/test/index.html을 확인하세요."
        exit 1
    fi
    
    # 4. 테스트 결과 요약
    echo ""
    echo "========================================="
    echo "✅ 테스트 결과"
    echo "========================================="
    echo "빌드: 성공"
    echo "테스트: 통과"
    echo "========================================="
    echo ""
    
    log_success "Phase 3 완료! 다음 단계:"
    echo "  1. 브랜치 푸시: git push origin ${MERGE_BRANCH}"
    echo "  2. GitHub에서 Pull Request 생성"
    echo "  3. 팀원 리뷰 요청"
}

# Phase 4: 롤백
phase_rollback() {
    log_warning "⚠️  롤백을 시작합니다..."
    
    # 확인 메시지
    echo ""
    log_warning "정말로 롤백하시겠습니까? 이 작업은 되돌릴 수 없습니다!"
    echo "계속하려면 'ROLLBACK'을 입력하세요:"
    read -r confirmation
    
    if [ "$confirmation" != "ROLLBACK" ]; then
        log_info "롤백이 취소되었습니다."
        exit 0
    fi
    
    # 백업에서 복구
    log_info "백업 태그에서 복구 중..."
    git checkout main
    git reset --hard "${BACKUP_TAG}"
    
    log_warning "원격 저장소에 강제 푸시하시겠습니까? (y/N)"
    read -r push_response
    
    if [[ "$push_response" =~ ^[Yy]$ ]]; then
        git push origin main --force-with-lease
        log_success "롤백 완료 및 원격 저장소 업데이트 완료"
    else
        log_info "로컬만 롤백되었습니다. 원격 저장소는 변경되지 않았습니다."
    fi
    
    # 머지 브랜치 삭제
    if git rev-parse --verify "${MERGE_BRANCH}" 2>/dev/null; then
        git branch -D "${MERGE_BRANCH}"
        log_info "머지 브랜치 삭제 완료"
    fi
}

# 상태 확인
phase_status() {
    echo "========================================="
    echo "📊 현재 머지 상태"
    echo "========================================="
    echo ""
    
    echo "현재 브랜치: $(git branch --show-current)"
    echo ""
    
    echo "백업 태그:"
    git tag | grep "backup-before-merge" | tail -5 || echo "  (없음)"
    echo ""
    
    echo "머지 브랜치 존재 여부:"
    if git rev-parse --verify "${MERGE_BRANCH}" 2>/dev/null; then
        echo "  ✅ ${MERGE_BRANCH} 존재"
    else
        echo "  ❌ ${MERGE_BRANCH} 없음"
    fi
    echo ""
    
    echo "main과 develop 차이:"
    echo "  Commits ahead: $(git rev-list --count origin/develop ^origin/main)"
    echo "  Files changed: $(git diff --name-only origin/main...origin/develop | wc -l)"
    echo ""
    
    echo "========================================="
}

# 도움말
show_help() {
    cat << EOF
Develop → Main 브랜치 머지 자동화 스크립트

사용법: $0 [command]

Commands:
  prepare   사전 준비 (백업, 동기화)
  merge     테스트 머지 실행
  test      빌드 및 테스트 실행
  status    현재 머지 상태 확인
  rollback  롤백 (백업으로 복구)
  help      이 도움말 표시

Example:
  $0 prepare   # 1. 사전 준비
  $0 merge     # 2. 머지 실행
  $0 test      # 3. 테스트 실행
  $0 rollback  # 롤백 (필요시)

상세 가이드: MERGE_PLAN.md 참조
EOF
}

# 메인 실행
main() {
    case "${1:-help}" in
        prepare)
            phase_prepare
            ;;
        merge)
            phase_merge
            ;;
        test)
            phase_test
            ;;
        rollback)
            phase_rollback
            ;;
        status)
            phase_status
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            log_error "알 수 없는 명령어: $1"
            echo ""
            show_help
            exit 1
            ;;
    esac
}

# 스크립트 실행
main "$@"

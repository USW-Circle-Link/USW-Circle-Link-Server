# 동시 발표 중복 방지 구현 계획

**Goal:** 사용자가 이해하고 선택한 락·DB 제약을 적용해 중복 발표와 회원 중복 INSERT를 막는다.
**Spec:** ../specs/2026-11-05-duplicate-publication-design.md
**Tech Stack:** Spring Data JPA, MySQL 8.0.35, JUnit, WireMock.

1. 테스트 먼저: 동시 발표·재요청·본문 중복은 회원/알림 1건을 기대하도록 변경하고, DB 중복 INSERT 거절 테스트를 추가한다. 기존 코드에서 실패하는지 확인한다.
2. AplictRepository에 잠금 조회를 추가하고, ClubLeaderService에서 잠금 후 공개 상태를 검사한다. 다건 잠금은 일관된 UUID 순서를 사용한다.
3. ClubMembers에 Unique 제약 추가. 운영 DB용 중복 사전 조회와 ALTER SQL 및 적용 안내를 제공한다.
4. 반대 순서 다건 요청·DB 제약·전체 회귀 테스트를 실행하고 이전 증거와 분리해 기록한다.
5. 변경분만 검토·스테이징 후 `fix: 락과 DB 제약으로 동시 발표 중복 처리 방지` 메시지로 커밋한다. 원격 push는 요청 범위에 포함하지 않는다.

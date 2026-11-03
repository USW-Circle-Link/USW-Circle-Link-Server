# 10주차 — 대상 API 및 시나리오 명세

- 작성일: 2026-11-03
- 브랜치: `refactor/baseline`
- 기준 커밋: `794306c2b48c5d3e92a8a8018b2701e32a4c14dd`
- 범위: 기존 Endpoint·Request/Response DTO·권한·현재 동작 확정. 명세 작성 단계에서는 애플리케이션 코드를 변경하지 않았다.
- 최초 작성 상태: 정적 코드 확인 완료. 아래 예상 결과는 실측값과 구분한다.
- 후속 실행 결과: [상세 재현 결과](reproduction-results.md). 재현을 위해 FCM URL·인증·transport 교체 지점만 추가했으며, 원래 계약과 순차 전송 로직은 유지했다.

## 1. API 계약 요약

요청 본문은 `Content-Type: application/json`, 인증은 `Authorization: Bearer <accessToken>`을 사용한다.
아래 권한은 현재 SecurityConfig와 서비스의 추가 검증을 함께 반영한다.

| ID | Method / Endpoint | 허용 권한 | 성공 HTTP / message |
|---|---|---|---|
| PUB | POST `/clubs/{clubUUID}/applicants/notifications` | ADMIN 또는 해당 동아리 LEADER | 200 / 지원 결과 처리 완료 |
| STATUS | PATCH `/clubs/{clubUUID}/applications/{applicationUUID}/status` | ADMIN 또는 해당 동아리 LEADER | 200 / 지원자 상태 변경 완료 |
| LIST | GET `/clubs/{clubUUID}/applicants` | ADMIN 또는 해당 동아리 LEADER | 200 / 동아리 지원자 조회 완료 |
| APPLY | POST `/clubs/{clubUUID}/applications` | USER | 200 / 지원서 제출 성공 |

ADMIN은 관리 API 3개에서 모든 동아리에 접근할 수 있다. LEADER는 자신의 동아리만 허용된다.
ADMIN·LEADER가 USER 역할 없이 지원서를 제출하는 것은 허용되지 않는다.
네 API는 익명 공개 경로에 포함되지 않는다.

성공 응답은 `ApiResponse<T>`이다. `data`가 null이면 필드가 생략된다. PUB·STATUS·APPLY는 다음 형식이다.

```json
{"message":"지원 결과 처리 완료"}
```

### 1.1 PUB — 지원 결과 발표 및 알림

- Request DTO: `List<ApplicantResultsRequest>`.
- 각 원소 필드: `aplictUUID: UUID`. 실제 철자는 `aplictUUID`이며 `applicationUUID`로 변경하지 않는다.
- 발표 결과를 본문에서 받지 않고 각 지원서의 DB `privateStatus`를 읽는다.
- DTO에 그룹 기반 `@NotNull`이 있지만 List 원소에 명시적 `@Valid`가 없으므로 원소 검증이 보장된다고 단정하지 않는다.
- 빈 배열·중복 UUID·배열 내 null·누락 UUID·최대 크기 제한은 실행 검증 대상으로 둔다. 서비스에 빈 배열·중복 제거·크기 제한 코드는 없다.

```json
[
  {"aplictUUID":"11111111-1111-4111-8111-111111111111"},
  {"aplictUUID":"22222222-2222-4222-8222-222222222222"}
]
```

현재 처리 순서:

1. 관리 권한과 동아리 존재 확인.
2. 요청 순서대로 지원서를 개별 조회하고 해당 동아리 소속인지 확인.
3. `privateStatus == WAIT`이면 건너뜀.
4. `publicStatus = privateStatus`로 반영.
5. PASS이면 `(profile, club)` 회원 존재 조회 후 없을 때 회원 저장. FAIL이면 회원 변경 없음.
6. FCM 동기 전송 후 지원서 저장.

서비스 클래스의 `@Transactional` 범위에 전체 반복과 외부 호출이 포함된다.
이미 공개된 지원서·동일 UUID 반복을 차단하지 않으므로 같은 요청을 다시 보내면 다시 전송을 시도한다.
회원 존재 조회는 있지만 회원 엔티티에 `(club_id, profile_id)` 복합 Unique 제약은 없다.
FCM 토큰이 없으면 전송을 생략하고 0을 반환하며 결과 공개와 회원 반영은 계속된다.
인증 파일/토큰 획득 등 try 블록의 IOException은 내부에서 삼켜진다. 반면 HTTP 오류 등 런타임 예외는 이 catch 대상이 아니다.
따라서 API 200은 모든 알림의 FCM 접수 성공을 뜻하지 않는다.

### 1.2 STATUS — 지원자 합/불 상태 변경

- Request DTO: `AplictDto.UpdateStatusRequest`.
- `status`: 필수 enum `WAIT | PASS | FAIL` (`@Valid`, `@NotNull`).
- 지원서가 없으면 `APT-202`, 타 동아리 지원서이면 `APT-201`로 거절한다.
- 현재는 `privateStatus`만 변경한다. `publicStatus`·회원·FCM에는 직접 변경이 없다.
- 발표 후 수정 방어가 없고 WAIT로 변경하는 것도 서비스에서 금지하지 않는다.

```json
{"status":"PASS"}
```

```json
{"message":"지원자 상태 변경 완료"}
```

### 1.3 LIST — 지원자 목록 조회

| Query | 타입 / 기본값 | 현재 의미 |
|---|---|---|
| status | 선택 enum / 미지정 | `privateStatus`로 필터링 |
| isResultPublished | 선택 boolean / 미지정 | true: `publicStatus != WAIT`, false: `publicStatus == WAIT` |

두 필터는 함께 지정하면 AND로 적용된다. 미지정하면 해당 조건을 적용하지 않는다.
페이지네이션·명시적 정렬은 없고 반환 순서는 계약으로 보장하지 않는다.
공개 여부는 FCM 성공 여부가 아닌 DB 공개 상태를 뜻한다. 삭제일 조건도 조회 쿼리에 없다.

예: `GET /clubs/{clubUUID}/applicants?status=PASS&isResultPublished=false`

Response DTO: `List<ApplicantsResponse>`.

```json
{
  "message":"동아리 지원자 조회 완료",
  "data":[{
    "aplictUUID":"11111111-1111-4111-8111-111111111111",
    "userName":"테스트지원자",
    "major":"컴퓨터학과",
    "studentNumber":"TEST0001",
    "userHp":"01000000001",
    "privateStatus":"PASS"
  }]
}
```

결과가 없으면 `data: []`이다. `publicStatus`나 FCM 상태 필드를 새로 추가하지 않는다.

### 1.4 APPLY — 지원서 제출

- Request DTO: `AplictDto.SubmitRequest`.
- `qnaList`: 필수 List (`@NotNull`, 원소 `@Valid`). `@NotEmpty`는 없다.
- AnswerRequest: `questionId: Long` 필수, `optionId: Long` 선택, `optionIds: List<Long>` 선택, `answerText: String` 선택.
- `optionIds`가 비어 있지 않으면 각 옵션마다 답변을 생성하고 `optionId` 분기는 사용하지 않는다.
- 그 외에는 단일 옵션 또는 텍스트 답변을 저장한다.

```json
{
  "qnaList":[
    {"questionId":101,"answerText":"지원 동기입니다."},
    {"questionId":102,"optionId":201},
    {"questionId":103,"optionIds":[301,302]}
  ]
}
```

위 ID는 예시이며 테스트 DB의 실제 질문·옵션으로 교체한다.

```json
{"message":"지원서 제출 성공"}
```

현재 검증 순서는 인증 사용자 프로필 → 기존 지원서 → 기존 회원 → 회원 전화번호 중복 → 회원 학번 중복 → 동아리 조회 → 질문/옵션 조회 → 저장이다.
저장 시 공개·비공개 상태는 모두 WAIT이며 답변은 cascade 저장된다.

`uk_aplict_active`는 엔티티상 **전체 행의 `(club_id, profile_id)` Unique 제약**이다.
이름과 달리 `deleteDate IS NULL` 같은 조건부 제약이 아니며, 기존 지원서 조회에도 삭제일 조건이 없다.
실제 MySQL DDL에 해당 제약이 존재하는지는 추후 확인한다.
동시 제출의 유일성 보장과 실패 HTTP 응답은 별개다. 현재 저장 예외는 RuntimeException으로 감싸며 커밋 시점 예외도 가능하므로 항상 `APT-205`로 응답한다고 보장하지 않는다.

현재 제출 메서드에는 모집 상태·활성 폼·필수 질문 답변·질문의 동아리 소속·옵션의 질문 소속에 대한 명시적 검증이 없다.
이 항목은 현재 동작 확인용이며 이번 단계에서 새 검증을 추가하지 않는다.

## 2. 오류 계약

인증/필터 오류와 서비스/MVC 오류는 응답 모양이 다르므로 하나로 통일하지 않는다.

```json
{"status":401,"errorCode":"AUTH_REQUIRED","message":"인증이 필요합니다."}
```

유효하지 않은 JWT는 `INVALID_TOKEN`, 만료 JWT는 `TOKEN_EXPIRED`, 역할 부족은 HTTP 403 / `ACCESS_DENIED`이다.
서비스 예외 예시:

```json
{
  "exception":"AplictException",
  "code":"APT-202",
  "message":"지원자 정보를 찾을 수 없습니다.",
  "status":404,
  "error":"Not Found",
  "additionalData":null
}
```

| 상황 | 코드 / HTTP (코드상 매핑) |
|---|---|
| 타 동아리 회장 접근 | CLDR-101 / 403 |
| 없는 동아리 | CLUB-201 / 404 (권한 확인 순서에 따라 앞선 거절 가능) |
| 없는 지원자, PUB의 타 동아리 지원자 | APT-202 / 404 |
| STATUS의 타 동아리 지원서 | APT-201 / 404 |
| 기존 지원서 | APT-205 / 400 |
| 기존 회원 | APT-206 / 400 |
| 회원 전화번호·학번 중복 | APT-207·APT-208 / 400 |
| 인증 사용자 프로필 없음 | USR-201 / 400 |
| DTO 검증 실패 | INVALID_ARGUMENT / 400 |
| 본문 누락·파싱 불가·본문 enum 오류 | INVALID_REQUEST_BODY / 400 |
| 미처리 예외 | NO_CATCH_ERROR / 500 |

경로 UUID·쿼리 enum/boolean 변환 실패의 전용 handler는 없다. 일반 Exception handler와 MVC 처리에 따른 실제 응답을 검증해야 하며, 일반적인 관례만으로 400이라고 문서화하지 않는다.
질문·옵션 미존재의 IllegalArgumentException도 전용 handler가 없다.

## 3. 실행 검증 시나리오

오류별 주입 조건, 상태 변경·발표 경합, 초기값과 정확한 기대 건수는 [상세 재현 시나리오](detailed-reproduction-scenarios.md)에 정의한다. 실행 시 해당 상세 조건을 함께 적용한다.

모든 시나리오의 실행 상태는 **미실행**이다. ID를 테스트명·결과 파일에 공통 사용한다.
AUTH는 네 API 각각에 적용한다. DB 결과는 요청 완료 후 새 트랜잭션으로 읽는다.

| ID | 준비·요청 | 현재 예상 / 검증할 항목 |
|---|---|---|
| AUTH-01 | JWT 없음·변조·만료 | 각각 401, errorCode와 DB/FCM 부작용 없음 |
| AUTH-02 | USER로 관리 API 3개 호출 | 403 ACCESS_DENIED, 부작용 없음 |
| AUTH-03 | 다른 동아리 LEADER로 관리 API 호출 | 403 CLDR-101, 부작용 없음 |
| AUTH-04 | ADMIN으로 관리 API 호출 | 동아리 존재 시 접근 허용 |
| AUTH-05 | USER 역할 없는 ADMIN/LEADER로 APPLY | 403, 지원서 생성 없음 |
| PUB-01 | 미발표 PASS·FAIL 각 1명, 정상 FCM | 200, 공개 결과 일치, PASS 회원 1명, FCM 2회·본문 확인 |
| PUB-02 | WAIT 1명 | 200, 공개 WAIT 유지, 회원·FCM 증가 없음 |
| PUB-03 | 토큰 없는 PASS | 200, 공개 PASS·회원 생성, FCM HTTP 0회 |
| PUB-04 |  같은발표 순차 2회 | 공개 상태 유지, 회원 1명, FCM 재전송 관측 |
| PUB-05 | 같은 UUID를 본문에 2회 | 같은 지원서 반복 처리·FCM 재전송 관측 |
| PUB-06 | 동일 발표 동시 10회 × 10회 반복 | 시작 장벽 사용; 응답별 성공/실패·회원/FCM 수 기록, 재현 안 되면 미재현 표시 |
| PUB-07 | 첫 전송 200, 다음 전송 500 | DB 롤백과 먼저 발생한 외부 호출 불일치 검증; HTTP 예외 확인 |
| PUB-08 | 인증 토큰 공급 단계 IOException | 내부 0 반환 후 공개·회원 커밋 여부, API 200 확인 |
| PUB-09 | 유효 UUID 다음 없는/타 동아리 UUID | 404, DB 롤백과 선행 FCM 호출 확인; 잘못된 UUID가 첫 항목인 경우도 비교 |
| PUB-10 | [], [{}], [null], UUID null, 본문 없음 | 실제 검증·상태 코드·예외·부작용 기록; List 원소 검증 단정 금지 |
| STATUS-01 | WAIT→PASS→FAIL→WAIT | 매번 200, private만 변경, public WAIT·회원·FCM 유지 |
| STATUS-02 | 공개 PASS 이후 private FAIL | 현재 수정 허용, public PASS 유지; 향후 정책과 차이 기록 |
| STATUS-03 | 없는/타 동아리 지원서 | 각각 APT-202/APT-201, 404, 변경 없음 |
| STATUS-04 | null/누락/알 수 없는 status | DTO 검증·JSON 파싱 오류 형식 확인 |
| LIST-01 | private 3종 × public 공개/미공개 데이터 | 필터 없음, 각 status, 공개 true/false, 조합의 포함/제외 확인 |
| LIST-02 | 지원자 없음 | 200, data [] |
| LIST-03 | 잘못된 query·경로 UUID | 실제 오류 코드·HTTP 기록; 정렬은 assertion 대상 제외 |
| APPLY-01 | 새 USER, 유효 텍스트·단일·다중 답변 | 200, 지원서 1행·상태 WAIT/WAIT·답변/옵션 연결 확인 |
| APPLY-02 | 같은 USER가 같은 동아리 재제출 | 400 APT-205, 추가 지원서 없음 |
| APPLY-03 | 같은 USER 동시 제출 10회 | 지원서 총 1행, Unique 제약 실제 존재 확인, 실패 응답 분포 기록 |
| APPLY-04 | 기존 회원·회원과 전화/학번 중복 | APT-206/207/208, 저장 없음 (검사 순서 고려) |
| APPLY-05 | qnaList null/누락, questionId 누락 | 400 검증 오류, 저장 없음 |
| APPLY-06 | qnaList [], 질문/옵션 미존재 | 빈 목록 허용 여부와 미존재 오류·롤백 확인 |
| APPLY-07 | 다른 동아리 질문·다른 질문 옵션, 두 옵션 필드 동시 입력 | 현재 소속 검증 부재와 optionIds 우선 처리 확인 |
| APPLY-08 | deleteDate가 있는 기존 지원서 | 기존 지원 검사·Unique 제약이 계속 적용되는지 확인 |
| APPLY-09 | 없는 동아리·프로필 없는 USER | CLUB-201/USR-201, 저장 없음 |

각 실행 결과에는 기준 커밋·설정·데이터 ID·요청(토큰 제거)·응답·DB 전후 값·FCM 호출 수/본문·로그를 남긴다.
동시성 테스트는 격리된 데이터셋을 사용하고, FCM을 대역으로 연결한 뒤 수행한다.

## 4. 유지 항목과 후속 요구사항 분리

10주차에는 위 네 API의 경로·메서드·JSON 필드·성공 HTTP/메시지·역할/동아리 권한을 유지한다.
명세 확정 및 후속 대역 연결에서 Endpoint·DTO·권한 규칙은 변경하지 않았다. 실행 검증 범위와 미검증 항목은 [재현 결과](reproduction-results.md)에 구분했다.

후속 개선에서 의도적으로 바꿀 동작은 다음과 같다. 현재 Before의 합격 조건으로 적용하지 않는다.

| 후속 요구사항 | 목표 |
|---|---|
| 발표 확정 | 지원서별 최초 PASS/FAIL 발표 이후 다른 상태 수정 금지; 같은 상태는 변경 없이 성공 |
| 중복 요청 | 동일 발표 재요청·본문 UUID 중복 시 추가 회원·알림 작업 생성 없음 |
| 원자성 | 모든 대상 검증과 결과/회원/알림 작업 저장을 원자적으로 처리 |
| 외부 호출 분리 | 성공은 결과·작업 저장 완료, FCM 접수 성공과 구분 |
| 오류 정책 | 발표 후 다른 상태 수정의 409 도입은 기존 계약 유지의 예외로 별도 명시 |

## 5. 코드 근거 및 다음 단계

아래 경로는 저장소 루트 기준이며 괄호는 확인한 메서드/타입이다.

- `src/main/java/com/USWCicrcleLink/server/club/api/ClubController.java` (getApplicants, pushApplicantResults, updateApplicationStatus)
- `src/main/java/com/USWCicrcleLink/server/club/application/api/AplictController.java` (submitApplication)
- `src/main/java/com/USWCicrcleLink/server/club/application/dto/` (ApplicantResultsRequest, ApplicantsResponse, AplictDto)
- `src/main/java/com/USWCicrcleLink/server/club/leader/service/ClubLeaderService.java` (validateLeaderAccess, updateApplicantResults, updateAplictStatus, getApplicants)
- `src/main/java/com/USWCicrcleLink/server/club/leader/service/FcmServiceImpl.java` (sendMessageTo, getAccessToken)
- `src/main/java/com/USWCicrcleLink/server/club/application/service/AplictService.java` (submitAplict, checkIfCanApply)
- `src/main/java/com/USWCicrcleLink/server/club/application/repository/` (AplictRepository, AplictRepositoryCustomImpl)
- `src/main/java/com/USWCicrcleLink/server/club/application/domain/Aplict.java` (uk_aplict_active, 상태 변경)
- `src/main/java/com/USWCicrcleLink/server/club/domain/ClubMembers.java` (회원 제약)
- `src/main/java/com/USWCicrcleLink/server/global/security/config/SecurityConfig.java` (securityFilterChain)
- `src/main/java/com/USWCicrcleLink/server/global/security/` (jwt/filter/JwtFilter, jwt/JwtProvider, exception/CustomAuthenticationEntryPoint, exception/CustomAccessDeniedHandler)
- `src/main/resources/yaml/application-security.yml` (익명 허용 경로)
- `src/main/java/com/USWCicrcleLink/server/global/response/` (ApiResponse, ErrorResponse)
- `src/main/java/com/USWCicrcleLink/server/global/exception/` (GlobalExceptionHandler, ExceptionType)

기존 `src/test/java/com/USWCicrcleLink/server/aplict/api/AplictControllerTest.java`와 `aplict/repository/AplictRepositoryTest.java`는 주석 처리되어 있다.
기존 `club/leader/service/ClubLeaderServiceAccessTest.java`는 서비스 권한 테스트이며 HTTP 필터·JSON·DB 계약 검증을 대체하지 않는다.

다음 단계: FCM 대역·격리된 MySQL 데이터 구성 후 위 ID로 실행 검증한다. 50명/100명 성능 측정은 신규 발표 데이터와 재발표 데이터를 분리하고 별도 결과로 기록한다.

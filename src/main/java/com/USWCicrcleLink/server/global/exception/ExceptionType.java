package com.USWCicrcleLink.server.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Getter
@AllArgsConstructor
public enum ExceptionType {
        // 100-199: 인증 및 권한
        // 200-299: 사용자 관리
        // 300-399: 데이터 관리
        // 400-499: 토큰 및 인증
        // 500-599: 메일 및 통신

        /**
         * SERVER ERROR
         */
        // ======= 공통 예외 =======
        SERVER_ERROR("COM-501", "서버에 일시적인 문제가 발생했습니다. 관리자에게 문의해주시면 빠르게 조치하겠습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
        INVALID_INPUT("COM-302", "입력값이 올바르지 않습니다. 다시 한 번 확인해 주세요.", HttpStatus.BAD_REQUEST),

        /**
         * Domain: EmailToken
         */
        EMAIL_TOKEN_NOT_FOUND("EMAIL_TOKEN-001", "유효한 인증 토큰을 찾을 수 없습니다. 다시 시도해 주세요.", BAD_REQUEST),
        EMAIL_TOKEN_IS_EXPIRED("EMAIL_TOKEN-002", "인증 시간이 만료되었습니다. 인증 메일을 다시 요청해 주세요.", BAD_REQUEST),
        EMAIL_TOKEN_CREATION_FALILED("EMAIL_TOKEN-003", "이메일 인증 토큰을 생성하는 중 문제가 발생했습니다.", INTERNAL_SERVER_ERROR),
        EMAIL_TOKEN_STATUS_UPATE_FALIED("EMAIL_TOKEN-004", "이메일 인증 상태를 업데이트하는 중 문제가 발생했습니다.",
                        INTERNAL_SERVER_ERROR),
        EMAIL_TOKEN_NOT_VERIFIED("EMAIL_TOKEN-005", "이메일 인증이 완료되지 않았습니다. 인증을 먼저 진행해 주세요.", BAD_REQUEST),

        /**
         * Domain: User
         */
        USER_NOT_EXISTS("USR-201", "해당 사용자를 찾을 수 없습니다. 아이디를 확인하거나 회원가입을 진행해 주세요.", BAD_REQUEST),
        USER_NEW_PASSWORD_NOT_MATCH("USR-202", "입력하신 두 비밀번호가 일치하지 않습니다.", BAD_REQUEST),
        USER_PASSWORD_NOT_INPUT("USR-203", "비밀번호를 입력해 주세요.", BAD_REQUEST),
        USER_PASSWORD_NOT_MATCH("USR-204", "현재 비밀번호가 일치하지 않습니다. 다시 확인해 주세요.", BAD_REQUEST),
        USER_PASSWORD_UPDATE_FAIL("USR-205", "비밀번호 변경 중 문제가 발생했습니다. 잠시 후 다시 시도해 주세요.", INTERNAL_SERVER_ERROR),
        USER_OVERLAP("USR-206", "이미 가입된 회원입니다. 로그인을 진행해 주세요.", CONFLICT),
        USER_ACCOUNT_OVERLAP("USR-207", "이미 사용 중인 계정(아이디)입니다. 다른 아이디를 사용해 주세요.", CONFLICT),
        USER_INVALID_ACCOUNT_AND_EMAIL("USR-209", "아이디 또는 이메일 정보가 올바르지 않습니다.", BAD_REQUEST),
        USER_UUID_NOT_FOUND("USR-210", "회원 정보를 찾을 수 없습니다. (UUID 불일치)", BAD_REQUEST),
        USER_AUTHENTICATION_FAILED("USR-211", "아이디 또는 비밀번호가 일치하지 않습니다.", UNAUTHORIZED),
        INVALID_EVENT_CODE("EVT-101", "이벤트 코드가 올바르지 않습니다.", BAD_REQUEST),
        EVENT_ALREADY_VERIFIED("EVT-102", "이미 인증 이벤트에 참여하셨습니다.", BAD_REQUEST),
        USER_PASSWORD_CONDITION_FAILED("USR-214", "비밀번호는 영문, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다.", BAD_REQUEST),
        USER_PASSWORD_NOT_REUSE("USR-217", "현재 비밀번호와 동일한 비밀번호로 변경할 수 없습니다.", BAD_REQUEST),
        USER_CREATION_FAILED("USR-218", "회원 가입 처리 중 문제가 발생했습니다.", INTERNAL_SERVER_ERROR),
        USER_UUID_IS_NOT_VALID("USR-219", "요청받은 가입 정보(UUID)가 일치하지 않습니다.", UNAUTHORIZED),
        THIRD_PARTY_LOGIN_ATTEMPT("USR-220", "잘못된 경로로 로그인을 시도했습니다.", UNAUTHORIZED),

        /**
         * Domain: ClubMemberTemp
         */
        CLUB_MEMBERTEMP_CREATE_FAILED("CMEM-TEMP-301", "기존 회원 정보를 생성하는 데 실패했습니다.", INTERNAL_SERVER_ERROR),
        CLUB_MEMBERTEMP_IS_DUPLICATED("CMEM-TEMP-302", "이미 등록된 이메일입니다.", BAD_REQUEST),
        CLUB_MEMBERTEMP_IS_EXISTS("CMEM-TEMP-303", "이미 등록된 프로필 정보가 있습니다.", BAD_REQUEST),
        /**
         * Domain: ClubMemberAccountStatus
         */
        CLUB_MEMBER_ACCOUNTSTATUS_CREATE_FAILED("CMEM-ACST-301", "AccountStatus 객체 생성 과정중 오류가 발생했습니다",
                        INTERNAL_SERVER_ERROR),
        CLUB_MEMBER_ACCOUNTSTATUS_SAVE_FAILED("CMEM-ACST-302", "AccountStatus 객체 저장 과정중 오류가 발생했습니다",
                        INTERNAL_SERVER_ERROR),
        CLUB_MEMBER_ACCOUNTSTATUS_COUNT_NOT_MATCH("CMEM-ACST-303", "사용자가 요청한 개수와 실제 요청된 개수가 다릅니다",
                        INTERNAL_SERVER_ERROR),
        CLUB_MEMBER_ACCOUNTSTATUS_REQEUST_NOT_MATCH("CMEM-ACST-304", "사용자가 요청한 동아리와 실제 요청값이 다르게 생성되었습니다",
                        INTERNAL_SERVER_ERROR),

        /**
         * Domain: Security
         */
        INVALID_ROLE("TOK-201", "권한 정보가 올바르지 않습니다.", BAD_REQUEST),
        UNAUTHENTICATED_USER("TOK-204", "로그인이 필요하거나 인증되지 않은 사용자입니다.", UNAUTHORIZED),
        INVALID_TOKEN("TOK-202", "로그인 정보가 유효하지 않습니다. 다시 로그인해 주세요.", UNAUTHORIZED),

        /**
         * Domain: Club
         */
        CLUB_NOT_EXISTS("CLUB-201", "해당 동아리를 찾을 수 없습니다.", NOT_FOUND),
        ClUB_CHECKING_ERROR("CLUB-202", "동아리 정보를 조회하는 중 문제가 발생했습니다.", INTERNAL_SERVER_ERROR),
        CLUB_NAME_ALREADY_EXISTS("CLUB-203", "이미 사용 중인 동아리 이름입니다. 다른 이름을 사용해 주세요.", CONFLICT),
        CLUB_MAINPHOTO_NOT_EXISTS("CLUB-204", "등록된 동아리 메인 사진이 없습니다.", NOT_FOUND),
        CLUB_ROOM_ALREADY_EXISTS("CLUB-205", "해당 동아리방은 이미 사용 중입니다.", CONFLICT),

        /**
         * Domain: ClubCategory
         */
        CATEGORY_NOT_FOUND("CTG-201", "해당 카테고리를 찾을 수 없습니다.", NOT_FOUND),
        INVALID_CATEGORY_COUNT("CG-202", "카테고리는 최대 3개까지만 선택할 수 있습니다.", PAYLOAD_TOO_LARGE),
        DUPLICATE_CATEGORY("CTG-203", "이미 존재하는 카테고리입니다.", CONFLICT),

        /**
         * Domain: ClubIntro
         */
        CLUB_INTRO_NOT_EXISTS("CINT-201", "동아리 소개글을 찾을 수 없습니다.", NOT_FOUND),
        GOOGLE_FORM_URL_NOT_EXISTS("CINT-202", "등록된 구글 폼 링크가 없습니다.", BAD_REQUEST),
        INVALID_RECRUITMENT_STATUS("CINT-303", "모집 상태 값이 올바르지 않습니다.", BAD_REQUEST),

        /**
         * Domain: Admin
         */
        ADMIN_NOT_EXISTS("ADM-201", "관리자 계정을 찾을 수 없습니다.", NOT_FOUND),
        ADMIN_PASSWORD_NOT_MATCH("ADM-202", "관리자 비밀번호가 일치하지 않습니다.", BAD_REQUEST),

        /**
         * Domain: Notice
         */
        NOTICE_NOT_EXISTS("NOT-201", "요청하신 공지사항을 찾을 수 없습니다.", NOT_FOUND),
        UP_TO_5_PHOTOS_CAN_BE_UPLOADED("NOT-202", "사진은 최대 5장까지만 업로드할 수 있습니다.", PAYLOAD_TOO_LARGE),
        NOTICE_PHOTO_NOT_EXISTS("NOT-204", "첨부된 사진을 찾을 수 없습니다.", NOT_FOUND),
        INVALID_PHOTO_ORDER("NOT-205", "사진 순서는 1번부터 5번 사이여야 합니다.", BAD_REQUEST),
        NOTICE_CHECKING_ERROR("NOT-206", "공지사항을 불러오는 중 문제가 발생했습니다.", BAD_REQUEST),
        NOTICE_NOT_AUTHOR("NOT-207", "공지사항 작성 권한이 없습니다.", FORBIDDEN),

        /**
         * Domain: Profile
         */
        PROFILE_NOT_EXISTS("PFL-201", "프로필 정보를 찾을 수 없습니다.", NOT_FOUND),
        PROFILE_UPDATE_FAIL("PFL-202", "프로필 수정 중 문제가 발생했습니다.", INTERNAL_SERVER_ERROR),
        PROFILE_NOT_INPUT("PFL-203", "프로필 정보는 필수 입력값입니다.", BAD_REQUEST),
        DUPLICATE_PROFILE("PFL-204", "이미 등록된 프로필이 있습니다.", BAD_REQUEST),
        DEPARTMENT_NOT_INPUT("PFL-205", "학과를 선택해 주세요.", BAD_REQUEST),
        PROFILE_ALREADY_EXISTS("PFL-207", "이미 프로필이 존재합니다.", BAD_REQUEST),
        INVALID_MEMBER_TYPE("PFL-208", "회원 유형(Member Type)이 올바르지 않습니다.", BAD_REQUEST),
        PROFILE_VALUE_MISMATCH("PFL-209", "요청하신 프로필 정보가 일치하지 않습니다.", BAD_REQUEST),
        PROFILE_CREATION_FAILED("PFL-210", "프로필 생성 중 문제가 발생했습니다.", INTERNAL_SERVER_ERROR),

        /**
         * Domain: ClubIntroPhoto, Club(MainPhoto)
         */
        PHOTO_ORDER_MISS_MATCH("CLP-201", "범위를 벗어난 사진 순서 값입니다.", BAD_REQUEST),
        CLUB_ID_NOT_EXISTS("CLP-202", "동아리 ID가 존재하지 않습니다.", INTERNAL_SERVER_ERROR),

        /**
         * Domain: ClubLeader
         */
        CLUB_LEADER_ACCESS_DENIED("CLDR-101", "해당 동아리에 대한 관리 권한이 없습니다.", FORBIDDEN),
        CLUB_LEADER_NOT_EXISTS("CLDR-201", "동아리 회장 정보를 찾을 수 없습니다.", BAD_REQUEST),
        ClUB_LEADER_PASSWORD_NOT_MATCH("CLDR-202", "회장 계정 비밀번호가 일치하지 않습니다.", BAD_REQUEST),
        LEADER_ACCOUNT_ALREADY_EXISTS("CLDR-203", "이미 등록된 동아리 회장 아이디입니다.", UNPROCESSABLE_ENTITY),
        LEADER_NAME_REQUIRED("CLDR-204", "회장 이름은 필수로 입력해야 합니다.", BAD_REQUEST),

        /**
         * Domain: ClubMember
         */
        CLUB_MEMBER_NOT_EXISTS("CMEM-201", "해당 동아리 회원을 찾을 수 없습니다.", NOT_FOUND),
        CLUB_MEMBER_ALREADY_EXISTS("CMEM-202", "이미 가입되어 있는 동아리 회원입니다.", BAD_REQUEST),

        /**
         * Domain: ClubMemberAccountStatus
         */
        CLUB_MEMBER_SIGN_UP_REQUEST_NOT_EXISTS("CMEMT-201", "회원가입 요청 내역을 찾을 수 없습니다.", NOT_FOUND),

        /**
         * Domain: Aplict
         */
        APLICT_NOT_EXISTS("APT-201", "지원서를 찾을 수 없습니다.", NOT_FOUND),
        APPLICANT_NOT_EXISTS("APT-202", "유효한 지원자 정보를 찾을 수 없습니다.", NOT_FOUND),
        ADDITIONAL_APPLICANT_NOT_EXISTS("APT-203", "추가 합격 대상자를 찾을 수 없습니다.", NOT_FOUND),
        APPLICANT_COUNT_MISMATCH("APT-204", "선택하신 지원자 수가 실제와 다릅니다. 다시 확인해 주세요.", BAD_REQUEST),
        ALREADY_APPLIED("APT-205", "이미 지원하신 동아리입니다. 결과를 기다려 주세요.", BAD_REQUEST),
        ALREADY_MEMBER("APT-206", "이미 해당 동아리의 회원입니다.", BAD_REQUEST),
        PHONE_NUMBER_ALREADY_REGISTERED("APT-207", "이미 등록된 전화번호입니다.", BAD_REQUEST),
        STUDENT_NUMBER_ALREADY_REGISTERED("APT-208", "이미 등록된 학번입니다.", BAD_REQUEST),
        APLICT_ACCESS_DENIED("APT-209", "이 지원서를 볼 권한이 없습니다.", FORBIDDEN),

        /**
         * Domain: AuthCodeToken
         */
        INVALID_AUTH_CODE("AC-101", "입력하신 인증번호가 일치하지 않습니다.", BAD_REQUEST),
        AUTHCODETOKEN_NOT_EXISTS("AC-102", "유효한 인증 정보를 찾을 수 없습니다. 다시 시도해 주세요.", BAD_REQUEST),

        /**
         * Domain: WithdrawalToken
         */
        INVALID_WITHDRAWAL_CODE("WT-101", "탈퇴 인증번호가 일치하지 않습니다.", BAD_REQUEST),
        WITHDRAWALTOKEN_NOT_EXISTS("WT-102", "탈퇴 처리를 위한 정보가 유효하지 않습니다.", BAD_REQUEST),

        /**
         * 공통
         */
        SEND_MAIL_FAILED("EML-501", "메일을 전송하지 못했습니다. 잠시 후 다시 시도해 주세요.", INTERNAL_SERVER_ERROR),
        INVALID_UUID_FORMAT("UUID-502", "올바르지 않은 UUID 형식입니다.", BAD_REQUEST),
        TOO_MANY_ATTEMPT("ATTEMPT-503", "너무 많은 시도가 감지되었습니다. 5분 후에 다시 시도해 주세요.", BAD_REQUEST),
        PHOTO_FILE_IS_EMPTY("PHOTO-504", "사진 파일이 없습니다. 파일을 선택해 주세요.", BAD_REQUEST),
        PHOTO_NOT_FOUND("PHOTO-505", "요청하신 사진을 찾을 수 없습니다.", NOT_FOUND),
        INVALID_ENUM_VALUE("ENUM-401", "올바르지 않은 선택값(Enum)입니다.", BAD_REQUEST),

        /**
         * File I/O
         */
        FILE_ENCODING_FAILED("FILE-301", "파일 이름을 처리하는 중 문제가 발생했습니다.", BAD_REQUEST),
        FILE_CREATE_FAILED("FILE-302", "파일을 생성하지 못했습니다. 관리자에게 문의해 주세요.", INTERNAL_SERVER_ERROR),
        INVALID_PHOTO_DATA("FILE-303", "사진이나 순서 정보가 누락되었습니다.", BAD_REQUEST),
        PHOTO_ORDER_MISMATCH("FILE-304", "사진 개수와 순서 개수가 일치하지 않습니다.", BAD_REQUEST),
        FILE_SAVE_FAILED("FILE-305", "파일 저장 중 문제가 발생했습니다.", INTERNAL_SERVER_ERROR),
        FILE_UPLOAD_FAILED("FILE-306", "파일 업로드에 실패했습니다. 네트워크를 확인하거나 다시 시도해 주세요.", BAD_REQUEST),
        FILE_DELETE_FAILED("FILE-307", "파일 삭제 중 문제가 발생했습니다.", BAD_REQUEST),
        MAXIMUM_FILE_LIMIT_EXCEEDED("FILE-308", "업로드 가능한 최대 파일 개수를 초과했습니다.", BAD_REQUEST),
        INVALID_FILE_NAME("FILE-309", "파일 이름에 사용할 수 없는 문자가 포함되어 있습니다.", BAD_REQUEST),
        MISSING_FILE_EXTENSION("FILE-310", "파일 확장자가 없습니다. (예: .jpg, .png)", BAD_REQUEST),
        UNSUPPORTED_FILE_EXTENSION("FILE-311", "지원하지 않는 파일 형식입니다.", BAD_REQUEST),
        FILE_VALIDATION_FAILED("FILE-312", "파일 유효성 검사에 실패했습니다. 손상된 파일인지 확인해 주세요.", BAD_REQUEST);

        private final String code;
        private final String message;
        private final HttpStatus status;
}

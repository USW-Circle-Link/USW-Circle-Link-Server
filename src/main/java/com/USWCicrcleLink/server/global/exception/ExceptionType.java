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
//    100-199: 인증 및 권한
//    200-299: 사용자 관리
//    300-399: 데이터 관리
//    400-499: 토큰 및 인증
//    500-599: 메일 및 통신

    /**
     * SERVER ERROR
     */
    SERVER_ERROR("SER-501", "서버 오류입니다. 관리자에게 문의해주세요", INTERNAL_SERVER_ERROR),

    /**
     * Domain: EmailToken
     */
    EMAIL_TOKEN_NOT_FOUND("EMAIL_TOKEN-001", "해당 토큰이 존재하지 않습니다.", BAD_REQUEST),
    EMAIL_TOKEN_IS_EXPIRED("EMAIL_TOKEN-002", "토큰이 만료되었습니다. 다시 이메일인증 해주세요", BAD_REQUEST),

    /**
     * Domain: User
     */
    USER_NOT_EXISTS("USR-201", "사용자가 존재하지 않습니다.", BAD_REQUEST),
    USER_NEW_PASSWORD_NOT_MATCH("USR-202","새 비밀번호와 새 비밀번호 확인이 일치하지 않습니다.", BAD_REQUEST),
    USER_PASSWORD_NOT_INPUT("USR-203","비밀번호 값이 빈칸입니다",BAD_REQUEST),
    USER_PASSWORD_NOT_MATCH("USR-204","현재 비밀번호와 일치하지 않습니다",BAD_REQUEST),
    USER_PASSWORD_UPDATE_FAIL("USR-205","비밀번호 업데이트에 실패했습니다",INTERNAL_SERVER_ERROR),
    USER_OVERLAP("USR-206", "이미 존재하는 회원입니다.", CONFLICT),
    USER_ACCOUNT_OVERLAP("USR-207", "계정이 중복됩니다.", CONFLICT),
    USER_ACCOUNT_NOT_EXISTS("USR-208", "존재하지 않는 계정입니다.", BAD_REQUEST),
    USER_INVALID_ACCOUNT_AND_EMAIL("USR-209", "올바르지 않은 이메일 혹은 아이디입니다.", BAD_REQUEST),
    USER_UUID_NOT_FOUND("USR-210","회원의 uuid를 찾을 수 없습니다.", BAD_REQUEST),
    USER_AUTHENTICATION_FAILED("USR-211","아이디 혹은 비밀번호가 일치하지 않습니다",UNAUTHORIZED),
    USER_PASSWORD_MISMATCH("USR-212", "두 비밀번호가 일치하지 않습니다", BAD_REQUEST),
    USER_PROFILE_NOT_FOUND("USR-213","프로필 정보를 찾을 수 없습니다", NOT_FOUND),

    /**
     * Domain: Jwt
     */
    UNABLE_GENERATE_ROLE_TOKEN("TOK-201", "해당 역할 토큰 생성 불가능합니다.", BAD_REQUEST),
    INVALID_REFRESH_TOKEN("TOK-202", "유효하지 않은 리프레시 토큰입니다.", UNAUTHORIZED),
    INVALID_ACCESS_TOKEN("TOK-203", "유효하지 않은 엑세스 토큰입니다.", UNAUTHORIZED),
    UNAUTHENTICATED_USER("TOK-204", "인증되지 않은 사용자입니다.", UNAUTHORIZED),

    /**
     * Domain: Club
     */
    CLUB_NOT_EXISTS("CLUB-201", "동아리가 존재하지 않습니다.", NOT_FOUND),
    ClUB_CHECKING_ERROR("CLUB-202", "동아리 조회 중 오류가 발생했습니다.", INTERNAL_SERVER_ERROR),
    CLUB_NAME_ALREADY_EXISTS("CLUB-203", "이미 존재하는 동아리 이름입니다.", CONFLICT),

    /**
     * Domain: ClubIntro
     */
    CLUB_INTRO_NOT_EXISTS("CINT-201", "해당 동아리 소개글이 존재하지 않습니다.", NOT_FOUND),
    GOOGLE_FORM_URL_NOT_EXISTS("CINT-202", "구글 폼 URL이 존재하지 않습니다.", BAD_REQUEST),

    /**
     * Domain: Admin
     */
    ADMIN_NOT_EXISTS("ADM-201", "해당 계정이 존재하지 않습니다.", NOT_FOUND),
    ADMIN_PASSWORD_NOT_MATCH("ADM-202", "관리자 비밀번호가 일치하지 않습니다.", BAD_REQUEST),

    /**
     * Domain: Notice
     */
    NOTICE_NOT_EXISTS("NOT-201", "공지사항이 존재하지 않습니다.", NOT_FOUND),
    UP_TO_5_PHOTOS_CAN_BE_UPLOADED("NOT-202", "최대 5개의 사진이 업로드 가능합니다.", PAYLOAD_TOO_LARGE),
    TITEL_AND_CONENT_REQUIRED("NOT-203", "제목과 내용을 모두 입력해주세요.", UNPROCESSABLE_ENTITY),
    NOTICE_PHOTO_NOT_EXISTS("NOT-204", "사진이 존재하지 않습니다.", NOT_FOUND),

    /**
     * Domain: Profile
     */
    PROFILE_NOT_EXISTS("PFL-201", "프로필이 존재하지 않습니다.", NOT_FOUND),
    PROFILE_UPDATE_FAIL("PFL-202", "프로필 업데이트에 실패했습니다.", INTERNAL_SERVER_ERROR),
    PROFILE_NOT_INPUT("PFL-203","프로필 입력값은 필수입니다.", BAD_REQUEST),

    /**
     * Domain: ClubIntroPhoto, Club(MainPhoto)
     */
    PHOTO_ORDER_MISS_MATCH("CLP-201", "범위를 벗어난 사진 순서 값입니다.", BAD_REQUEST),

    /**
     * Domain: ClubLeader
     */
    CLUB_LEADER_ACCESS_DENIED("CLDR-101","동아리 접근 권한이 없습니다.", FORBIDDEN),
    CLUB_LEADER_NOT_EXISTS("CLDR-201","동아리 회장이 존재하지 않습니다.", BAD_REQUEST),
    ClUB_LEADER_PASSWORD_NOT_MATCH("CLDR-202", "동아리 회장 비밀번호가 일치하지 않습니다", BAD_REQUEST),
    LEADER_ACCOUNT_ALREADY_EXISTS("CLDR-203", "이미 존재하는 동아리 회장 계정입니다.", UNPROCESSABLE_ENTITY),


    /**
     * Domain: ClubMember
     */
    CLUB_MEMBER_NOT_EXISTS("CMEM-201","클럽멤버가 존재하지 않습니다.", NOT_FOUND),

    /**
     * Domain: Aplict
     */
    APLICT_NOT_EXISTS("APT-201","지원서가 존재하지 않습니다.", NOT_FOUND),
    APPLICANT_NOT_EXISTS("APT-202","유효한 지원자가 존재하지 않습니다.", NOT_FOUND),
    ADDITIONAL_APPLICANT_NOT_EXISTS("APT-203","유효한 추합 대상자가 존재하지 않습니다.", NOT_FOUND),
    APPLICANT_COUNT_MISMATCH("APT-204", "선택한 지원자 수와 전체 지원자 수가 일치하지 않습니다.", BAD_REQUEST),

    /**
     * Domain: AuthCodeToken
     */
    INVALID_AUTH_CODE("AC-101", "인증번호가 일치하지 않습니다", BAD_REQUEST),
    AUTHCODETOKEN_NOT_EXISTS("AC-102", "인증 코드 토큰이 존재하지 않습니다", BAD_REQUEST),

    /**
     * Domain: WithdrawalToken
     */
    INVALID_WITHDRAWAL_CODE("WT-101", "인증번호가 일치하지 않습니다", BAD_REQUEST),
    WITHDRAWALTOKEN_NOT_EXISTS("WT-102", "탈퇴 토큰이 존재하지 않습니다", BAD_REQUEST),

    /**
     * 공통
     */
    SEND_MAIL_FAILED("EML-501", "메일 전송에 실패했습니다.", INTERNAL_SERVER_ERROR),
    INVALID_UUID_FORMAT("UUID-502", "유효하지 않은 UUID 형식입니다." , BAD_REQUEST),
    TEXT_IS_EMPTY("TEXT-503", "글이 비어있습니다.", BAD_REQUEST),

    /**
     * File I/O
     */
    FILE_ENCODING_FAILED("FILE-301", "파일 이름 인코딩에 실패했습니다.", BAD_REQUEST),
    FILE_CREATE_FAILED("FILE-302", "파일 생성에 실패했습니다.", INTERNAL_SERVER_ERROR),
    INVALID_PHOTO_DATA("FILE-303", "사진 또는 순서 정보가 제공되지 않았습니다.", BAD_REQUEST),
    PHOTO_ORDER_MISMATCH("FILE-304", "사진의 개수와 순서 정보의 개수가 일치하지 않습니다.", BAD_REQUEST),
    FILE_SAVE_FAILED("FILE-305", "파일 저장에 실패했습니다.", INTERNAL_SERVER_ERROR),
    FILE_UPLOAD_FAILED("FILE-306", "파일 업로드에 실패했습니다.", BAD_REQUEST),
    FILE_DELETE_FAILED("FILE-307", "파일 삭제에 실패했습니다.", BAD_REQUEST),
    MAXIMUM_FILE_LIMIT_EXCEEDED("FILE-308", "업로드 가능한 사진 갯수를 초과했습니다.", BAD_REQUEST),
    INVALID_FILE_NAME("FILE-309", "파일 이름이 유효하지 않습니다.", BAD_REQUEST),
    MISSING_FILE_EXTENSION("FILE-310", "파일 확장자가 없습니다.", BAD_REQUEST),
    UNSUPPORTED_FILE_EXTENSION("FILE-311", "지원하지 않는 파일 확장자입니다.", BAD_REQUEST),
    FILE_VALIDATION_FAILED("FILE-312", "파일 유효성 검사 실패", BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;
}

package com.USWCicrcleLink.server.email.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
@Table(name = "EMAIL_TOKEN_TABLE")
public class EmailToken { // temp 테이블 (이메일 인증 관리)

    // 이메일 토큰 만료 시간 5분
    private static final long EMAIL_TOKEN_CERTIFICATION_TIME_VALUE = 5L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_token_id", nullable = false)
    private Long emailTokenId;

    // 이메일 인증을 위한 UUID (이메일 인증 시 사용)
    @Column(name = "email_token_uuid", unique = true, nullable = false)
    private UUID emailTokenUUID;

    // 회원가입을 위한 UUID (이메일 인증 완료 후 회원가입 시 사용)
    @Column(name = "signup_uuid", unique = true)
    private UUID signupUUID;

    // 인증 요청한 이메일
    @Column(name = "email", unique = true, nullable = false,length = 30)
    private String email;

    // 이메일 토큰 만료시간
    @Column(name = "expiration_time", nullable = false)
    private LocalDateTime expirationTime;

    // 이메일 인증 여부
    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean isVerified=false;

    // 새로운 이메일 인증 토큰 생성
    public static EmailToken createEmailToken(String email) {
        return EmailToken.builder()
                .emailTokenUUID(UUID.randomUUID())  // 이메일 인증용 UUID 생성
                .email(email)
                .expirationTime(LocalDateTime.now().plusMinutes(EMAIL_TOKEN_CERTIFICATION_TIME_VALUE)) // 5분 유효
                .isVerified(false) // 기본값: 인증 전
                .build();
    }

    // 이메일 인증 완료 (signupUuid 생성)
    public void verifyEmail() {
        this.isVerified = true; // 인증 완료 처리
        this.signupUUID = UUID.randomUUID(); // 회원가입용 UUID 생성
    }

    // 만료 여부 확인 메서드
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationTime);
    }

    // 만료시간 연장 (이메일 재인증 요청 시)
    public void extendExpirationTime() {
        this.expirationTime = LocalDateTime.now().plusMinutes(EMAIL_TOKEN_CERTIFICATION_TIME_VALUE);
    }
}

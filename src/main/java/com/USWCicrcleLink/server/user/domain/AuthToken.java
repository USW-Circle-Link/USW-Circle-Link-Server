package com.USWCicrcleLink.server.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Random;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "AUTH_TOKEN_TABLE")
public class AuthToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auth_token_id")
    private Long authTokenId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_uuid", referencedColumnName = "uuid", unique = true)
    private User user;

    @Column(name="auth_code",nullable = false)
    private String authCode;


    public static AuthToken createAuthToken(User user) {
        String authCode = generateRandomAuthCode();
        return AuthToken.builder()
                .user(user)
                .authCode(authCode)
                .build();
    }

    // 인증 코드 생성
    private static String generateRandomAuthCode() {
            Random r = new Random();
            StringBuilder randomNumber = new StringBuilder();
            for(int i = 0; i < 4; i++) {
                randomNumber.append(r.nextInt(10));
            }
            return randomNumber.toString();
    }

    // 인증 코드 검증
    public boolean isAuthCodeValid(String authCode) {
        return this.authCode.equals(authCode);
    }

    // 새로운 인증 번호 생성
    public void updateAuthCode(){
        this.authCode=generateRandomAuthCode();
    }
}

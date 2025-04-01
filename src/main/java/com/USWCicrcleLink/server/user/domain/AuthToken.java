package com.USWCicrcleLink.server.user.domain;

import jakarta.persistence.Column;
import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.Random;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash(value="authToken",timeToLive = 300)
public class AuthToken implements Serializable {

    @Id
    @Column(name="userUUID")
    private UUID userUUID;

    @Column(name="auth_code",nullable = false)
    private String authCode;


    public static AuthToken createAuthToken(User user) {
        String authCode = generateRandomAuthCode();
        return AuthToken.builder()
                .userUUID(user.getUserUUID())
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

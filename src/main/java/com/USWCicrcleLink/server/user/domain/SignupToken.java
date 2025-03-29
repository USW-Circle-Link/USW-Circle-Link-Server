package com.USWCicrcleLink.server.user.domain;

import com.USWCicrcleLink.server.email.domain.EmailToken;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.index.Indexed;


import java.io.Serializable;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash(value="signupToken")
public class SignupToken implements Serializable {

    // 회원가입을 위한 UUID (이메일 인증 완료 후 회원가입 시 사용)
    @Id
    @Column(name = "signup_uuid", unique = true)
    private UUID signupUUID;

    // 이메일 토큰 uuid
    @Column(name = "email_token_uuid", unique = true, nullable = false)
    private UUID emailTokenUUID;

    // 인증 요청한 이메일
    @Column(name = "email", unique = true, nullable = false,length = 30)
    private String email;

    // 새로운 SignupToken 생성
    public static SignupToken createSignupToken(EmailToken emailToken) {
        return SignupToken.builder()
                .signupUUID(UUID.randomUUID())
                .emailTokenUUID(emailToken.getEmailTokenUUID())
                .email(emailToken.getEmail())
                .build();
    }
}

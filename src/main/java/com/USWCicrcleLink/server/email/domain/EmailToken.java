package com.USWCicrcleLink.server.email.domain;

import jakarta.persistence.*;
import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.util.UUID;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
@RedisHash(value="emailToken",timeToLive = 300)
public class EmailToken implements Serializable { // temp 테이블 (이메일 인증 관리)

    // 이메일 인증을 위한 UUID (이메일 인증 시 사용)
    @Id
    @Column(name = "email_token_uuid", unique = true, nullable = false)
    private UUID emailTokenUUID;

    // 인증 요청한 이메일
    @Column(name = "email", unique = true, nullable = false,length = 30)
    private String email;

    // 새로운 이메일 인증 토큰 생성
    public static EmailToken createEmailToken(String email) {
        return EmailToken.builder()
                .emailTokenUUID(UUID.randomUUID())  // 이메일 인증용 UUID 생성
                .email(email)
                .build();
    }

}

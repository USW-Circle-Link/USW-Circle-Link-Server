package com.USWCicrcleLink.server.email.service;

import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.EmailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTokenService {

    @Qualifier("emailTokenRedisTemplate")
    private final RedisTemplate<String, EmailToken> emailTokenRedisTemplate;

    // 이메일 토큰 저장
    public EmailToken saveEmailToken(EmailToken emailToken) {
        log.debug("[saveEmailToken] 시작 - emailToken: {}", emailToken);
        String keyByUUID = "emailToken:" + emailToken.getEmailTokenUUID().toString();
        String keyByEmail = "emailToken:" + emailToken.getEmail();
        log.debug("[saveEmailToken] 생성된 키 - UUID: {}, Email: {}", keyByUUID, keyByEmail);

        // 5분(300초) TTL 설정하여 저장
        emailTokenRedisTemplate.opsForValue().set(keyByUUID, emailToken, Duration.ofMinutes(5));
        emailTokenRedisTemplate.opsForValue().set(keyByEmail, emailToken, Duration.ofMinutes(5));
        log.debug("[saveEmailToken] 저장 완료");

        return emailToken;
    }

    // 이메일 토큰 생성
    @Transactional
    public EmailToken createEmailToken(String email) {
        log.debug("[createEmailToken] 시작 - email: {}", email);
        try {
            EmailToken emailToken = EmailToken.createEmailToken(email);
            log.debug("[createEmailToken] 토큰 생성 완료: {}", emailToken);
            EmailToken savedToken = saveEmailToken(emailToken);
            log.debug("[createEmailToken] 토큰 저장 완료: {}", savedToken);
            return savedToken;
        } catch (Exception e) {
            log.error("[createEmailToken] 이메일 토큰 생성 중 오류 발생, email: {}", email, e);
            throw new EmailException(ExceptionType.EMAIL_TOKEN_CREATION_FALILED);
        }
    }

    // 이메일 토큰의 만료시간 5분 업데이트
    @Transactional
    public EmailToken updateExpirationTime(EmailToken emailToken) {
        log.debug("[updateExpirationTime] 시작 - emailToken: {}", emailToken);
        String keyByUUID = "emailToken:" + emailToken.getEmailTokenUUID().toString();
        String keyByEmail = "emailToken:" + emailToken.getEmail();
        log.debug("[updateExpirationTime] 업데이트할 키 - UUID: {}, Email: {}", keyByUUID, keyByEmail);

        // TTL 갱신
        boolean resultUUID = Boolean.TRUE.equals(emailTokenRedisTemplate.expire(keyByUUID, Duration.ofMinutes(5)));
        boolean resultEmail = Boolean.TRUE.equals(emailTokenRedisTemplate.expire(keyByEmail, Duration.ofMinutes(5)));
        log.debug("[updateExpirationTime] TTL 갱신 결과 - UUID: {}, Email: {}", resultUUID, resultEmail);

        return emailToken;
    }

    // UUID로 이메일 토큰 조회
    @Transactional(readOnly = true)
    public EmailToken getEmailTokenByUUID(String emailTokenUUID) {
        String keyByUUID = "emailToken:" + emailTokenUUID;
        log.debug("[getEmailTokenByUUID] 조회 시작 - 키: {}", keyByUUID);
        EmailToken token = emailTokenRedisTemplate.opsForValue().get(keyByUUID);
        log.debug("[getEmailTokenByUUID] 조회 결과: {}", token);
        return token;
    }

    // 이메일로 이메일 토큰 조회
    @Transactional(readOnly = true)
    public EmailToken getEmailTokenByEmail(String email) {
        String keyByEmail = "emailToken:" + email;
        log.debug("[getEmailTokenByEmail] 조회 시작 - 키: {}", keyByEmail);
        EmailToken token = emailTokenRedisTemplate.opsForValue().get(keyByEmail);
        log.debug("[getEmailTokenByEmail] 조회 결과: {}", token);
        return token;
    }

    // 이메일 토큰 삭제
    public void deleteEmailTokenFromRedis(EmailToken emailToken) {
        String keyByUUID = "emailToken:" + emailToken.getEmailTokenUUID().toString();
        String keyByEmail = "emailToken:" + emailToken.getEmail();
        log.debug("[deleteEmailTokenFromRedis] 삭제 시작 - UUID 키: {}, 이메일 키: {}", keyByUUID, keyByEmail);

        emailTokenRedisTemplate.delete(keyByUUID);
        emailTokenRedisTemplate.delete(keyByEmail);
        log.debug("[deleteEmailTokenFromRedis] 삭제 완료");
    }
}

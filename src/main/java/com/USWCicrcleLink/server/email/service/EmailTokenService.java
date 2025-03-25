package com.USWCicrcleLink.server.email.service;

import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.email.repository.EmailTokenRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.EmailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTokenService {

    private  final EmailTokenRepository emailTokenRepository;

    @Qualifier("emailTokenRedisTemplate")
    private RedisTemplate<String,EmailToken> emailTokenRedisTemplate;

    // emailToken 저장
    public EmailToken saveEmailToken(EmailToken token) {
        emailTokenRedisTemplate.opsForValue().set(token.getEmailTokenUUID().toString(), token);
        return token;
    }

    // uuid로 이메일 토큰 조회
    public EmailToken getEmailToken(String uuid) {
        return emailTokenRedisTemplate.opsForValue().get(uuid);
    }

    /*// email로 이메일 토큰 조회
    public EmailToken getEmailTokenByEmail(String email){
        // 이메일로 uuid 조회
        EmailToken emailToken = emailTokenRedisTemplate.opsForValue().get(email);

    }*/


    // 이메일 토큰 생성
    @Transactional
    public EmailToken createEmailToken(String email) {
        try{
            log.debug("이메일 토큰 생성 완료 email= {}", email);
            EmailToken emailToken = EmailToken.createEmailToken(email);
            return saveEmailToken(emailToken);
        } catch (Exception e){
            log.error("이메일 토큰 생성시 오류 발생");
            throw new EmailException(ExceptionType.EMAIL_TOKEN_CREATION_FALILED);
        }
    }

}

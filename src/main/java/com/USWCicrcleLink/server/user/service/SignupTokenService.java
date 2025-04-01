package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.SignupTokenException;
import com.USWCicrcleLink.server.user.domain.SignupToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SignupTokenService {

    private final RedisTemplate<String, SignupToken> signUpTokenRedisTemplate;

    // SignupToken 저장
    @Transactional
    public void saveSignUpToken(SignupToken signUpToken) {

        String keyByUUID = "signUpToken:" + signUpToken.getEmailTokenUUID().toString();
        String keyByEmail = "signUpToken:" + signUpToken.getEmail();

        // 1시간 TTL 설정하여 저장
        signUpTokenRedisTemplate.opsForValue().set(keyByUUID, signUpToken,Duration.ofHours(1));
        signUpTokenRedisTemplate.opsForValue().set(keyByEmail, signUpToken,Duration.ofHours(1));
    }

    // uuid로 이메일 토큰 조회
    @Transactional(readOnly = true)
    public SignupToken getSignUpTokenByUUID(String emailTokenUUID) {
        String keyByUUID = "signUpToken:" + emailTokenUUID;
        SignupToken signupToken = signUpTokenRedisTemplate.opsForValue().get(keyByUUID);

        // emailTokenUUID에 해당하는 SignupToken 존재하지 않는 경우
        if(signupToken==null){
            throw new SignupTokenException(ExceptionType.SIGNUP_TOKEN_NOT_FOUND);
        }

        return signupToken;
    }

    // email로 이메일 토큰 조회
    @Transactional(readOnly = true)
    public SignupToken getSignUpTokenByEmail(String email) {

        String keyByEmail = "signUpToken:" + email;
        SignupToken signUpToken = signUpTokenRedisTemplate.opsForValue().get(keyByEmail);

        // signUpToken이 존재하지 않는 경우 -> 미인증
        if(signUpToken==null){
            throw new SignupTokenException(ExceptionType.SIGNUP_TOKEN_NOT_FOUND);
        }
        return signUpToken;
    }

    // SignUpToken 검증하기 (인증받은 사용자가 맞는지 확인하기)
    public SignupToken verifyUser(UUID emailTokenUUID, UUID requestSignupUUID) {

        // emailTokenUUID로 SingUpToken 조회
        SignupToken signupToken = getSignUpTokenByUUID(emailTokenUUID.toString());

        // SignUpToken에 저장된 uuid와 매개변수 uuid값이 일치하는지 확인
        if(!signupToken.getSignupUUID().equals(requestSignupUUID)){
            throw new SignupTokenException(ExceptionType.SIGNUP_UUID_IS_NOT_MATCH);
        }

        return signupToken;
    }

    // SignUpToken 레디스에서 삭제하기
    @Transactional
    public void deleteSignUpTokenFromRedis(SignupToken signupToken) {

        String keyByUUID = "signUpToken:" + signupToken.getEmailTokenUUID();
        String keyByEmail = "signUpToken:" + signupToken.getEmail();

        signUpTokenRedisTemplate.delete(keyByUUID);
        signUpTokenRedisTemplate.delete(keyByEmail);
    }
}

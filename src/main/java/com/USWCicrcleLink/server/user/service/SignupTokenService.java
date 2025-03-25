package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.SignupTokenException;
import com.USWCicrcleLink.server.user.domain.SignupToken;
import com.USWCicrcleLink.server.user.repository.SignupTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SignupTokenService {

    private final SignupTokenRepository signupTokenRepository;

    // SignupToken 생성
    @Transactional
    public void createSignupToken(EmailToken emailToken) {
        log.debug("SignupToken 생성 시작 - email: {}", emailToken.getEmail());
        signupTokenRepository.save(SignupToken.createSignupToken(emailToken));
        log.debug("SignupToken 생성 완료");
    }

    // 이메일로 SignupToken 조회
    public SignupToken getSignupTokenByEmail(String email) {
        log.debug("SignupToken 이메일 조회 시작 - email: {}", email);
        return signupTokenRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("SignupToken 조회 실패 - email: {}", email);
                    return new SignupTokenException(ExceptionType.SIGNUP_TOKEN_NOT_FOUND);
                });
    }

    // 이메일 인증이 완료된 사용자인지 검증하기
    public SignupToken verifyUser(UUID emailTokenUUID, UUID requestSignupUUID) {
        log.debug("이메일 인증 유저 검증 시작 - emailTokenUUID: {}, requestSignupUUID: {}", emailTokenUUID, requestSignupUUID);
        SignupToken signupToken = signupTokenRepository.findByEmailTokenUUID(emailTokenUUID)
                .orElseThrow(() -> {
                    log.error("SignupToken 조회 실패 - emailTokenUUID: {}", emailTokenUUID);
                    return new SignupTokenException(ExceptionType.SIGNUP_TOKEN_NOT_FOUND);
                });

        if (!signupToken.getSignupUUID().equals(requestSignupUUID)) {
            log.error("SignupUUID 불일치 - 저장된: {}, 요청된: {}", signupToken.getSignupUUID(), requestSignupUUID);
            throw new SignupTokenException(ExceptionType.SIGNUP_UUID_IS_NOT_MATCH);
        }

        log.debug("이메일 인증 유저 검증 성공");
        return signupToken;
    }

    // signupToken 삭제
    @Transactional
    public void delete(SignupToken signupToken) {
        log.debug("SignupToken 삭제 시작 - emailTokenUUID: {}", signupToken.getEmailTokenUUID());
        signupTokenRepository.delete(signupToken);
        log.debug("SignupToken 삭제 완료");
    }
}

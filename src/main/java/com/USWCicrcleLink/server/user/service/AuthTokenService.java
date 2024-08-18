package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.user.domain.AuthToken;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.dto.AuthCodeRequest;
import com.USWCicrcleLink.server.user.repository.AuthTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthTokenService {

    private final AuthTokenRepository authTokenRepository;

    // 인증 코드 토큰 생성 또는 업데이트
    @Transactional
    public AuthToken createOrUpdateAuthToken(User user) {
        // 인증 토큰이 이미 존재하는지 확인
        return authTokenRepository.findByUserUserUUID(user.getUserUUID())
                .map(existingAuthToken -> {
                    // 토큰이 존재할 경우, 인증 코드를 업데이트
                    log.debug("회원의 인증 코드 토큰이 이미 존재 user_uuid=  {}. 인증 코드 업데이트 메서드 실행.", user.getUserUUID());
                    existingAuthToken.updateAuthCode();
                    return authTokenRepository.save(existingAuthToken);
                })
                .orElseGet(() -> {
                    // 토큰이 존재하지 않을 경우, 새로운 인증 토큰을 생성
                    log.debug("새로운 인증 토큰 생성 시작 user_uuid={}", user.getUserUUID());
                    AuthToken newAuthToken = AuthToken.createAuthToken(user);
                    return authTokenRepository.save(newAuthToken);
                });
    }

    // 인증 코드 토큰 검증
    public void verifyAuthToken(UUID uuid, AuthCodeRequest request) {

        log.debug("인증 코드 토큰 검증 메서드 시작");
        AuthToken authToken = authTokenRepository.findByUserUserUUID(uuid)
                .orElseThrow(() -> new UserException(ExceptionType.USER_UUID_NOT_FOUND));

        log.debug("uuid ={} 에 해당하는 회원 조회 완료", uuid);
        log.debug("인증 코드 일치 확인 시작");
        if (!authToken.isAuthCodeValid(request.getAuthCode())) {
            throw new UserException(ExceptionType.INVALID_AUTH_CODE);
        }
        log.debug("인증 코드 토큰 검증 완료");
    }

    // 검증 완료된 인증 코드 토큰 삭제
    @Transactional
    public void deleteAuthToken(UUID uuid) {

        AuthToken authToken = authTokenRepository.findByUserUserUUID(uuid)
                .orElseThrow(() -> new UserException(ExceptionType.USER_UUID_NOT_FOUND));

        authTokenRepository.delete(authToken);
        log.debug("검증 완료된 인증 코드 토큰 삭제 완료");
    }
}

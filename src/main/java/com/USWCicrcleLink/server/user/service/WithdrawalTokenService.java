package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.WithdrawalTokenException;
import com.USWCicrcleLink.server.global.security.details.CustomUserDetails;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.domain.WithdrawalToken;
import com.USWCicrcleLink.server.user.dto.AuthCodeRequest;
import com.USWCicrcleLink.server.user.repository.WithdrawalTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class WithdrawalTokenService {

    private final WithdrawalTokenRepository withdrawalTokenRepository;

    // 어세스토큰에서 유저정보 가져오기
    private User getUserByAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.user();
    }

    // 탈퇴 토큰 생성 or 업데이트
    public WithdrawalToken createOrUpdateWithdrawalToken() {

        // 탈퇴를 요청한 회원 정보 가져오기
        User user = getUserByAuth();

        // 토큰이 이미 존재하는지 확인
        return withdrawalTokenRepository.findByUserUUID(user.getUserUUID())
                .map(token -> {
                    // 토큰이 존재할 경우, 인증 코드를 업데이트
                    log.debug("회원의 탈퇴 토큰이 이미 존재 user_uuid=  {}. 인증 코드 업데이트 메서드 실행.", user.getUserUUID());
                    token.updateWithdrawalCode();
                    return withdrawalTokenRepository.save(token);
                })
                .orElseGet(() -> {
                    // 토큰이 존재하지 않을 경우, 새로운 인증 토큰을 생성
                    log.debug("새로운 탈퇴 토큰 생성 시작 user_uuid={}", user.getUserUUID());
                    WithdrawalToken newToken = WithdrawalToken.createWithdrawalToken(user);
                    return withdrawalTokenRepository.save(newToken);
                });
    }

    // 탈퇴 코드 토큰 검증
    public UUID verifyWithdrawalToken(AuthCodeRequest authCodeRequest) {

        UUID userUUID  = getUserByAuth().getUserUUID();

        log.debug("탈퇴 코드 토큰 검증 메서드 시작");
        WithdrawalToken token = withdrawalTokenRepository.findByUserUUID(userUUID)
                .orElseThrow(() -> new WithdrawalTokenException(ExceptionType.WITHDRAWALTOKEN_NOT_EXISTS));

        log.debug("uuid ={} 에 해당하는 회원 조회 완료", userUUID);

        log.debug("인증 코드 일치 확인 시작");
        if (!token.isWithdrawalCodeValid(authCodeRequest.getAuthCode())) {
            throw new WithdrawalTokenException(ExceptionType.INVALID_WITHDRAWAL_CODE);
        }
        log.debug("인증 코드 토큰 검증 완료");

        return userUUID;
    }

    // 검증 완료된 토큰 삭제
    @Transactional
    public void deleteWithdrawalToken(UUID uuid) {

        WithdrawalToken token=  withdrawalTokenRepository.findByUserUUID(uuid)
                .orElseThrow(() -> new WithdrawalTokenException(ExceptionType.WITHDRAWALTOKEN_NOT_EXISTS));

        withdrawalTokenRepository.delete(token);

        log.debug("검증 완료된 탈퇴 코드 토큰 삭제 완료");
    }
}

package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.user.domain.EventVerification;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.dto.EventVerifyResponse;
import com.USWCicrcleLink.server.user.repository.EventVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventVerificationService {

    private final EventVerificationRepository eventVerificationRepository;

    // 이벤트 코드: 환경 변수/프로퍼티 없을 경우 기본값 1115 사용
    @Value("${event.code:1115}")
    private String expectedEventCode;

    @Transactional(readOnly = true)
    public boolean checkStatus(User user, UUID clubUUID) {
        return eventVerificationRepository.existsByUserUUIDAndClubUUID(user.getUserUUID(), clubUUID);
    }

    @Transactional
    public EventVerifyResponse verify(User user, UUID clubUUID, String code) {
        // 이미 인증된 경우: 오류 반환
        if (eventVerificationRepository.existsByUserUUIDAndClubUUID(user.getUserUUID(), clubUUID)) {
            log.debug("이미 인증된 상태 - userUUID={}, clubUUID={}", user.getUserUUID(), clubUUID);
            throw new UserException(ExceptionType.EVENT_ALREADY_VERIFIED);
        }

        // 코드 검증 (첫 인증만 코드 검사)
        if (code == null || !code.equals(expectedEventCode)) {
            throw new UserException(ExceptionType.INVALID_EVENT_CODE);
        }

        // 인증 성공 처리 (MYSQL 저장)
        EventVerification saved = eventVerificationRepository.save(EventVerification.create(user.getUserUUID(), clubUUID));
        log.info("이벤트 인증 완료 - userUUID={}, clubUUID={}", user.getUserUUID(), clubUUID);
        // 첫 인증 성공: isFirstVerify=true
        return new EventVerifyResponse(clubUUID, true, saved.getVerifiedAt());
    }
}

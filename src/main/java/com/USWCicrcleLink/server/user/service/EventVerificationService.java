package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.dto.EventVerifyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventVerificationService {

    private final RedisTemplate<String, String> redisTemplate;

    // 이벤트 코드(기본값 1115). 필요 시 프로필별 yml에서 event.code 로 오버라이드
    @Value("${event.code:1115}")
    private String expectedEventCode;

    private String key(UUID userUUID, UUID clubUUID) {
        return "event:verified:" + userUUID + ":" + clubUUID;
    }

    @Transactional(readOnly = true)
    public boolean checkStatus(User user, UUID clubUUID) {
        String val = redisTemplate.opsForValue().get(key(user.getUserUUID(), clubUUID));
        return "1".equals(val);
    }

    @Transactional
    public EventVerifyResponse verify(User user, UUID clubUUID, String code) {
        // 이미 인증된 경우 idempotent 처리
        if (checkStatus(user, clubUUID)) {
            log.debug("이미 인증된 상태 - userUUID={}, clubUUID={}", user.getUserUUID(), clubUUID);
            return new EventVerifyResponse(clubUUID, true);
        }

        // 코드 검증
        if (code == null || !code.equals(expectedEventCode)) {
            throw new UserException(ExceptionType.INVALID_EVENT_CODE);
        }

        // 인증 성공 처리 (Redis 기록)
        redisTemplate.opsForValue().set(key(user.getUserUUID(), clubUUID), "1");
        log.info("이벤트 인증 완료 - userUUID={}, clubUUID={}", user.getUserUUID(), clubUUID);
        return new EventVerifyResponse(clubUUID, true);
    }
}

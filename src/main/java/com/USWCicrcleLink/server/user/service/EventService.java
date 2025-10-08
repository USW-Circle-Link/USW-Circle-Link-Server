package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public boolean checkIsVerified(User user) {
        return user.isVerified();
    }

    @Transactional
    public String verifyEvent(User user, String code) {
        // 인증 완료시
        if (user.isVerified()) {
            throw new UserException(ExceptionType.USER_ALREADY_VERIFIED);
        }

        // 코드검증
        if (!"EVENT2025".equals(code)) {
            throw new UserException(ExceptionType.USER_AUTHENTICATION_FAILED);
        }

        // 인증성공시(완료되면 Db값 1로)
        user.setVerified(true);
        userRepository.save(user);

        log.info("이벤트 인증 완료 - userUUID={}", user.getUserUUID());
        return "이벤트 인증이 완료되었습니다!";
    }
}
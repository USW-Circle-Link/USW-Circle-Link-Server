package com.USWCicrcleLink.server.club.event.service;

import org.springframework.stereotype.Service;

@Service
public class EventService {

    // 정답 코드
    private static final String CORRECT_CODE = "1115";

    // 입력된 코드가 맞는지 검증
    public boolean verifyCode(String code) {
        return CORRECT_CODE.equals(code);
    }
}

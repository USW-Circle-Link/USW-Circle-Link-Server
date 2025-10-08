package com.USWCicrcleLink.server.user.api;

import com.USWCicrcleLink.server.global.security.details.CustomUserDetails;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.dto.EventCodeRequest;
import com.USWCicrcleLink.server.user.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/event")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // 인증 상태 확인하기
    @GetMapping("/status")
    public boolean checkEventStatus(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.user();
        return eventService.checkIsVerified(user);
    }

    // 인증코드 입력 후에 검증하기
    @PostMapping("/verify")
    public String verifyEvent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody EventCodeRequest request
    ) {
        User user = userDetails.user();
        return eventService.verifyEvent(user, request.getCode());
    }
}
package com.USWCicrcleLink.server.club.event.api;

import com.USWCicrcleLink.server.club.event.dto.EventCodeRequest;
import com.USWCicrcleLink.server.club.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/event")
public class EventController {

    private final EventService eventService;

    // 4자리 코드 검증 API
    @PostMapping("/verify")
    public ResponseEntity<String> verify(@RequestBody EventCodeRequest request) {
        boolean result = eventService.verifyCode(request.getCode());
        return ResponseEntity.ok(result ? "pass" : "unpass");
    }
}

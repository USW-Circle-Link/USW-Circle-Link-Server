package com.USWCicrcleLink.server.user.api;

import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.bucket4j.RateLimite;
import com.USWCicrcleLink.server.global.security.details.CustomUserDetails;
import com.USWCicrcleLink.server.global.security.jwt.JwtProvider;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.dto.EventVerifyRequest;
import com.USWCicrcleLink.server.user.dto.EventStatusResponse;
import com.USWCicrcleLink.server.user.dto.EventVerifyResponse;
import com.USWCicrcleLink.server.user.service.EventVerificationService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/users/event")
@RequiredArgsConstructor
public class EventVerificationController {

    private final EventVerificationService eventVerificationService;
    private final JwtProvider jwtProvider;

    // 상태 조회: 특정 clubUUID에 대해 현재 사용자 인증 여부 확인
    @GetMapping("/status")
    public ApiResponse<EventStatusResponse> status(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        User user = userDetails.user();
        String accessToken = jwtProvider.resolveAccessToken(request);
        UUID clubUUID = jwtProvider.getClubUUIDFromAccessToken(accessToken);
        if (clubUUID == null) {
            throw new UserException(ExceptionType.INVALID_INPUT);
        }
        boolean verified = eventVerificationService.checkStatus(user, clubUUID);
        return new ApiResponse<EventStatusResponse>("이벤트 인증 상태 조회", new EventStatusResponse(verified));
    }

    // 코드 검증: Authorization 필요, 본인 + clubUUID 기준으로 인증 처리
    @PostMapping("/verify")
    @RateLimite(action = "EVENT_VERIFY")
    public ApiResponse<EventVerifyResponse> verify(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody EventVerifyRequest request,
            HttpServletRequest httpRequest
    ) {
        User user = userDetails.user();
        String accessToken = jwtProvider.resolveAccessToken(httpRequest);
        UUID clubUUID = jwtProvider.getClubUUIDFromAccessToken(accessToken);
        if (clubUUID == null) {
            throw new UserException(ExceptionType.INVALID_INPUT);
        }
        EventVerifyResponse response = eventVerificationService.verify(user, clubUUID, request.getCode());
        String message = response.isFirstVerify() ? "이벤트 인증 완료" : "이미 인증된 사용자입니다";
        return new ApiResponse<EventVerifyResponse>(message, response);
    }

    @DeleteMapping("/delete")
    public ApiResponse<String> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        User user = userDetails.user();
        String accessToken = jwtProvider.resolveAccessToken(httpRequest);
        UUID clubUUID = jwtProvider.getClubUUIDFromAccessToken(accessToken);
        if (clubUUID == null) {
            throw new UserException(ExceptionType.INVALID_INPUT);
        }
        eventVerificationService.delete(user, clubUUID);
        return new ApiResponse<>("이벤트 인증 삭제가 완료되었습니다.");
    }
}

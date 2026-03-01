package com.USWCicrcleLink.server.club.leader.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FcmTokenRequest {
    @NotBlank(message = "FCM 토큰은 필수 값입니다.")
    String fcmToken;
}

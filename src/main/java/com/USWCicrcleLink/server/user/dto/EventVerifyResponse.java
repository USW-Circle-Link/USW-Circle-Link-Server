package com.USWCicrcleLink.server.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventVerifyResponse {
    @JsonProperty("isFirstVerify")
    private boolean isFirstVerify;

    @JsonProperty("verified_at")
    private LocalDateTime verifiedAt;
}

package com.USWCicrcleLink.server.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventVerifyResponse {
    private UUID clubUUID;

    @JsonProperty("isFirstVerify")
    private boolean isFirstVerify;

    @JsonProperty("verified_at")
    private LocalDateTime verifiedAt;
}

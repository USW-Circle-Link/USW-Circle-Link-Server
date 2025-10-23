package com.USWCicrcleLink.server.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class EventVerifyRequest {
    @NotNull
    private UUID clubUUID;

    @NotBlank
    private String code;
}

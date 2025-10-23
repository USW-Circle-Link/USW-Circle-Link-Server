package com.USWCicrcleLink.server.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EventVerifyRequest {
    @NotBlank
    private String code;
}

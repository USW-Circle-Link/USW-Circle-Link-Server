package com.USWCicrcleLink.server.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthCodeRequest {
    @NotBlank(message = "인증 코드를 입력해주세요.")
    private String authCode;
}

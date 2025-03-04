package com.USWCicrcleLink.server.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthCodeRequest {
    @NotBlank(message = "인증 코드를 입력해주세요.")
    @Pattern(regexp = "\\d+", message = "인증 코드는 숫자만 입력 가능합니다.")
    private String authCode;
}

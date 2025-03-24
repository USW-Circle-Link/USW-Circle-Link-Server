package com.USWCicrcleLink.server.user.dto;

import com.USWCicrcleLink.server.global.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthCodeRequest {
    @NotBlank(message = "인증 코드를 입력해주세요.",groups = ValidationGroups.NotBlankGroup.class)
    private String authCode;
}

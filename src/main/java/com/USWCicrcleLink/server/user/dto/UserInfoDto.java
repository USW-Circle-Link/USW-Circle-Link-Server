package com.USWCicrcleLink.server.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDto {

    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    private String userAccount;

    @NotBlank(message = "이메일 필수 입력 값입니다.")
    private String email;
}

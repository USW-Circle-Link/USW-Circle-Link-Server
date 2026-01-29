package com.USWCicrcleLink.server.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailDTO {

    @NotBlank(message = "이메일(포털 아이디)을 입력해주세요")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "이메일 아이디는 영문, 숫자, ., _, - 만 포함할 수 있습니다 (@ 제외)")
    private String email;

}

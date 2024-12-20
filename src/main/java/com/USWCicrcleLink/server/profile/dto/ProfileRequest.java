package com.USWCicrcleLink.server.profile.dto;

import com.USWCicrcleLink.server.global.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileRequest {

    @NotBlank(message = "이름은 필수 입력 값입니다.")
    @Pattern(regexp = "^[a-zA-Z가-힣]+$", message = "이름은 영어 또는 한글만 입력 가능합니다", groups = ValidationGroups.PatternGroup.class)
    private String userName;

    @NotBlank(message = "학번은 필수 입력 값입니다.",groups = ValidationGroups.NotBlankGroup.class)
    @Pattern(regexp = "^[0-9]*$", message = "숫자만 입력 가능 합니다",groups = ValidationGroups.PatternGroup.class)
    @Size(min = 8, max = 8, message = "학번은 8자리 숫자여야 합니다.")
    private String studentNumber;

    @NotBlank(message = "전화번호는 필수 입력값 입니다")
    @Pattern(regexp = "^[0-9]*$", message = "숫자만 입력 가능 합니다", groups = ValidationGroups.PatternGroup.class)
    @Size(min = 11, max = 11, message = "전화번호는 11자리 숫자여야 합니다.")
    private String userHp;

    @NotBlank(message = "학과는 필수 입력 값입니다.")
    private String major;
}

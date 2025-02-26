package com.USWCicrcleLink.server.admin.admin.dto;

import com.USWCicrcleLink.server.club.club.domain.Department;
import com.USWCicrcleLink.server.global.validation.Sanitize;
import com.USWCicrcleLink.server.global.validation.ValidClubRoomNumber;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AdminClubCreationRequest {

    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    @Size(min = 5, max = 20, message = "아이디는 5~20자 이내여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문 대소문자 및 숫자만 포함할 수 있으며 공백을 포함할 수 없습니다.")
    @Sanitize
    private String leaderAccount;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자 이내여야 합니다.")
    @Pattern(regexp = "^(?!.*\\s)[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]+$",
            message = "비밀번호는 영문 대소문자, 숫자, 특수문자만 포함할 수 있으며 공백을 포함할 수 없습니다.")
    @Sanitize
    private String leaderPw;

    @NotBlank(message = "비밀번호 확인은 필수 입력 값입니다.")
    @Sanitize
    private String leaderPwConfirm;

    @NotBlank(message = "동아리명은 필수 입력 값입니다.")
    @Size(max = 10, message = "동아리명은 10자 이내여야 합니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]+$", message = "동아리명에는 공백 또는 특수문자를 포함할 수 없습니다.")
    @Sanitize
    private String clubName;

    @NotNull(message = "학부는 필수 입력 값입니다.")
    @Enumerated(EnumType.STRING)
    private Department department;

    @NotBlank(message = "운영자 비밀번호는 필수 입력 값입니다.")
    @Sanitize
    private String adminPw;

    @NotBlank(message = "동아리 호수는 필수 입력 값입니다.")
    @ValidClubRoomNumber
    private String clubRoomNumber;
}
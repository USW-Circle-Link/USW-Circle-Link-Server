package com.USWCicrcleLink.server.user.dto;

import com.USWCicrcleLink.server.user.domain.UserTemp;
import  com.USWCicrcleLink.server.global.validation.ValidationGroups.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequest {

    @NotBlank(message = "아이디는 필수 입력 값입니다.",groups = NotBlankGroup.class)
    @Size(min = 5, max = 20, message = "아이디는 5~20자 이내여야 합니다.",groups = SizeGroup.class )
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문 대소문자 및 숫자만 가능합니다.",groups = PatternGroup.class)
    private String account;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.",groups = NotBlankGroup.class)
    @Size(min = 5, max = 20, message = "비밀번호는 5~20자 이내여야 합니다.",groups = SizeGroup.class)
    @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]+$", message = "비밀번호는 영문 대소문자, 숫자, 특수문자만 포함할 수 있습니다.",groups = PatternGroup.class)
    private String password;

    @NotBlank(message = "이름 필수 입력 값입니다.")
    private String userName;

    // 전화번호는 필수 값 x
    private String telephone;

    @NotBlank(message = "학번 필수 입력 값입니다.")
    private String studentNumber;

    @NotBlank(message = "학과 필수 입력 값입니다.")
    private String major;

    @NotBlank(message = "이메일 필수 입력 값입니다.")
    private String email;

    private Boolean isEmailVerified = false;

    public UserTemp toEntity(String encodedPassword) {
        return UserTemp.builder()
                .tempAccount(account)
                .tempPw(encodedPassword) // 인코딩된 비밀번호로 저장
                .tempName(userName)
                .tempHp(telephone)
                .tempStudentNumber(studentNumber)
                .tempMajor(major)
                .tempEmail(email)
                .isEmailVerified(isEmailVerified)
                .build();
    }

}

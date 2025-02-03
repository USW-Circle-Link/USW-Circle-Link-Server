package com.USWCicrcleLink.server.user.dto;

import com.USWCicrcleLink.server.global.validation.ValidationGroups;
import com.USWCicrcleLink.server.user.domain.ExistingMember.ClubMemberTemp;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExistingMemberSignUpRequest {

    @NotBlank(message = "아이디는 필수 입력 값입니다.",groups = ValidationGroups.NotBlankGroup.class)
    @Size(min = 5, max = 20, message = "아이디는 5~20자 이내여야 합니다.",groups = ValidationGroups.SizeGroup.class )
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문 대소문자 및 숫자만 가능합니다.",groups = ValidationGroups.PatternGroup.class)
    private String account;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.",groups = ValidationGroups.NotBlankGroup.class)
    @Size(min = 5, max = 20, message = "비밀번호는 5~20자 이내여야 합니다.",groups = ValidationGroups.SizeGroup.class)
    @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~`]+$", message = "비밀번호는 영문 대소문자, 숫자, 특수문자만 포함할 수 있습니다.",groups = ValidationGroups.PatternGroup.class)
    private String password;

    @NotBlank(message = "이름 필수 입력 값입니다.")
    @Pattern(regexp = "^[a-zA-Z가-힣]+$", message = "이름은 영어 또는 한글만 입력 가능합니다", groups = ValidationGroups.PatternGroup.class)
    private String userName;

    @NotBlank(message = "전화번호는 필수 입력값 입니다")
    @Size(max =11, message = "전화번호는 11자리까지 입력가능합니다",groups = ValidationGroups.SizeGroup.class)
    @Pattern(regexp = "^[0-9]*$", message = "숫자만 입력 가능 합니다", groups = ValidationGroups.PatternGroup.class)
    private String telephone;

    @NotBlank(message = "학번 필수 입력 값입니다.",groups = ValidationGroups.NotBlankGroup.class)
    @Size(max =8, message = "학번은 최대 8자리 입니다",groups = ValidationGroups.SizeGroup.class)
    @Pattern(regexp = "^[0-9]*$", message = "숫자만 입력 가능 합니다",groups = ValidationGroups.PatternGroup.class)
    private String studentNumber;

    @NotBlank(message = "학과 필수 입력 값입니다.")
    private String major;

    @NotBlank(message = "이메일 필수 입력 값입니다.")
    private String email;

    // 가입하려는 동아리 리스트
    @NotEmpty
    private List<ClubDTO> clubs;

    public ClubMemberTemp toEntity(String encodedPassword,String telephone,int total) {
        return ClubMemberTemp.builder()
                .profileTempAccount(account)
                .profileTempPw(encodedPassword)
                .profileTempName(userName)
                .profileTempHp(telephone)
                .profileTempStudentNumber(studentNumber)
                .profileTempMajor(major)
                .profileTempEmail(email)
                .totalClubRequest(total) // 총 지원한 동아리의 개수
                .clubRequestCount(0) // 0으로 초기화
                .clubExpiryDate(LocalDateTime.now().plusDays(7)) // 요청 마감일 7일후로 설정
                .build();
    }

}

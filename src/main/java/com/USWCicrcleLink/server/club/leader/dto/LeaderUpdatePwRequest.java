package com.USWCicrcleLink.server.club.leader.dto;

import com.USWCicrcleLink.server.global.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LeaderUpdatePwRequest {
	@NotBlank(message = "현재 비밀번호는 필수 입력 값입니다.", groups = ValidationGroups.NotBlankGroup.class)
	private String leaderPw;

	@NotBlank(message = "새 비밀번호는 필수 입력 값입니다.", groups = ValidationGroups.NotBlankGroup.class)
	@Size(min = 8, max = 20, message = "비밀번호는 8~20자 이내여야 합니다.", groups = ValidationGroups.SizeGroup.class)
	@Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?])(?!.*\\s).*$", message = "비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 하며 공백을 포함할 수 없습니다.", groups = ValidationGroups.PatternGroup.class)
	private String newPw;

	@NotBlank(message = "새 비밀번호 확인은 필수 입력 값입니다.", groups = ValidationGroups.NotBlankGroup.class)
	private String confirmNewPw;
}

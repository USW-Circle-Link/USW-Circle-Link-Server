package com.USWCicrcleLink.server.admin.admin.dto;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.Department;
import com.USWCicrcleLink.server.clubLeader.domain.Leader;
import com.USWCicrcleLink.server.global.security.domain.Role;
import com.USWCicrcleLink.server.global.util.validator.InputValidator;
import com.USWCicrcleLink.server.global.validation.ValidationGroups;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClubCreationRequest {

    @NotBlank(message = "아이디는 필수 입력 값입니다.",groups = ValidationGroups.NotBlankGroup.class)
    @Size(min = 5, max = 20, message = "아이디는 5~20자 이내여야 합니다.",groups = ValidationGroups.SizeGroup.class )
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문 대소문자 및 숫자만 가능합니다.",groups = ValidationGroups.PatternGroup.class)
    private String leaderAccount;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.",groups = ValidationGroups.NotBlankGroup.class)
    @Size(min = 5, max = 20, message = "비밀번호는 5~20자 이내여야 합니다.",groups = ValidationGroups.SizeGroup.class)
    @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]+$", message = "비밀번호는 영문 대소문자, 숫자, 특수문자만 포함할 수 있습니다.",groups = ValidationGroups.PatternGroup.class)
    private String leaderPw;

    private String leaderPwConfirm;

    @NotBlank(message = "동아리명은 필수 입력 값입니다.")
    @Size(max = 10, message = "동아리명은 10글자 이내여야 합니다.")
    private String clubName;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "학부는 필수 입력 값입니다.")
    private Department department;

    @NotBlank(message = "운영자 비밀번호는 필수 입력 값입니다.")
    private String adminPw;

    // Club 엔티티 변환 메서드
    public Club toClub() {
        return Club.builder()
                .clubName(InputValidator.sanitizeContent(this.clubName))  // 입력값 검증
                .department(this.department)
                .leaderName("")
                .leaderHp("")
                .clubInsta("")
                .build();
    }

    // Leader 엔티티 변환 메서드
    public Leader toLeader(Club club, PasswordEncoder passwordEncoder) {
        return Leader.builder()
                .leaderAccount(InputValidator.sanitizeContent(this.leaderAccount))  // 입력값 검증
                .leaderPw(passwordEncoder.encode(this.leaderPw))  // 비밀번호 암호화
                .leaderUUID(UUID.randomUUID())  // UUID 생성
                .role(Role.LEADER)
                .club(club)
                .build();
    }
}
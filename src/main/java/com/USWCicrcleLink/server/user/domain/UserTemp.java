package com.USWCicrcleLink.server.user.domain;

import com.USWCicrcleLink.server.global.bucket4j.ClientIdentifier;
import com.USWCicrcleLink.server.global.security.domain.Role;
import com.USWCicrcleLink.server.global.validation.ValidationGroups;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "USER_TEMP_TABLE")
public class UserTemp implements ClientIdentifier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_temp_id")
    private Long userTempId;

    @NotBlank(message = "아이디는 필수 입력 값입니다.",groups = ValidationGroups.NotBlankGroup.class)
    @Size(min = 5, max = 20, message = "아이디는 5~20자 이내여야 합니다.",groups = ValidationGroups.SizeGroup.class )
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문 대소문자 및 숫자만 가능합니다.",groups = ValidationGroups.PatternGroup.class)
    private String tempAccount;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.",groups = ValidationGroups.NotBlankGroup.class)
    private String tempPw;

    private String tempName;

    private String tempStudentNumber;

    private String tempHp;

    private String tempMajor;

    @Column(nullable = false)
    private String tempEmail;

    private boolean isEmailVerified;

    @Override
    public String getClientId() {
        return this.tempEmail;
    }

    public static UserTemp createUserTemp (ClubMemberTemp clubMemberTemp){
        return UserTemp.builder()
                .tempAccount(clubMemberTemp.getProfileTempAccount())
                .tempPw(clubMemberTemp.getProfileTempPw())
                .tempName(clubMemberTemp.getProfileTempName())
                .tempHp(clubMemberTemp.getProfileTempHp())
                .tempMajor(clubMemberTemp.getProfileTempMajor())
                .tempHp(clubMemberTemp.getProfileTempHp())
                .tempStudentNumber(clubMemberTemp.getProfileTempStudentNumber())
                .tempEmail(clubMemberTemp.getProfileTempEmail())
                .build();
    }

}

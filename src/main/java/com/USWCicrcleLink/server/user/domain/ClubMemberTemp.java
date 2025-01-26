package com.USWCicrcleLink.server.user.domain;

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
import java.util.Date;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "CLUBMEMBERTEMP_TABLE")
public class ClubMemberTemp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CLUBMEMBERTEMP_ID")
    private Long id;

    @NotBlank(message = "아이디는 필수 입력 값입니다.",groups = ValidationGroups.NotBlankGroup.class)
    @Size(min = 5, max = 20, message = "아이디는 5~20자 이내여야 합니다.",groups = ValidationGroups.SizeGroup.class )
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문 대소문자 및 숫자만 가능합니다.",groups = ValidationGroups.PatternGroup.class)
    private String profileTempAccount;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.",groups = ValidationGroups.NotBlankGroup.class)
    private String profileTempPw;

    private String profileTempName;

    private String profileTempStudentNumber;

    private String profileTempHp;

    private String profileTempMajor;

    @Column(nullable = false)
    private String profileTempEmail;

    private int totalClubRequest; // 총 지원한 동아리 수
    private int clubRequestCount; // 인증 받은 동아리 수

    private LocalDateTime clubExpiryDate;  // 요청  마감 날짜

}



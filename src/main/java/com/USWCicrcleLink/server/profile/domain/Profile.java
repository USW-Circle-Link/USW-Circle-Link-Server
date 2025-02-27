package com.USWCicrcleLink.server.profile.domain;

import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.ProfileException;
import com.USWCicrcleLink.server.global.validation.ValidationGroups;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.dto.SignUpRequest;
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
@Table(name = "PROFILE_TABLE")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(name = "user_name", nullable = false, length = 30)
    @NotBlank(message = "이름은 필수 입력 값입니다.",groups = ValidationGroups.NotBlankGroup.class)
    @Size(min = 2, max = 30, message = "이름은 2~30자 이내여야 합니다.", groups = ValidationGroups.SizeGroup.class)
    @Pattern(regexp = "^[a-zA-Z가-힣]+$", message = "이름은 영어 또는 한글만 입력 가능합니다.", groups = ValidationGroups.PatternGroup.class)
    private String userName;

    @Column(name = "student_number", nullable = false,length = 8)
    @NotBlank(message = "학번은 필수 입력 값입니다.", groups = ValidationGroups.NotBlankGroup.class)
    @Size(min = 8, max = 8, message = "학번은 8자리 숫자여야 합니다.", groups = ValidationGroups.SizeGroup.class)
    @Pattern(regexp = "^[0-9]{8}$", message = "학번은 숫자만 입력 가능합니다.", groups = ValidationGroups.PatternGroup.class)
    private String studentNumber;

    @Column(name = "user_hp", nullable = false,length = 11)
    @NotBlank(message = "전화번호는 필수 입력 값입니다.",groups = ValidationGroups.NotBlankGroup.class)
    @Size(min = 11, max = 11, message = "전화번호는 11자여야 합니다.", groups = ValidationGroups.SizeGroup.class)
    @Pattern(regexp = "^01[0-9]{9}$", message = "올바른 전화번호를 입력하세요.", groups = ValidationGroups.PatternGroup.class)
    private String userHp;

    @Column(name = "major", nullable = false,length = 20)
    @NotBlank(message = "학과는 필수 입력 값입니다.", groups = ValidationGroups.NotBlankGroup.class)
    @Size(min = 1, max = 20, message = "학과는 1~20자 이내여야 합니다.", groups = ValidationGroups.SizeGroup.class)
    private String major;

    @Column(name = "profile_created_at", nullable = false)
    private LocalDateTime profileCreatedAt;

    @Column(name = "profile_updated_at", nullable = false)
    private LocalDateTime profileUpdatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_type", nullable = false)
    private MemberType memberType;

    @Column(name = "fcm_token")
    private String fcmToken;// 회원이 직접 로그인할 때

    @Column(name = "fcm_token_updated_at")
    private LocalDateTime fcmTokenCertificationTimestamp;

    public static Profile createProfile(User user, SignUpRequest request,String telephone) {
        return Profile.builder()
                .user(user)
                .userName(request.getUserName())
                .studentNumber(request.getStudentNumber())
                .userHp(telephone)
                .major(request.getMajor())
                .profileCreatedAt(LocalDateTime.now())
                .profileUpdatedAt(LocalDateTime.now())
                .memberType(MemberType.REGULARMEMBER) // 기본값: 정회원
                .build();
    }

    public void updateProfile(String userName, String studentNumber, String major, String userHp) {
        if (userName != null) {
            validateProfileInput(userName);
            this.userName = userName;
        }
        if (studentNumber != null) {
            validateProfileInput(studentNumber);
            this.studentNumber = studentNumber;
        }
        if (major != null) {
            validateProfileInput(major);
            this.major = major;
        }
        if (userHp != null) {
            validateProfileInput(userHp);
            this.userHp = userHp;
        }

        this.profileUpdatedAt = LocalDateTime.now();
    }


    private void validateProfileInput(String fieldValue) {
        if (fieldValue == null || fieldValue.trim().isEmpty()) {
            throw new ProfileException(ExceptionType.INVALID_INPUT);
        }
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void updateFcmTokenTime(String fcmToken, LocalDateTime fcmTokenCertificationTimestamp) {
        this.fcmToken = fcmToken;
        this.fcmTokenCertificationTimestamp = fcmTokenCertificationTimestamp;
    }

    public void updateUser(User user) {
        this.user = user;
    }

    public void updateMemberType(MemberType memberType) {
        this.memberType = memberType;
    }
}
package com.USWCicrcleLink.server.email.domain;

import com.USWCicrcleLink.server.user.domain.UserTemp;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "EMAILTOKEN_TABLE")
public class EmailToken {

    // 이메일 토큰 만료 시간 1분
    private static final long EMAIL_TOKEN_CERTIFICATION_TIME_VALUE = 1L;

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(length = 36)
    private UUID emailTokenId;

    // 이메일 토큰과 관련된 임시 회원  id
    @OneToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name="USERTEMP_ID",unique = true)
    private UserTemp userTemp;

    // 이메일 토큰 만료시간
    private LocalDateTime certificationTime;

    // 이메일 토큰 만료 여부
    private boolean isEmailTokenExpired;


    // 이메일 인증 토큰 생성
    public static EmailToken createEmailToken(UserTemp userTemp) {
        return EmailToken.builder()
                .certificationTime(LocalDateTime.now().plusMinutes(EMAIL_TOKEN_CERTIFICATION_TIME_VALUE))
                .isEmailTokenExpired(false)
                .userTemp(userTemp)
                .build();
    }

    //필드 갱신
    public void updateToken(LocalDateTime certificationTime, boolean isEmailTokenExpired) {
        this.certificationTime=certificationTime;
        this.isEmailTokenExpired=isEmailTokenExpired;
    }


    public void useToken(){
        isEmailTokenExpired=true;
    }

    // 토큰 만료 시간 검증
    public boolean isValid() {
        return !LocalDateTime.now().isAfter(certificationTime);
    }

    // 토큰이 만료되었는지 검증 및 처리
    public void validateAndExpire() {
        if (!isValid()) {
            useToken();
            throw new IllegalStateException("이메일 토큰이 만료 되었습니다. 재인증을 요청해주세요");
        }
        useToken();
    }

}

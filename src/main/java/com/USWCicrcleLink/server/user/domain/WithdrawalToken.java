package com.USWCicrcleLink.server.user.domain;

import com.USWCicrcleLink.server.global.bucket4j.ClientIdentifier;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Random;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "WITHDRAWAL_TOKEN")
public class WithdrawalToken implements ClientIdentifier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "WITHDRAWAL_ID")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_uuid", referencedColumnName = "uuid", unique = true)
    private User user;

    private String withdrawalCode;


    public static WithdrawalToken createWithdrawalToken(User user) {
        String authCode = generateRandomAuthCode();
        return WithdrawalToken.builder()
                .user(user)
                .withdrawalCode(authCode)
                .build();
    }

    // 인증 코드 생성
    private static String generateRandomAuthCode() {
        Random r = new Random();
        StringBuilder randomNumber = new StringBuilder();
        for(int i = 0; i < 4; i++) {
            randomNumber.append(r.nextInt(10));
        }
        return randomNumber.toString();
    }

    // 인증 코드 검증
    public boolean isWithdrawalCodeValid(String authCode) {
        return this.withdrawalCode.equals(authCode);
    }

    // 새로운 인증 번호 생성
    public void updateWithdrawalCode(){
        this.withdrawalCode=generateRandomAuthCode();
    }

    @Override
    public String getClientId() {
        String uuid= String.valueOf(this.user.getUserUUID());
        return uuid;
    }
}

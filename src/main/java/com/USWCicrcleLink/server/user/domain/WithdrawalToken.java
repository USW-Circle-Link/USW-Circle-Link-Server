package com.USWCicrcleLink.server.user.domain;

import com.USWCicrcleLink.server.global.bucket4j.ClientIdentifier;
import org.springframework.data.annotation.Id;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.Random;
import java.util.UUID;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash(value = "withdrawalToken",timeToLive = 300)
public class WithdrawalToken implements ClientIdentifier, Serializable {

    @Id
    @Column(name = "userUUID")
    private UUID userUUID;

    @Column(name="withdrawal_code",nullable = false)
    private String withdrawalCode;


    public static WithdrawalToken createWithdrawalToken(User user) {
        String authCode = generateRandomAuthCode();
        return WithdrawalToken.builder()
                .userUUID(user.getUserUUID())
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
        return String.valueOf(this.userUUID);
    }
}

package com.USWCicrcleLink.server.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) //null 값이 있는 필드는 JSON에서 제외
public class SignUpResponse {

    private UUID emailTokenId; // 이메일 토큰의 uuid
    private String email;
    private String message;

    public SignUpResponse(String email, String message) {
        this.email = email;
        this.message = message;
    }
}

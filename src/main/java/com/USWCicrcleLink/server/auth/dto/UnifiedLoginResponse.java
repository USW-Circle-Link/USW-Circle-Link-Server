package com.USWCicrcleLink.server.auth.dto;

import com.USWCicrcleLink.server.global.security.jwt.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnifiedLoginResponse {
    private String accessToken;
    private String refreshToken;
    private Role role;
    private UUID clubuuid;
    private Boolean isAgreedTerms;
}

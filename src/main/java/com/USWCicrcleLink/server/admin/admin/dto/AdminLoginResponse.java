package com.USWCicrcleLink.server.admin.admin.dto;

import com.USWCicrcleLink.server.global.security.jwt.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginResponse {
    private String accessToken;

    private String refreshToken;

    private Role role;
}
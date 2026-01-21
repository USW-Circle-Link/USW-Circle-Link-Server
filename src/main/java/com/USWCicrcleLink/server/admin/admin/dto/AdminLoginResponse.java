package com.USWCicrcleLink.server.admin.admin.dto;

import com.USWCicrcleLink.server.global.security.jwt.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginResponse {
    private String accessToken;

    private String refreshToken;

    private Role role;
}
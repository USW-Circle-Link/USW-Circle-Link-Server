package com.USWCicrcleLink.server.clubLeader.dto;

import com.USWCicrcleLink.server.global.security.jwt.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LeaderLoginResponse {
    private String accessToken;

    private String refreshToken;

    private Role role;

    private UUID clubUUID;

    private Boolean isAgreedTerms;
}
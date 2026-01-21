package com.USWCicrcleLink.server.global.security.jwt.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken implements Serializable {

    private String refreshToken;
    private String uuid;
    private long expirationTime;
}

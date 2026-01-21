package com.USWCicrcleLink.server.global.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * JWT 인증 예외 (토큰 만료, 변조 등)
 */
public class CustomAuthenticationException extends AuthenticationException {

    public CustomAuthenticationException(String errorCode) {
        super(errorCode);
    }
}
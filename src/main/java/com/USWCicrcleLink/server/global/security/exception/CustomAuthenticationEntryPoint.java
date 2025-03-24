package com.USWCicrcleLink.server.global.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String errorCode = "AUTH_REQUIRED";
        String errorMessage = "인증이 필요합니다.";
        int status = HttpServletResponse.SC_UNAUTHORIZED;

        if (authException instanceof CustomAuthenticationException) {
            errorCode = authException.getMessage();

            if ("TOKEN_EXPIRED".equals(errorCode)) {
                errorMessage = "토큰이 만료되었습니다.";
            } else if ("INVALID_TOKEN".equals(errorCode)) {
                errorMessage = "인증이 필요합니다.";
            }
        }

        response.setStatus(status);
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", status);
        responseBody.put("errorCode", errorCode);
        responseBody.put("message", errorMessage);

        response.getWriter().write(objectMapper.writeValueAsString(responseBody));
    }
}
package com.USWCicrcleLink.server.global.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
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
    private final String activeProfile;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper, Environment environment) {
        this.objectMapper = objectMapper;
        this.activeProfile = environment.getProperty("spring.profiles.active", "dev");
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

                if ("prod".equals(activeProfile)) {
                    log.error("[SECURITY ALERT] 변조된 JWT 감지 - IP: {} | 요청 경로: {}",
                            request.getRemoteAddr(), request.getRequestURI());
                } else {
                    log.warn("[JWT WARNING] 변조된 JWT 감지 - IP: {} | 요청 경로: {}",
                            request.getRemoteAddr(), request.getRequestURI());
                }
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
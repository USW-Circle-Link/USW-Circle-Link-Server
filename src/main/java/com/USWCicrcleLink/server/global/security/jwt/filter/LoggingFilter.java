package com.USWCicrcleLink.server.global.security.jwt.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 특정 API 요청에서만 사용자 정보를 로깅하는 필터
 */
@Slf4j
@RequiredArgsConstructor
public class LoggingFilter extends OncePerRequestFilter {

    private final List<String> loggingPaths;
    private final List<String> loggingMethods;
    private final PathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        String requestMethod = request.getMethod();

        // 특정 API 요청에서만 로그 출력
        if (isLoggingPath(requestPath) && isLoggingMethod(requestMethod)) {
            log.info("[{}: {}] {} 요청 경로: {}",
                    MDC.get("userType"),  // Admin, User, Leader 구분
                    MDC.get("userUUID"),  // 사용자 UUID
                    requestMethod,        // 요청 메서드
                    requestPath);         // 요청 경로
        }


        filterChain.doFilter(request, response);
    }

    private boolean isLoggingPath(String requestPath) {
        return loggingPaths.stream().anyMatch(logPath -> pathMatcher.match(logPath, requestPath));
    }

    private boolean isLoggingMethod(String requestMethod) {
        return loggingMethods.contains(requestMethod);
    }
}

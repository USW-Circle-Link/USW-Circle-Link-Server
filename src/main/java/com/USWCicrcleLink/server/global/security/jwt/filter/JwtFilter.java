package com.USWCicrcleLink.server.global.security.jwt.filter;

import com.USWCicrcleLink.server.global.security.details.CustomAdminDetails;
import com.USWCicrcleLink.server.global.security.details.CustomLeaderDetails;
import com.USWCicrcleLink.server.global.security.details.CustomUserDetails;
import com.USWCicrcleLink.server.global.security.exception.CustomAuthenticationEntryPoint;
import com.USWCicrcleLink.server.global.security.exception.CustomAuthenticationException;
import com.USWCicrcleLink.server.global.security.jwt.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import static com.USWCicrcleLink.server.global.util.IpUtil.getClientIp;

/**
 * JWT 유효성 검증 필터 (User, Admin, Leader UUID 처리)
 */
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final List<String> permitAllPaths;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final PathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        if (isPermitAllPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = jwtProvider.resolveAccessToken(request);

        try {
            jwtProvider.validateAccessToken(accessToken);

            Authentication auth = jwtProvider.getAuthentication(accessToken);
            setMDCUserDetails(auth);
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);

        } catch (CustomAuthenticationException e) {
            if ("INVALID_TOKEN".equals(e.getMessage())) {
                log.error("[SECURITY ALERT] 변조된 토큰 감지 | API: {} {} | IP: {} | Token: {}",
                        request.getMethod(), request.getRequestURI(), getClientIp(request), maskToken(accessToken));
            }
            SecurityContextHolder.clearContext();
            customAuthenticationEntryPoint.commence(request, response, e);
        } finally {
            MDC.clear();
        }
    }

    /**
     * JWT 토큰 마스킹 (앞 10자만 노출)
     */
    private String maskToken(String token) {
        if (token.length() <= 10) {
            return token;
        }
        return token.substring(0, 10) + "...";
    }

    /**
     * MDC(User Type, UUID) 설정
     */
    private void setMDCUserDetails(Authentication auth) {
        if (auth.getPrincipal() instanceof CustomAdminDetails adminDetails) {
            MDC.put("userType", "Admin");
            MDC.put("userUUID", adminDetails.getAdminUUID().toString());
        } else if (auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            MDC.put("userType", "User");
            MDC.put("userUUID", userDetails.getUserUUID().toString());
        } else if (auth.getPrincipal() instanceof CustomLeaderDetails leaderDetails) {
            MDC.put("userType", "Leader");
            MDC.put("userUUID", leaderDetails.getLeaderUUID().toString());
        } else {
            MDC.put("userType", "Unknown");
            MDC.put("userUUID", "Unknown");
        }
    }

    /**
     * 인증이 필요 없는 경로인지 확인
     */
    private boolean isPermitAllPath(String requestPath) {
        return permitAllPaths.stream().anyMatch(permitPath -> pathMatcher.match(permitPath, requestPath));
    }
}

package com.USWCicrcleLink.server.global.security.jwt.filter;

import com.USWCicrcleLink.server.global.security.details.CustomAdminDetails;
import com.USWCicrcleLink.server.global.security.details.CustomLeaderDetails;
import com.USWCicrcleLink.server.global.security.details.CustomUserDetails;
import com.USWCicrcleLink.server.global.security.exception.CustomAuthenticationEntryPoint;
import com.USWCicrcleLink.server.global.security.exception.CustomAuthenticationException;
import com.USWCicrcleLink.server.global.security.jwt.JwtProvider;
import com.USWCicrcleLink.server.global.security.jwt.domain.TokenValidationResult;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

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
            if (accessToken == null) {
                throw new CustomAuthenticationException("INVALID_TOKEN");
            }

            TokenValidationResult tokenValidationResult = jwtProvider.validateAccessToken(accessToken);

            switch (tokenValidationResult) {
                case EXPIRED -> throw new CustomAuthenticationException("TOKEN_EXPIRED");
                case INVALID -> throw new CustomAuthenticationException("INVALID_TOKEN");
                case VALID -> {
                    Authentication auth = jwtProvider.getAuthentication(accessToken);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    setMDCUserDetails(auth, request.getMethod(), request.getRequestURI());
                    filterChain.doFilter(request, response);
                }
            }
        } catch (AuthenticationException e) {
            SecurityContextHolder.clearContext();
            customAuthenticationEntryPoint.commence(request, response, e);
        } finally {
            MDC.clear();
        }
    }

    /**
     * MDC(User Type, UUID) 설정
     */
    private void setMDCUserDetails(Authentication auth, String method, String path) {
        if (auth.getPrincipal() instanceof CustomAdminDetails adminDetails) {
            MDC.put("userType", "Admin");
            MDC.put("userUUID", adminDetails.getAdminUUID().toString());
        } else if (auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            MDC.put("userType", "User");
            MDC.put("userUUID", userDetails.getUserUUID().toString());
        } else if (auth.getPrincipal() instanceof CustomLeaderDetails leaderDetails) {
            MDC.put("userType", "Leader");
            MDC.put("userUUID", leaderDetails.getLeaderUUID().toString());
        }

        if (log.isDebugEnabled()) {
            log.info("[{}: {}] {} 요청 경로: {}", MDC.get("userType"), MDC.get("userUUID"), method, path);
        }
    }

    /**
     * 인증이 필요 없는 경로인지 확인
     */
    private boolean isPermitAllPath(String requestPath) {
        return permitAllPaths.stream().anyMatch(permitPath -> pathMatcher.match(permitPath, requestPath));
    }
}
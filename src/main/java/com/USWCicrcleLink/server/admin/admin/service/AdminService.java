package com.USWCicrcleLink.server.admin.admin.service;

import com.USWCicrcleLink.server.admin.admin.dto.AdminLoginRequest;
import com.USWCicrcleLink.server.admin.admin.dto.AdminLoginResponse;
import com.USWCicrcleLink.server.global.Integration.domain.LoginType;
import com.USWCicrcleLink.server.global.bucket4j.RateLimite;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.global.security.domain.Role;
import com.USWCicrcleLink.server.global.security.service.CustomUserDetailsService;
import com.USWCicrcleLink.server.global.security.util.JwtProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AdminService {
    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // 운영팀 로그인
    @RateLimite(action = "WEB_LOGIN")
    public AdminLoginResponse adminLogin(AdminLoginRequest request, HttpServletResponse response) {
        log.debug("로그인 요청: {}, 사용자 유형: {}", request.getAdminAccount(), request.getLoginType());

        Role role = getRoleFromLoginType(request.getLoginType());
        UserDetails userDetails;

        try {
            userDetails = customUserDetailsService.loadUserByAccountAndRole(request.getAdminAccount(), role);
        } catch (UserException e) {
            // 아이디가 존재하지 않는 경우
            throw new UserException(ExceptionType.USER_AUTHENTICATION_FAILED);
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getAdminPw(), userDetails.getPassword())) {
            throw new UserException(ExceptionType.USER_AUTHENTICATION_FAILED);
        }

        // 토큰 생성
        String accessToken = jwtProvider.createAccessToken(userDetails.getUsername(), response);
        String refreshToken = jwtProvider.createRefreshToken(userDetails.getUsername(), response);

        log.debug("로그인 성공, uuid: {}", userDetails.getUsername());
        return new AdminLoginResponse(accessToken, refreshToken, role);
    }

    // 로그인 타입
    private Role getRoleFromLoginType(LoginType loginType) {
        return switch (loginType) {
            case LEADER -> Role.LEADER;
            case ADMIN -> Role.ADMIN;
        };
    }
}

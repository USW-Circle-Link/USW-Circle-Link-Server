package com.USWCicrcleLink.server.auth.service;

import com.USWCicrcleLink.server.global.bucket4j.RateLimite;
import com.USWCicrcleLink.server.global.exception.errortype.TokenException;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.global.exception.errortype.ProfileException;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.auth.dto.UnifiedLoginRequest;
import com.USWCicrcleLink.server.auth.dto.UnifiedLoginResponse;
import com.USWCicrcleLink.server.global.security.jwt.JwtProvider;
import com.USWCicrcleLink.server.global.security.jwt.dto.TokenDto;
import com.USWCicrcleLink.server.global.security.jwt.domain.Role;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.profile.domain.Profile;
import com.USWCicrcleLink.server.user.repository.UserRepository;

import com.USWCicrcleLink.server.user.profile.repository.ProfileRepository;
import com.USWCicrcleLink.server.club.leader.domain.Leader;
import com.USWCicrcleLink.server.club.leader.repository.LeaderRepository;
import com.USWCicrcleLink.server.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.repository.AdminRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final JwtProvider jwtProvider;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    private final LeaderRepository leaderRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int FCM_TOKEN_CERTIFICATION_TIME = 60;

    /**
     * 통합 로그인 (user_table 기반 단일 인증)
     */
    @RateLimite(action = "USER_LOGIN")
    public UnifiedLoginResponse unifiedLogin(UnifiedLoginRequest request, HttpServletResponse response) {
        // 1. user_table에서 계정 조회 (Role 확인용)
        User user = userRepository.findByUserAccount(request.getAccount())
                .orElseThrow(() -> {
                    log.error("로그인 실패: User 테이블에 계정 없음 - inputAccount: {}", request.getAccount());
                    return new UserException(ExceptionType.USER_AUTHENTICATION_FAILED);
                });

        UUID userUUID = user.getUserUUID();
        String accessToken;
        String refreshToken;

        // 2. Role에 따라 분기 및 비밀번호 검증
        switch (user.getRole()) {
            case USER:
                // User는 user_table 비밀번호 사용
                if (!passwordEncoder.matches(request.getPassword(), user.getUserPw())) {
                    log.error("로그인 실패(USER): 비밀번호 불일치 - Account: {}", user.getUserAccount());
                    throw new UserException(ExceptionType.USER_AUTHENTICATION_FAILED);
                }

                Profile profile = profileRepository.findByUser_UserUUID(userUUID)
                        .orElseThrow(() -> new ProfileException(ExceptionType.PROFILE_NOT_EXISTS));

                // FCM 토큰 업데이트
                if (request.getFcmToken() != null && !request.getFcmToken().isEmpty()) {
                    profile.updateFcmTokenTime(request.getFcmToken(),
                            LocalDateTime.now().plusDays(FCM_TOKEN_CERTIFICATION_TIME));
                    profileRepository.save(profile);
                    log.debug("FCM 토큰 업데이트 완료: {}", user.getUserAccount());
                }

                accessToken = jwtProvider.createAccessToken(userUUID, user.getRole(), response);
                refreshToken = jwtProvider.createRefreshToken(userUUID, response);

                log.debug("User 로그인 성공, UUID: {}", userUUID);
                return UnifiedLoginResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .role(Role.USER)
                        .build();

            case LEADER:
                // Leader 테이블에서 Leader 조회 및 비밀번호 검증
                Leader leader = leaderRepository.findByLeaderAccount(user.getUserAccount())
                        .orElseThrow(() -> {
                            log.error("로그인 실패(LEADER): Leader 테이블 계정 없음 - Account: {}", user.getUserAccount());
                            return new UserException(ExceptionType.USER_NOT_EXISTS);
                        });

                if (!passwordEncoder.matches(request.getPassword(), leader.getLeaderPw())) {
                    log.error("로그인 실패(LEADER): 비밀번호 불일치 - Account: {}", user.getUserAccount());
                    throw new UserException(ExceptionType.USER_AUTHENTICATION_FAILED);
                }

                UUID leaderUUID = leader.getLeaderUUID();
                UUID clubuuid = leaderRepository.findClubuuidByLeaderUUID(leaderUUID)
                        .orElseThrow(() -> {
                            log.error("로그인 실패(LEADER): ClubUUID 없음 - LeaderUUID: {}", leaderUUID);
                            return new UserException(ExceptionType.USER_NOT_EXISTS);
                        });

                log.debug("Leader 로그인 성공 - leaderUUID: {}, 클럽 UUID: {}", leaderUUID, clubuuid);

                accessToken = jwtProvider.createAccessToken(leaderUUID, user.getRole(), response);
                refreshToken = jwtProvider.createRefreshToken(leaderUUID, response);

                return UnifiedLoginResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .role(Role.LEADER)
                        .clubuuid(clubuuid)
                        .isAgreedTerms(leader.isAgreedTerms())
                        .build();

            case ADMIN:
                // Admin 테이블에서 Admin 조회 및 비밀번호 검증
                Admin admin = adminRepository.findByAdminAccount(user.getUserAccount())
                        .orElseThrow(() -> {
                            log.error("로그인 실패(ADMIN): Admin 테이블 계정 없음 - Account: {}", user.getUserAccount());
                            return new UserException(ExceptionType.USER_NOT_EXISTS);
                        });

                if (!passwordEncoder.matches(request.getPassword(), admin.getAdminPw())) {
                    log.error("로그인 실패(ADMIN): 비밀번호 불일치 - Account: {}", user.getUserAccount());
                    throw new UserException(ExceptionType.USER_AUTHENTICATION_FAILED);
                }

                UUID adminUUID = admin.getAdminUUID();

                accessToken = jwtProvider.createAccessToken(adminUUID, user.getRole(), response);
                refreshToken = jwtProvider.createRefreshToken(adminUUID, response);

                log.debug("Admin 로그인 성공 - adminUUID: {}", adminUUID);
                return UnifiedLoginResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .role(Role.ADMIN)
                        .build();

            default:
                log.error("로그인 실패: 알 수 없는 Role - {}", user.getRole());
                throw new UserException(ExceptionType.USER_AUTHENTICATION_FAILED);
        }
    }

    /**
     * 동아리 회장 로그인 (삭제됨 - unifiedLogin 사용)
     */
    // leaderLogin 메서드 삭제 완료

    /**
     * 로그아웃 (User, Admin & Leader 통합)
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtProvider.resolveRefreshToken(request);

        if (refreshToken != null) {
            try {
                jwtProvider.validateRefreshToken(refreshToken, request);
                UUID userUUID = jwtProvider.getUUIDFromRefreshToken(refreshToken);

                // 유저라면 FCM 토큰 삭제 (푸시 알림 무효화)
                profileRepository.findByUser_UserUUID(userUUID).ifPresent(profile -> {
                    profile.updateFcmToken(null);
                    profileRepository.save(profile);
                    log.debug("User 로그아웃 - FCM 토큰 삭제 완료 - UUID: {}", userUUID);
                });

                jwtProvider.deleteRefreshToken(userUUID);
            } catch (TokenException ignored) {
            }
        }

        SecurityContextHolder.clearContext();
        jwtProvider.deleteRefreshTokenCookie(response);
        log.debug("클라이언트 쿠키에서 리프레시 토큰 삭제 완료");
        log.debug("로그아웃 완료");
    }

    /**
     * 토큰 갱신 (User, Admin & Leader 통합)
     */
    public TokenDto refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtProvider.resolveRefreshToken(request);

        if (refreshToken == null) {
            logout(request, response);
            return null;
        }

        try {
            jwtProvider.validateRefreshToken(refreshToken, request);
            UUID uuid = jwtProvider.getUUIDFromRefreshToken(refreshToken);

            // UUID로 유저와 권한 조회 (Access Token 생성에 필요)
            User user = userRepository.findByUserUUID(uuid)
                    .orElseThrow(() -> new UserException(ExceptionType.USER_NOT_EXISTS));

            jwtProvider.deleteRefreshToken(uuid);

            String newAccessToken = jwtProvider.createAccessToken(uuid, user.getRole(), response);
            String newRefreshToken = jwtProvider.createRefreshToken(uuid, response);

            log.debug("토큰 갱신 성공 - UUID: {}", uuid);
            return new TokenDto(newAccessToken, newRefreshToken);
        } catch (TokenException e) {
            logout(request, response);
            return null;
        }
    }
}

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
import com.USWCicrcleLink.server.user.domain.ExistingMember.ClubMemberTemp;
import com.USWCicrcleLink.server.user.profile.domain.Profile;
import com.USWCicrcleLink.server.user.repository.UserRepository;
import com.USWCicrcleLink.server.user.repository.ClubMemberTempRepository;
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
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final JwtProvider jwtProvider;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ClubMemberTempRepository clubMemberTempRepository;
    private final LeaderRepository leaderRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int FCM_TOKEN_CERTIFICATION_TIME = 60;

    /**
     * 유저 로그인
     */
    @RateLimite(action = "USER_LOGIN")
    public UnifiedLoginResponse userLogin(UnifiedLoginRequest request, HttpServletResponse response) {
        User user = userRepository.findByUserAccount(request.getAccount()).orElse(null);

        // 유저 객체가 존재하는지 확인
        if (user == null) {
            log.warn("Login Failed: User not found with account: {}", request.getAccount()); // 로그 추가
            // 기존 회원 가입 요청을 보낸 사람인지 확인(비회원 확인)
            Optional<ClubMemberTemp> clubMemberTemp = clubMemberTempRepository
                    .findByProfileTempAccount(request.getAccount());
            if (clubMemberTemp.isPresent()
                    && passwordEncoder.matches(request.getPassword(), clubMemberTemp.get().getProfileTempPw())) {
                throw new UserException(ExceptionType.USER_NONMEMBER);
            } else { // 제3자의 요청인 경우
                throw new UserException(ExceptionType.THIRD_PARTY_LOGIN_ATTEMPT);
            }
        }

        // 아이디 비밀번호 일치 불일치 여부 확인
        boolean matches = passwordEncoder.matches(request.getPassword(), user.getUserPw());
        log.info("Login Debug - Account: {}, User Found: true, Password Match: {}", request.getAccount(), matches); // 로그
                                                                                                                    // 추가

        if (!matches) {
            throw new UserException(ExceptionType.USER_AUTHENTICATION_FAILED);
        }

        UUID userUUID = user.getUserUUID();
        Profile profile = profileRepository.findByUser_UserUUID(userUUID)
                .orElseThrow(() -> new ProfileException(ExceptionType.PROFILE_NOT_EXISTS));

        log.debug("프로필 조회 성공 - 사용자 UUID: {}, 회원 타입: {}", userUUID, profile.getMemberType());

        String accessToken = jwtProvider.createAccessToken(userUUID, response);
        String refreshToken = jwtProvider.createRefreshToken(userUUID, response);

        // FCM 토큰 업데이트
        if (request.getFcmToken() != null && !request.getFcmToken().isEmpty()) {
            profile.updateFcmTokenTime(request.getFcmToken(),
                    LocalDateTime.now().plusDays(FCM_TOKEN_CERTIFICATION_TIME));
            profileRepository.save(profile);
            log.debug("FCM 토큰 업데이트 완료: {}", user.getUserAccount());
        }

        log.debug("User 로그인 성공, UUID: {}", userUUID);
        return UnifiedLoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(Role.USER)
                .build();
    }

    /**
     * 동아리 회장 로그인
     */
    @RateLimite(action = "WEB_LOGIN")
    public UnifiedLoginResponse leaderLogin(UnifiedLoginRequest request, HttpServletResponse response) {
        Leader leader = leaderRepository.findByLeaderAccount(request.getAccount())
                .orElseThrow(() -> new UserException(ExceptionType.USER_AUTHENTICATION_FAILED));

        if (!passwordEncoder.matches(request.getPassword(), leader.getLeaderPw())) {
            throw new UserException(ExceptionType.USER_AUTHENTICATION_FAILED);
        }

        UUID clubuuid = leaderRepository.findClubuuidByLeaderUUID(leader.getLeaderUUID())
                .orElseThrow(() -> new UserException(ExceptionType.USER_NOT_EXISTS));

        UUID leaderUUID = leader.getLeaderUUID();
        String accessToken = jwtProvider.createAccessToken(leaderUUID, response);
        String refreshToken = jwtProvider.createRefreshToken(leaderUUID, response);

        log.debug("Leader 로그인 성공 - uuid: {}, 클럽 UUID: {}", leaderUUID, clubuuid);
        return UnifiedLoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(Role.LEADER)
                .clubuuid(clubuuid) // Changed from .clubUUID(clubUUID) to .clubuuid(clubUUID)
                .isAgreedTerms(leader.isAgreedTerms())
                .build();
    }

    /**
     * 관리자 로그인
     */
    @RateLimite(action = "WEB_LOGIN")
    public UnifiedLoginResponse adminLogin(UnifiedLoginRequest request, HttpServletResponse response) {
        Admin admin = adminRepository.findByAdminAccount(request.getAccount())
                .orElseThrow(() -> new UserException(ExceptionType.USER_AUTHENTICATION_FAILED));

        if (!passwordEncoder.matches(request.getPassword(), admin.getAdminPw())) {
            throw new UserException(ExceptionType.USER_AUTHENTICATION_FAILED);
        }

        UUID adminUUID = admin.getAdminUUID();
        String accessToken = jwtProvider.createAccessToken(adminUUID, response);
        String refreshToken = jwtProvider.createRefreshToken(adminUUID, response);

        log.debug("Admin 로그인 성공 - uuid: {}", adminUUID);
        return UnifiedLoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(Role.ADMIN)
                .build();
    }

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

            jwtProvider.deleteRefreshToken(uuid);

            String newAccessToken = jwtProvider.createAccessToken(uuid, response);
            String newRefreshToken = jwtProvider.createRefreshToken(uuid, response);

            log.debug("토큰 갱신 성공 - UUID: {}", uuid);
            return new TokenDto(newAccessToken, newRefreshToken);
        } catch (TokenException e) {
            logout(request, response);
            return null;
        }
    }
}

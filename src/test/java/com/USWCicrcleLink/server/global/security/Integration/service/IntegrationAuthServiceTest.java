package com.USWCicrcleLink.server.global.security.Integration.service;

import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.TokenException;
import com.USWCicrcleLink.server.global.security.jwt.JwtProvider;
import com.USWCicrcleLink.server.global.security.jwt.dto.TokenDto;
import com.USWCicrcleLink.server.profile.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.profile.repository.ProfileRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class IntegrationAuthServiceTest {

    @Mock
    JwtProvider jwtProvider;
    @Mock
    ProfileRepository profileRepository;
    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;

    @InjectMocks
    IntegrationAuthService integrationAuthService;

    @Nested
    @DisplayName("logout 메서드")
    class LogoutTest {
        @Test
        @DisplayName("refreshToken 있고, 정상 동작 시 로그아웃 처리/FCM 토큰 삭제/쿠키 삭제/토큰 삭제 모두 수행")
        void logout_refreshToken정상_유저존재() {
            // given
            String refreshToken = "test-refresh-token";
            UUID userUuid = UUID.randomUUID();
            Profile profile = mock(Profile.class);

            given(jwtProvider.resolveRefreshToken(request)).willReturn(refreshToken);
            willDoNothing().given(jwtProvider).validateRefreshToken(refreshToken, request);
            given(jwtProvider.getUUIDFromRefreshToken(refreshToken)).willReturn(userUuid);
            given(profileRepository.findByUser_UserUUID(userUuid)).willReturn(Optional.of(profile));
            given(profileRepository.save(profile)).willReturn(profile);

            // when
            integrationAuthService.logout(request, response);

            // then
            verify(profile).updateFcmToken(null);
            verify(profileRepository).save(profile);
            verify(jwtProvider).deleteRefreshToken(userUuid);
            verify(jwtProvider).deleteRefreshTokenCookie(response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("refreshToken이 없는 경우에도 쿠키 삭제/컨텍스트 초기화만 수행")
        void logout_refreshToken없음() {
            // given
            given(jwtProvider.resolveRefreshToken(request)).willReturn(null);

            // when
            integrationAuthService.logout(request, response);

            // then
            verify(jwtProvider, never()).validateRefreshToken(anyString(), any());
            verify(jwtProvider, never()).getUUIDFromRefreshToken(anyString());
            verify(jwtProvider, never()).deleteRefreshToken(any());
            verify(jwtProvider).deleteRefreshTokenCookie(response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("refreshToken이 있으나 TokenException 발생시 쿠키 삭제/컨텍스트 초기화만 수행")
        void logout_refreshToken_예외() {
            // given
            String refreshToken = "test-refresh-token";
            given(jwtProvider.resolveRefreshToken(request)).willReturn(refreshToken);
            willThrow(new TokenException(ExceptionType.INVALID_TOKEN)).given(jwtProvider).validateRefreshToken(refreshToken, request);

            // when
            integrationAuthService.logout(request, response);

            // then
            verify(jwtProvider, never()).getUUIDFromRefreshToken(anyString());
            verify(jwtProvider, never()).deleteRefreshToken(any());
            verify(profileRepository, never()).findByUser_UserUUID(any());
            verify(jwtProvider).deleteRefreshTokenCookie(response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }

    @Nested
    @DisplayName("refreshToken 메서드")
    class RefreshTokenTest {
        @Test
        @DisplayName("refreshToken 정상 -> 기존 리프레시 삭제, 새 토큰 발급, TokenDto 반환")
        void refreshToken_정상() {
            // given
            String refreshToken = "test-refresh-token";
            UUID userUuid = UUID.randomUUID();
            String newAccessToken = "access-token";
            String newRefreshToken = "refresh-token";

            given(jwtProvider.resolveRefreshToken(request)).willReturn(refreshToken);
            willDoNothing().given(jwtProvider).validateRefreshToken(refreshToken, request);
            given(jwtProvider.getUUIDFromRefreshToken(refreshToken)).willReturn(userUuid);
            willDoNothing().given(jwtProvider).deleteRefreshToken(userUuid);
            given(jwtProvider.createAccessToken(userUuid, response)).willReturn(newAccessToken);
            given(jwtProvider.createRefreshToken(userUuid, response)).willReturn(newRefreshToken);

            // when
            TokenDto result = integrationAuthService.refreshToken(request, response);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo(newAccessToken);
            assertThat(result.getRefreshToken()).isEqualTo(newRefreshToken);

            // 메서드 호출 순서까지 검증 (optional)
            InOrder inOrder = inOrder(jwtProvider);
            inOrder.verify(jwtProvider).resolveRefreshToken(request);
            inOrder.verify(jwtProvider).validateRefreshToken(refreshToken, request);
            inOrder.verify(jwtProvider).getUUIDFromRefreshToken(refreshToken);
            inOrder.verify(jwtProvider).deleteRefreshToken(userUuid);
            inOrder.verify(jwtProvider).createAccessToken(userUuid, response);
            inOrder.verify(jwtProvider).createRefreshToken(userUuid, response);
        }

        @Test
        @DisplayName("refreshToken이 없는 경우 로그아웃 수행 및 null 반환")
        void refreshToken_토큰없음() {
            // given
            given(jwtProvider.resolveRefreshToken(request)).willReturn(null);

            // when
            TokenDto result = integrationAuthService.refreshToken(request, response);

            // then
            assertThat(result).isNull();
            verify(jwtProvider).deleteRefreshTokenCookie(response);
        }

        @Test
        @DisplayName("refreshToken 검증/파싱 중 예외 발생시 로그아웃 및 null 반환")
        void refreshToken_토큰에러() {
            // given
            String refreshToken = "test-refresh-token";
            given(jwtProvider.resolveRefreshToken(request)).willReturn(refreshToken);
            willThrow(new TokenException(ExceptionType.INVALID_TOKEN)).given(jwtProvider).validateRefreshToken(refreshToken, request);

            // when
            TokenDto result = integrationAuthService.refreshToken(request, response);

            // then
            assertThat(result).isNull();
            verify(jwtProvider).deleteRefreshTokenCookie(response);
        }
    }
}

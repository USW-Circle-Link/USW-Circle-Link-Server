package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.global.exception.errortype.AuthCodeException;
import com.USWCicrcleLink.server.user.domain.AuthToken;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.dto.AuthCodeRequest;
import com.USWCicrcleLink.server.user.repository.AuthTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthTokenServiceTest {

    @Mock
    private AuthTokenRepository authTokenRepository;
    private AuthTokenService authTokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authTokenService = new AuthTokenService(authTokenRepository);
    }

    @Test
    @DisplayName("인증 토큰이 없을 경우 새로 생성되는지 테스트")
    void createAuthToken_whenNotExists_createsNew() {
        User user = mock(User.class);
        when(user.getUserUUID()).thenReturn(UUID.randomUUID());

        when(authTokenRepository.findByUserUUID(user.getUserUUID())).thenReturn(Optional.empty());

        AuthToken mockToken = mock(AuthToken.class);
        when(authTokenRepository.save(any())).thenReturn(mockToken);

        AuthToken result = authTokenService.createOrUpdateAuthToken(user);

        assertThat(result).isEqualTo(mockToken);
        verify(authTokenRepository).save(any(AuthToken.class));
    }

    @Test
    @DisplayName("인증 토큰이 있을 경우 업데이트되는지 테스트")
    void createAuthToken_whenExists_updatesExisting() {
        User user = mock(User.class);
        UUID uuid = UUID.randomUUID();
        when(user.getUserUUID()).thenReturn(uuid);

        AuthToken existing = mock(AuthToken.class);
        when(authTokenRepository.findByUserUUID(uuid)).thenReturn(Optional.of(existing));
        when(authTokenRepository.save(existing)).thenReturn(existing);

        AuthToken result = authTokenService.createOrUpdateAuthToken(user);

        assertThat(result).isEqualTo(existing);
        verify(existing).updateAuthCode();
    }

    @Test
    @DisplayName("인증 코드 검증 성공 테스트")
    void verifyAuthToken_validCode_passes() {
        UUID uuid = UUID.randomUUID();
        AuthToken token = mock(AuthToken.class);
        AuthCodeRequest request = mock(AuthCodeRequest.class);

        when(authTokenRepository.findByUserUUID(uuid)).thenReturn(Optional.of(token));
        when(request.getAuthCode()).thenReturn("123456");
        when(token.isAuthCodeValid("123456")).thenReturn(true);

        authTokenService.verifyAuthToken(uuid, request);

        verify(token).isAuthCodeValid("123456");
    }

    /*@Test
    @DisplayName("인증 코드가 틀렸을 때 예외 테스트")
    void verifyAuthToken_invalidCode_throws() {
        UUID uuid = UUID.randomUUID();
        AuthToken token = mock(AuthToken.class);
        AuthCodeRequest request = mock(AuthCodeRequest.class);

        when(authTokenRepository.findByUserUUID(uuid)).thenReturn(Optional.of(token));
        when(request.getAuthCode()).thenReturn("000000");
        when(token.isAuthCodeValid("000000")).thenReturn(false);

        Throwable thrown = catchThrowable(() -> authTokenService.verifyAuthToken(uuid, request));
        assertThat(thrown)
                .isInstanceOf(AuthCodeException.class)
                .hasMessageContaining("인증번호가 일치하지 않습니다");
    }*/



    @Test
    @DisplayName("인증 토큰이 없을 때 예외 테스트")
    void verifyAuthToken_noToken_throws() {
        UUID uuid = UUID.randomUUID();
        when(authTokenRepository.findByUserUUID(uuid)).thenReturn(Optional.empty());

        AuthCodeRequest request = mock(AuthCodeRequest.class);

        assertThatThrownBy(() -> authTokenService.verifyAuthToken(uuid, request))
                .isInstanceOf(AuthCodeException.class)
                .hasMessageContaining("인증 코드 토큰이 존재하지 않습니다");
    }

    @Test
    @DisplayName("검증된 인증 토큰 삭제 테스트")
    void deleteAuthToken_deletesExistingToken() {
        UUID uuid = UUID.randomUUID();
        AuthToken token = mock(AuthToken.class);

        when(authTokenRepository.findByUserUUID(uuid)).thenReturn(Optional.of(token));

        authTokenService.deleteAuthToken(uuid);

        verify(authTokenRepository).delete(token);
    }

    @Test
    @DisplayName("회원 탈퇴시 인증 토큰 삭제 테스트")
    void delete_authTokenIfPresent() {
        UUID uuid = UUID.randomUUID();
        AuthToken token = mock(AuthToken.class);

        when(authTokenRepository.findByUserUUID(uuid)).thenReturn(Optional.of(token));

        authTokenService.delete(uuid);

        verify(authTokenRepository).delete(token);
    }
}

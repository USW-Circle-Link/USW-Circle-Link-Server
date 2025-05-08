package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.global.exception.errortype.WithdrawalTokenException;
import com.USWCicrcleLink.server.global.security.context.AuthContext;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.domain.WithdrawalToken;
import com.USWCicrcleLink.server.user.dto.AuthCodeRequest;
import com.USWCicrcleLink.server.user.repository.WithdrawalTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WithdrawalTokenServiceTest {

    @Mock private WithdrawalTokenRepository withdrawalTokenRepository;
    @Mock private AuthContext authContext;

    @InjectMocks private WithdrawalTokenService withdrawalTokenService;

    private User user;
    private UUID uuid;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        user = mock(User.class);
        uuid = UUID.randomUUID();
        when(user.getUserUUID()).thenReturn(uuid);
        when(authContext.getUserByAuth()).thenReturn(user);
    }

    @Test
    @DisplayName("기존 탈퇴 토큰이 존재할 때 업데이트")
    void createOrUpdateWithdrawalToken_existingToken() {
        WithdrawalToken token = mock(WithdrawalToken.class);
        when(withdrawalTokenRepository.findByUserUUID(uuid)).thenReturn(Optional.of(token));
        when(withdrawalTokenRepository.save(token)).thenReturn(token);

        WithdrawalToken result = withdrawalTokenService.createOrUpdateWithdrawalToken();

        verify(token).updateWithdrawalCode();
        verify(withdrawalTokenRepository).save(token);
        assertEquals(token, result);
    }

    @Test
    @DisplayName("탈퇴 토큰이 존재하지 않을 때 새로 생성")
    void createOrUpdateWithdrawalToken_newToken() {
        when(withdrawalTokenRepository.findByUserUUID(uuid)).thenReturn(Optional.empty());

        WithdrawalToken token = mock(WithdrawalToken.class);
        mockStatic(WithdrawalToken.class).when(() -> WithdrawalToken.createWithdrawalToken(user)).thenReturn(token);
        when(withdrawalTokenRepository.save(token)).thenReturn(token);

        WithdrawalToken result = withdrawalTokenService.createOrUpdateWithdrawalToken();
        verify(withdrawalTokenRepository).save(token);
        assertEquals(token, result);
    }

    @Test
    @DisplayName("탈퇴 코드 인증 성공")
    void verifyWithdrawalToken_success() {
        AuthCodeRequest request = new AuthCodeRequest("123456");
        WithdrawalToken token = mock(WithdrawalToken.class);

        when(withdrawalTokenRepository.findByUserUUID(uuid)).thenReturn(Optional.of(token));
        when(token.isWithdrawalCodeValid("123456")).thenReturn(true);

        UUID result = withdrawalTokenService.verifyWithdrawalToken(request);
        assertEquals(uuid, result);
    }

    @Test
    @DisplayName("탈퇴 코드 인증 실패 - 코드 불일치")
    void verifyWithdrawalToken_invalidCode() {
        AuthCodeRequest request = new AuthCodeRequest("wrong-code");
        WithdrawalToken token = mock(WithdrawalToken.class);

        when(withdrawalTokenRepository.findByUserUUID(uuid)).thenReturn(Optional.of(token));
        when(token.isWithdrawalCodeValid("wrong-code")).thenReturn(false);

        assertThrows(WithdrawalTokenException.class, () -> withdrawalTokenService.verifyWithdrawalToken(request));
    }

    @Test
    @DisplayName("탈퇴 코드 인증 실패 - 토큰 없음")
    void verifyWithdrawalToken_tokenNotFound() {
        AuthCodeRequest request = new AuthCodeRequest("code");
        when(withdrawalTokenRepository.findByUserUUID(uuid)).thenReturn(Optional.empty());

        assertThrows(WithdrawalTokenException.class, () -> withdrawalTokenService.verifyWithdrawalToken(request));
    }

    @Test
    @DisplayName("토큰 삭제 성공")
    void deleteWithdrawalToken_success() {
        WithdrawalToken token = mock(WithdrawalToken.class);
        when(withdrawalTokenRepository.findByUserUUID(uuid)).thenReturn(Optional.of(token));

        withdrawalTokenService.deleteWithdrawalToken(uuid);

        verify(withdrawalTokenRepository).delete(token);
    }

    @Test
    @DisplayName("토큰 삭제 실패 - 존재하지 않음")
    void deleteWithdrawalToken_notFound() {
        when(withdrawalTokenRepository.findByUserUUID(uuid)).thenReturn(Optional.empty());

        assertThrows(WithdrawalTokenException.class, () -> withdrawalTokenService.deleteWithdrawalToken(uuid));
    }
}

package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.global.exception.errortype.SignupTokenException;

import com.USWCicrcleLink.server.user.domain.SignupToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SignupTokenServiceTest {

    private SignupTokenService signupTokenService;

    @Mock
    private RedisTemplate<String, SignupToken> redisTemplate;

    @Mock
    private ValueOperations<String, SignupToken> valueOperations;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        PasswordService passwordService = new PasswordService();
        signupTokenService = new SignupTokenService(redisTemplate);
    }

    @Test
    @DisplayName("UUID로 SignupToken 조회 성공")
    void getSignUpTokenByUUID_success() {
        UUID uuid = UUID.randomUUID();
        SignupToken token = new SignupToken(uuid, uuid, "test@example.com");
        String key = "signUpToken:" + uuid;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(token);

        SignupToken result = signupTokenService.getSignUpTokenByUUID(uuid.toString());
        assertEquals(token, result);
    }

    @Test
    @DisplayName("UUID로 SignupToken 조회 실패 - 없음")
    void getSignUpTokenByUUID_fail_notFound() {
        String key = "signUpToken:" + "non-existent-uuid";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(null);

        assertThrows(SignupTokenException.class,
                () -> signupTokenService.getSignUpTokenByUUID("non-existent-uuid"));
    }

    @Test
    @DisplayName("Email로 SignupToken 조회 성공")
    void getSignUpTokenByEmail_success() {
        String email = "test@example.com";
        SignupToken token = new SignupToken(UUID.randomUUID(), UUID.randomUUID(), email);
        String key = "signUpToken:" + email;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(token);

        SignupToken result = signupTokenService.getSignUpTokenByEmail(email);
        assertEquals(token, result);
    }

    @Test
    @DisplayName("Email로 SignupToken 조회 실패 - 없음")
    void getSignUpTokenByEmail_fail_notFound() {
        String email = "missing@example.com";
        String key = "signUpToken:" + email;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(null);

        assertThrows(SignupTokenException.class,
                () -> signupTokenService.getSignUpTokenByEmail(email));
    }

    @Test
    @DisplayName("SignupToken 검증 성공")
    void verifyUser_success() {
        UUID uuid = UUID.randomUUID();
        SignupToken token = new SignupToken(uuid, uuid, "test@example.com");

        SignupTokenService spyService = spy(signupTokenService);
        doReturn(token).when(spyService).getSignUpTokenByUUID(uuid.toString());

        SignupToken result = spyService.verifyUser(uuid, uuid);
        assertEquals(token, result);
    }


    @Test
    @DisplayName("SignupToken 검증 실패 - UUID 불일치")
    void verifyUser_fail_uuidMismatch() {
        UUID uuid = UUID.randomUUID();
        SignupToken token = new SignupToken(UUID.randomUUID(), UUID.randomUUID(), "test@example.com");

        SignupTokenService spyService = spy(signupTokenService);
        doReturn(token).when(spyService).getSignUpTokenByUUID(uuid.toString());

        assertThrows(SignupTokenException.class, () -> spyService.verifyUser(uuid, uuid));
    }

    @Test
    @DisplayName("SignupToken Redis에서 삭제 성공")
    void deleteSignUpTokenFromRedis_success() {
        SignupToken token = new SignupToken(UUID.randomUUID(), UUID.randomUUID(), "test@example.com");

        signupTokenService.deleteSignUpTokenFromRedis(token);

        verify(redisTemplate, times(1)).delete("signUpToken:" + token.getEmailTokenUUID());
        verify(redisTemplate, times(1)).delete("signUpToken:" + token.getEmail());
    }
}

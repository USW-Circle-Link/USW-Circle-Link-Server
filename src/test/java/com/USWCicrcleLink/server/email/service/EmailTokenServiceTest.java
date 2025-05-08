package com.USWCicrcleLink.server.email.service;

import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.global.exception.errortype.EmailException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmailTokenServiceTest {

    @Mock
    private RedisTemplate<String, EmailToken> redisTemplate;

    @Mock
    private ValueOperations<String, EmailToken> valueOps;

    private EmailTokenService emailTokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        emailTokenService = new EmailTokenService(redisTemplate);
    }

    @Test
    @DisplayName("이메일 토큰 생성 성공 테스트")
    void createEmailToken_success() {
        String email = "test@example.com";
        EmailToken token = emailTokenService.createEmailToken(email);

        assertThat(token).isNotNull();
        assertThat(token.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("이메일 토큰 생성 실패 시 예외 발생 테스트")
    void createEmailToken_failure_throwsException() {
        EmailTokenService serviceSpy = spy(new EmailTokenService(redisTemplate));
        doThrow(RuntimeException.class).when(serviceSpy).saveEmailToken(any());

        assertThatThrownBy(() -> serviceSpy.createEmailToken("fail@example.com"))
                .isInstanceOf(EmailException.class)
                .hasMessageContaining("이메일 토큰 생성중 오류가 발생했습니다.");
    }

    @Test
    @DisplayName("이메일 토큰 Redis 저장 성공 테스트")
    void saveEmailToken_savesSuccessfully() {
        String email = "save@example.com";
        EmailToken token = EmailToken.createEmailToken(email);

        EmailToken saved = emailTokenService.saveEmailToken(token);

        verify(valueOps, times(2)).set(anyString(), eq(token), eq(Duration.ofMinutes(5)));
        assertThat(saved).isEqualTo(token);
    }

    @Test
    @DisplayName("UUID로 이메일 토큰 조회 테스트")
    void getEmailTokenByUUID_returnsCorrectToken() {
        String uuid = UUID.randomUUID().toString();
        EmailToken token = EmailToken.createEmailToken("uuid@example.com");

        when(valueOps.get("emailToken:" + uuid)).thenReturn(token);

        EmailToken result = emailTokenService.getEmailTokenByUUID(uuid);

        assertThat(result).isEqualTo(token);
    }

    @Test
    @DisplayName("이메일로 이메일 토큰 조회 테스트")
    void getEmailTokenByEmail_returnsCorrectToken() {
        String email = "find@example.com";
        EmailToken token = EmailToken.createEmailToken(email);

        when(valueOps.get("emailToken:" + email)).thenReturn(token);

        EmailToken result = emailTokenService.getEmailTokenByEmail(email);

        assertThat(result).isEqualTo(token);
    }

    @Test
    @DisplayName("이메일 토큰 TTL 갱신 테스트")
    void updateExpirationTime_renewsTTL() {
        EmailToken token = EmailToken.createEmailToken("ttl@example.com");

        when(redisTemplate.expire(anyString(), eq(Duration.ofMinutes(5)))).thenReturn(true);

        EmailToken result = emailTokenService.updateExpirationTime(token);

        assertThat(result).isEqualTo(token);
    }

    @Test
    @DisplayName("이메일 토큰 Redis 삭제 테스트")
    void deleteEmailTokenFromRedis_deletesCorrectly() {
        EmailToken token = EmailToken.createEmailToken("delete@example.com");

        emailTokenService.deleteEmailTokenFromRedis(token);

        verify(redisTemplate, times(1)).delete("emailToken:" + token.getEmailTokenUUID().toString());
        verify(redisTemplate, times(1)).delete("emailToken:" + token.getEmail());
    }
}

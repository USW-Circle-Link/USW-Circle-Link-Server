package com.USWCicrcleLink.server.email.service;
import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.email.repository.EmailTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class ShedulerConfig {

    private final EmailTokenRepository emailTokenRepository;


    // 미인증 회원 삭제
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void deleteExpiredTokens() {
        LocalDateTime time = LocalDateTime.now().minusHours(1);
        List<EmailToken> tokens = emailTokenRepository.findAllByCertificationTimeBefore(time);
        emailTokenRepository.deleteAll(tokens);
    }

}

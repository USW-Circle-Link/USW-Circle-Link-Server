package com.USWCicrcleLink.server.global.config;

import com.USWCicrcleLink.server.club.application.domain.Aplict;
import com.USWCicrcleLink.server.club.application.repository.AplictRepository;
import com.USWCicrcleLink.server.global.email.domain.EmailToken;
import com.USWCicrcleLink.server.global.email.repository.EmailTokenRepository;
import com.USWCicrcleLink.server.user.profile.domain.Profile;
import com.USWCicrcleLink.server.user.profile.repository.ProfileRepository;

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
public class SchedulerConfig {

    private final EmailTokenRepository emailTokenRepository;
    private final AplictRepository aplictRepository;
    private final ProfileRepository profileRepository;

    // 미인증 회원 삭제
    @Scheduled(cron = "0 0 * * * *") // 1시간 마다 실행
    @Transactional
    public void deleteExpiredTokens() {

        LocalDateTime time = LocalDateTime.now().minusHours(1); // 만료시간 1시간 경과된 토큰 삭제
        List<EmailToken> tokens = emailTokenRepository.findAllByExpirationTimeBefore(time);

        if (!tokens.isEmpty()) {
            emailTokenRepository.deleteAll(tokens);
            log.debug("만료된 이메일 토큰 삭제");
        } else {
            log.debug("삭제할 이메일 토큰 존재하지않음");
        }
    }

    // 매일 00시(자정)에 실행
    // 최초 합 통보 후 4일이 지난 지원서 삭제
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void deleteOldApplications() {
        // 지원서에는 최초 합 통보 후 4일이 지난 날짜 값만 기입
        // 현재 날짜와 같은 지원서 삭제
        LocalDateTime now = LocalDateTime.now();
        List<Aplict> applicantsToDelete = aplictRepository.findAllByDeleteDateBefore(now);
        if (!applicantsToDelete.isEmpty()) {
            aplictRepository.deleteAll(applicantsToDelete);
            log.debug("4일 지난 지원서 {}개 삭제 완료", applicantsToDelete.size());
        } else {
            log.debug("삭제할 지원서 없음");
        }
    }

    // 매일 00시(자정)에 실행
    // 7일이 지난 fcm 토큰 삭제
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void deleteExpiredFcmTokens() {
        // 만료된 fcm토큰 삭제
        LocalDateTime now = LocalDateTime.now();
        List<Profile> expiredFcmTokens = profileRepository.findAllByFcmTokenCertificationTimestampBefore(now);
        if (!expiredFcmTokens.isEmpty()) {
            for (Profile profile : expiredFcmTokens) {
                profile.updateFcmToken(null); // FCM 토큰을 null로 설정하여 만료 처리
                profileRepository.save(profile);
            }
            log.debug("만료된 FCM 토큰 {}개 만료 처리 완료", expiredFcmTokens.size());
        } else {
            log.debug("만료된 FCM 토큰이 없음");
        }
    }

}

package com.USWCicrcleLink.server.notification.outbox;

import com.USWCicrcleLink.server.club.application.domain.Aplict;
import com.USWCicrcleLink.server.club.application.domain.AplictStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Saves the notification snapshot inside the publication's transaction; never sends FCM. */
@Service
@RequiredArgsConstructor
public class PublicationOutboxWriter {
    private final NotificationOutboxRepository repository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void save(Aplict applicant, AplictStatus result) {
        String suffix = result == AplictStatus.PASS ? "에 합격했습니다." : "에 불합격했습니다.";
        repository.save(NotificationOutbox.applicationResult(
                applicant.getAplictUUID(), applicant.getProfile().getProfileId(),
                applicant.getProfile().getFcmToken(), result,
                "동아리 지원 결과", applicant.getClub().getClubName() + suffix));
    }
}

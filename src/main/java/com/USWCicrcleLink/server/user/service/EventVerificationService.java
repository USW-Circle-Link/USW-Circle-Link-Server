package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.club.repository.ClubMembersRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.user.domain.EventVerification;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.dto.EventVerifyResponse;
import com.USWCicrcleLink.server.user.repository.EventVerificationRepository;
import com.USWCicrcleLink.server.user.profile.repository.ProfileRepository;
import com.USWCicrcleLink.server.user.profile.domain.Profile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventVerificationService {

    private final EventVerificationRepository eventVerificationRepository;
    private final ProfileRepository profileRepository;
    private final ClubMembersRepository clubMembersRepository;

    // 이벤트 코드: 환경 변수/프로퍼티 없을 경우 기본값 1115 사용
    @Value("${event.code:1115}")
    private String expectedEventCode;

    @Transactional(readOnly = true)
    public boolean checkStatus(User user) {
        // profileId로 clubUUID 조회
        UUID clubuuid = getClubuuidByUserUUID(user.getUserUUID());
        return eventVerificationRepository.existsByUserUUIDAndClubuuid(user.getUserUUID(), clubuuid);
    }

    @Transactional
    public EventVerifyResponse verify(User user, String code) {

        Profile profile = profileRepository.findByUser_UserUUID(user.getUserUUID())
                .orElseThrow(() -> {
                    log.warn("이벤트 인증 시도 - 프로필 없음. userUUID={}", user.getUserUUID());
                    return new UserException(ExceptionType.INVALID_INPUT); // 프로필이 없으므로 진행 불가
                });

        Long profileId = profile.getProfileId();

        List<UUID> clubuuids = clubMembersRepository.findClubuuidByProfileId(profileId);
        if (clubuuids.isEmpty()) {
            log.warn("이벤트 인증 시도 - 연결된 동아리 없음. userUUID={}, profileId={}", user.getUserUUID(), profileId);
            throw new UserException(ExceptionType.INVALID_INPUT); // 동아리가 없으므로 진행 불가
        }
        UUID clubuuid = clubuuids.get(0); // 첫 번째 동아리 UUID 사용 (기존 JWT 로직과 동일하게)

        // 이미 인증된 경우: 오류 반환
        if (eventVerificationRepository.existsByUserUUIDAndClubuuid(user.getUserUUID(), clubuuid)) {
            log.debug("이미 인증된 상태 - userUUID={}, clubuuid={}", user.getUserUUID(), clubuuid);
            throw new UserException(ExceptionType.EVENT_ALREADY_VERIFIED);
        }

        // 코드 검증 (첫 인증만 코드 검사)
        if (code == null || !code.equals(expectedEventCode)) {
            throw new UserException(ExceptionType.INVALID_EVENT_CODE);
        }

        // [수정] 프로필 여부 조회 (위에서 이미 조회함)
        // Long profileId = profileRepository.findByUserUserId(user.getUserId())
        // .map(Profile::getProfileId)
        // .orElse(null);

        // 인증 성공 처리 (MYSQL 저장)
        EventVerification saved = eventVerificationRepository.save(
                EventVerification.create(
                        user.getUserUUID(),
                        clubuuid, // 내부에서 조회한 clubuuid
                        user.getUserId(),
                        profileId, // 내부에서 조회한 profileId
                        user.getUserAccount(),
                        user.getEmail()));
        log.info("이벤트 인증 완료 - userUUID={}, clubuuid={}", user.getUserUUID(), clubuuid);
        // 첫 인증 성공: isFirstVerify=true
        return new EventVerifyResponse(clubuuid, true, saved.getVerifiedAt());
    }

    private UUID getClubuuidByUserUUID(UUID userUUID) {
        Profile profile = profileRepository.findByUser_UserUUID(userUUID)
                .orElseThrow(() -> new UserException(ExceptionType.INVALID_INPUT)); // 프로필이 없음

        Long profileId = profile.getProfileId();

        List<UUID> clubuuids = clubMembersRepository.findClubuuidByProfileId(profileId);
        if (clubuuids.isEmpty()) {
            throw new UserException(ExceptionType.INVALID_INPUT); // 연결된 동아리가 없음
        }
        return clubuuids.get(0); // 첫 번째 UUID 반환
    }
}

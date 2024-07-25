package com.USWCicrcleLink.server.profile.service;

import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
import com.USWCicrcleLink.server.profile.dto.ProfileRequest;
import com.USWCicrcleLink.server.profile.dto.ProfileResponse;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public ProfileResponse updateProfile(UUID userUUID, ProfileRequest profileRequest) {

        Profile profile = getProfileByUserUUID(userUUID);

        profile.updateProfile(profileRequest);

        profileRepository.save(profile);

        log.info("프로필 수정 완료 {}", userUUID);
        return new ProfileResponse(profile);
    }

    private Profile getProfileByUserUUID(UUID userUUID) {
        User user = userRepository.findByUserUUID(userUUID);
        if (user == null) {
            throw new IllegalArgumentException("해당 uuid의 유저가 존재하지 않습니다.: " + userUUID);
        }

        return profileRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저Id의 프로필이 존재하지 않습니다.: " + userUUID));
    }
}

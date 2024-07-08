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

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    @PostConstruct
    @Transactional
    public void init() {
        // Create User
        User user = User.builder()
                .userUUID("0000")
                .userAccount("admin")
                .userPw("1234")
                .email("admin")
                .userCreatedAt(LocalDateTime.now())
                .userUpdatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        // Create Profile
        Profile profile = Profile.builder()
                .user(user)
                .userName("안녕")
                .studentNumber("1234")
                .userHp("01012345678")
                .major("정보보호")
                .profileCreatedAt(LocalDateTime.now())
                .profileUpdatedAt(LocalDateTime.now())
                .build();

        profileRepository.save(profile);
    }

    public ProfileResponse updateProfile(String userUUID, ProfileRequest profileRequest){

        Profile profile = getProfileByUserUUID(userUUID);

        profile.setUserName(profileRequest.getUserName());
        profile.setStudentNumber(profileRequest.getStudentNumber());
        profile.setUserHp(profileRequest.getUserHp());
        profile.setMajor(profileRequest.getMajor());
        profile.setProfileUpdatedAt(LocalDateTime.now());

        profileRepository.save(profile);

        log.info("프로필 수정 완료 {}", userUUID);
        return new ProfileResponse(profile);
    }

    private Profile getProfileByUserUUID(String userUUID) {
        User user = userRepository.findByUserUUID(userUUID);
        if (user == null) {
            throw new IllegalArgumentException("해당 uuid의 유저가 존재하지 않습니다.: " + userUUID);
        }

        return profileRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저Id의 프로필이 존재하지 않습니다.: " + userUUID));
    }

}

package com.USWCicrcleLink.server.global.security.details.service;

import com.USWCicrcleLink.server.club.club.repository.ClubMembersRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.ProfileException;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.global.security.details.CustomUserDetails;
import com.USWCicrcleLink.server.profile.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.profile.repository.ProfileRepository;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    ProfileRepository profileRepository;
    @Mock
    ClubMembersRepository clubMembersRepository;

    @InjectMocks
    CustomUserDetailsService customUserDetailsService;

    @Nested
    @DisplayName("loadUserByUuid 메서드 단위 테스트")
    class LoadUserByUuidTest {

        @Test
        @DisplayName("정상적으로 User, Profile, ClubUUIDs 조회 시 CustomUserDetails를 반환")
        void loadUserByUuid_정상() {
            // given
            UUID userUuid = UUID.randomUUID();
            User user = User.builder()
                    .userUUID(userUuid)
                    .userAccount("test@company.com")
                    .userPw("encoded_pw")
                    .build();
            Profile profile = Profile.builder()
                    .profileId(1L)
                    .user(user)
                    .build();
            List<UUID> clubUUIDs = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());

            given(userRepository.findByUserUUID(userUuid)).willReturn(Optional.of(user));
            given(profileRepository.findByUser_UserUUID(userUuid)).willReturn(Optional.of(profile));
            given(clubMembersRepository.findClubUUIDsByProfileId(profile.getProfileId())).willReturn(clubUUIDs);

            // when
            UserDetails result = customUserDetailsService.loadUserByUuid(userUuid);

            // then
            assertThat(result).isInstanceOf(CustomUserDetails.class);
            CustomUserDetails details = (CustomUserDetails) result;
            assertThat(details.getUserUUID()).isEqualTo(user.getUserUUID());
            assertThat(details.getUsername()).isEqualTo(user.getUserUUID().toString());
            assertThat(details.getClubUUIDs()).containsExactlyElementsOf(clubUUIDs);

        }

        @Test
        @DisplayName("User가 존재하지 않으면 UserException 발생")
        void loadUserByUuid_유저없음() {
            // given
            UUID userUuid = UUID.randomUUID();
            given(userRepository.findByUserUUID(userUuid)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> customUserDetailsService.loadUserByUuid(userUuid))
                    .isInstanceOf(UserException.class)
                    .hasMessageContaining(ExceptionType.USER_NOT_EXISTS.getMessage());
        }

        @Test
        @DisplayName("Profile이 존재하지 않으면 ProfileException 발생")
        void loadUserByUuid_프로필없음() {
            // given
            UUID userUuid = UUID.randomUUID();
            User user = User.builder()
                    .userUUID(userUuid)
                    .userAccount("test@company.com")
                    .userPw("encoded_pw")
                    .build();
            given(userRepository.findByUserUUID(userUuid)).willReturn(Optional.of(user));
            given(profileRepository.findByUser_UserUUID(userUuid)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> customUserDetailsService.loadUserByUuid(userUuid))
                    .isInstanceOf(ProfileException.class)
                    .hasMessageContaining(ExceptionType.PROFILE_NOT_EXISTS.getMessage());
        }

        @Test
        @DisplayName("가입한 동아리가 없을 때도 빈 clubUUIDs로 CustomUserDetails를 반환")
        void loadUserByUuid_동아리없음() {
            // given
            UUID userUuid = UUID.randomUUID();
            User user = User.builder()
                    .userUUID(userUuid)
                    .userAccount("test@company.com")
                    .userPw("encoded_pw")
                    .build();
            Profile profile = Profile.builder()
                    .profileId(1L)
                    .user(user)
                    .build();

            given(userRepository.findByUserUUID(userUuid)).willReturn(Optional.of(user));
            given(profileRepository.findByUser_UserUUID(userUuid)).willReturn(Optional.of(profile));
            given(clubMembersRepository.findClubUUIDsByProfileId(profile.getProfileId())).willReturn(Collections.emptyList());

            // when
            UserDetails result = customUserDetailsService.loadUserByUuid(userUuid);

            // then
            assertThat(result).isInstanceOf(CustomUserDetails.class);
            CustomUserDetails details = (CustomUserDetails) result;
            assertThat(details.getUserUUID()).isEqualTo(user.getUserUUID());
            assertThat(details.getUsername()).isEqualTo(user.getUserUUID().toString());
            assertThat(details.getClubUUIDs()).isEmpty();

        }
    }
}

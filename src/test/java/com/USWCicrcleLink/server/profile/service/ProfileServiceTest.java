//package com.USWCicrcleLink.server.profile.service;
//
//import com.USWCicrcleLink.server.profile.domain.Profile;
//import com.USWCicrcleLink.server.profile.dto.ProfileRequest;
//import com.USWCicrcleLink.server.profile.dto.ProfileResponse;
//import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
//import com.USWCicrcleLink.server.user.domain.User;
//import com.USWCicrcleLink.server.user.repository.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class ProfileServiceTest {
//    @InjectMocks
//    private ProfileService profileService;
//
//    @Mock
//    private ProfileRepository profileRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    private User user;
//    private Profile profile;
//
//    @BeforeEach
//    void setUp() {
//        user = User.builder()
//                .userId(1L)
//                .userUUID(UUID.randomUUID())
//                .userAccount("testUser")
//                .userPw("testPw")
//                .email("test@example.com")
//                .build();
//
//        profile = Profile.builder()
//                .profileId(1L)
//                .user(user)
//                .build();
//    }
//    @Test
//    @DisplayName("프로필 수정 성공")
//    void updateProfile_성공() {
//        //given
//        UUID userUUID = user.getUserUUID();
//        ProfileRequest request = new ProfileRequest();
//
//        when(userRepository.findByUserUUID(any(UUID.class))).thenReturn(user);
//        when(profileRepository.findByUserUserId(anyLong())).thenReturn(Optional.of(profile));
//        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        //when
//        ProfileResponse response = profileService.updateProfile(userUUID, request);
//
//        //then
//        assertNotNull(response);
//        verify(profileRepository, times(1)).save(any(Profile.class));
//        verify(profileRepository, times(1)).findByUserUserId(anyLong());
//        verify(userRepository, times(1)).findByUserUUID(any(UUID.class));
//    }
//
//    @Test
//    @DisplayName("프로필 수정 실패 - 유저 없음")
//    void updateProfile_실패_유저없음() {
//        //given
//        UUID userUUID = UUID.randomUUID();
//        ProfileRequest request = new ProfileRequest();
//
//        when(userRepository.findByUserUUID(any(UUID.class))).thenReturn(null);
//
//        //when
//        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
//                profileService.updateProfile(userUUID, request));
//
//        //then
//        assertEquals("해당 uuid의 유저가 존재하지 않습니다.: " + userUUID, exception.getMessage());
//    }
//
//}
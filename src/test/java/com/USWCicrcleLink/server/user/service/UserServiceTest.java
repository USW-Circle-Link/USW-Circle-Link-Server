package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.global.exception.errortype.ClubMemberTempException;
import com.USWCicrcleLink.server.global.exception.errortype.ProfileException;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.profile.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.profile.repository.ProfileRepository;
import com.USWCicrcleLink.server.user.domain.ExistingMember.ClubMemberTemp;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.dto.ExistingMemberSignUpRequest;
import com.USWCicrcleLink.server.user.dto.SignUpRequest;
import com.USWCicrcleLink.server.user.repository.ClubMemberTempRepository;
import com.USWCicrcleLink.server.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private ClubMemberTempRepository clubMemberTempRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private SignUpRequest signUpRequest;
    private ExistingMemberSignUpRequest existingRequest;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        signUpRequest = mock(SignUpRequest.class);
        existingRequest = mock(ExistingMemberSignUpRequest.class);
        user = mock(User.class);
    }

    @Test
    @DisplayName("신규 회원가입 성공")
    void signUpUser_success() {
        Profile profile = mock(Profile.class);
        when(userService.createUser(signUpRequest, "test@example.com")).thenReturn(user);
        when(userService.createProfile(user, signUpRequest)).thenReturn(profile);

        userService.signUpUser(signUpRequest, "test@example.com");

        verify(userRepository).save(user);
        verify(profileRepository).save(profile);
    }

    @Test
    @DisplayName("User 객체 생성 성공")
    void createUser_success() {
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(signUpRequest.getPassword()).thenReturn("raw");
        mockStatic(User.class).when(() -> User.createUser(signUpRequest, "encoded", "test@example.com"))
                .thenReturn(user);

        User result = userService.createUser(signUpRequest, "test@example.com");
        assertEquals(user, result);
    }

    @Test
    @DisplayName("User 객체 생성 실패 시 예외 발생")
    void createUser_fail() {
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(signUpRequest.getPassword()).thenReturn("raw");
        mockStatic(User.class).when(() -> User.createUser(signUpRequest, "encoded", "test@example.com"))
                .thenThrow(RuntimeException.class);

        assertThrows(UserException.class, () -> userService.createUser(signUpRequest, "test@example.com"));
    }

    @Test
    @DisplayName("Profile 객체 생성 성공")
    void createProfile_success() {
        Profile profile = mock(Profile.class);
        when(signUpRequest.getTelephone()).thenReturn("010-1234-5678");
        mockStatic(Profile.class).when(() -> Profile.createProfile(user, signUpRequest, "01012345678"))
                .thenReturn(profile);

        Profile result = userService.createProfile(user, signUpRequest);
        assertEquals(profile, result);
    }

    @Test
    @DisplayName("Profile 객체 생성 실패 시 예외 발생")
    void createProfile_fail() {
        when(signUpRequest.getTelephone()).thenReturn("010-1234-5678");
        mockStatic(Profile.class).when(() -> Profile.createProfile(user, signUpRequest, "01012345678"))
                .thenThrow(RuntimeException.class);

        assertThrows(ProfileException.class, () -> userService.createProfile(user, signUpRequest));
    }

    @Test
    @DisplayName("임시 동아리원 등록 성공")
    void registerClubMemberTemp_success() {
        ClubMemberTemp clubMemberTemp = mock(ClubMemberTemp.class);
        when(existingRequest.getClubs()).thenReturn(java.util.Collections.singletonList(mock(com.USWCicrcleLink.server.user.dto.ClubDTO.class)));
        when(existingRequest.getTelephone()).thenReturn("010-1234-5678");
        when(existingRequest.getPassword()).thenReturn("pw");
        when(existingRequest.getUserName()).thenReturn("홍길동");
        when(passwordEncoder.encode("pw")).thenReturn("encoded");
        when(clubMemberTempRepository.save(any())).thenReturn(clubMemberTemp);

        ClubMemberTemp result = userService.registerClubMemberTemp(existingRequest);
        assertEquals(clubMemberTemp, result);
    }

    @Test
    @DisplayName("임시 동아리원 등록 실패")
    void registerClubMemberTemp_fail() {
        when(existingRequest.getClubs()).thenReturn(java.util.Collections.singletonList(mock(com.USWCicrcleLink.server.user.dto.ClubDTO.class)));
        when(existingRequest.getTelephone()).thenReturn("010-1234-5678");
        when(existingRequest.getPassword()).thenReturn("pw");
        when(existingRequest.getUserName()).thenReturn("홍길동");
        when(passwordEncoder.encode("pw")).thenReturn("encoded");
        when(clubMemberTempRepository.save(any())).thenThrow(RuntimeException.class);

        assertThrows(ClubMemberTempException.class, () -> userService.registerClubMemberTemp(existingRequest));
    }
}

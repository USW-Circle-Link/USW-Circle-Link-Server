//package com.USWCicrcleLink.server.aplict.service;
//
//import com.USWCicrcleLink.server.aplict.dto.AplictRequest;
//import com.USWCicrcleLink.server.aplict.repository.AplictRepository;
//import com.USWCicrcleLink.server.club.club.repository.ClubMembersRepository;
//import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
//import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
//import com.USWCicrcleLink.server.global.exception.errortype.ClubException;
//import com.USWCicrcleLink.server.global.exception.errortype.ClubIntroException;
//import com.USWCicrcleLink.server.global.exception.errortype.UserException;
//import com.USWCicrcleLink.server.global.security.details.CustomUserDetails;
//import com.USWCicrcleLink.server.profile.domain.Profile;
//import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
//import com.USWCicrcleLink.server.user.domain.User;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//
//import java.util.Optional;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.mock;
//
//@ExtendWith(MockitoExtension.class)
//class AplictServiceTest {
//
//    @Mock
//    private AplictRepository aplictRepository;
//
//    @Mock
//    private ProfileRepository profileRepository;
//
//    @Mock
//    private ClubIntroRepository clubIntroRepository;
//
//    @Mock
//    private ClubMembersRepository clubMembersRepository;
//
//    @InjectMocks
//    private AplictService aplictService;
//
//    private User mockUser;
//    private Profile mockProfile;
//    private CustomUserDetails mockUserDetails;
//
//    @BeforeEach
//    void setUp() {
//        mockUser = User.builder().userAccount("user").userPw("pw").email("email@example.com").build();
//        mockUserDetails = mock(CustomUserDetails.class);
//        mockProfile = mock(Profile.class);
//    }
//
//    //securityContext
//    private void setUpSecurityContext() {
//        Authentication authentication = mock(Authentication.class);
//        SecurityContext securityContext = mock(SecurityContext.class);
//
//        given(mockUserDetails.user()).willReturn(mockUser);
//        given(securityContext.getAuthentication()).willReturn(authentication);
//        given(authentication.getPrincipal()).willReturn(mockUserDetails);
//
//        SecurityContextHolder.setContext(securityContext);
//    }
//
//    @Test
//    @DisplayName("사용자 프로필이 존재하지 않으면 USER_NOT_EXISTS 예외 발생")
//    void ThrowUserExceptionWhenUserProfileDoesNotExist() {
//        // given
//        setUpSecurityContext();
//        given(profileRepository.findByUser_UserUUID(mockUser.getUserUUID())).willReturn(Optional.empty());
//
//        // when, then
//        assertThatThrownBy(() -> aplictService.checkIfCanApply(1L))
//                .isInstanceOf(UserException.class)
//                .hasMessageContaining("사용자가 존재하지 않습니다.");
//    }
//
//    @Test
//    @DisplayName("해당 동아리에 이미 지원했으면 ALREADY_APPLIED 예외 발생")
//    void checkIfCanApply_AlreadyApplied_ThrowsClubException() {
//        // given
//        setUpSecurityContext();
//        given(profileRepository.findByUser_UserUUID(mockUser.getUserUUID())).willReturn(Optional.of(mockProfile));
//        given(aplictRepository.existsByProfileAndClub_ClubId(mockProfile, 1L)).willReturn(true);
//
//        // when, then
//        assertThatThrownBy(() -> aplictService.checkIfCanApply(1L))
//                .isInstanceOf(ClubException.class)
//                .hasMessageContaining("이미 지원한 동아리입니다.");
//    }
//
//    @Test
//    @DisplayName("이미 해당 동아리 회원이면 ALREADY_MEMBER 예외 발생")
//    void checkIfCanApply_AlreadyMember_ThrowsClubException() {
//        // given
//        setUpSecurityContext();
//        given(profileRepository.findByUser_UserUUID(mockUser.getUserUUID())).willReturn(Optional.of(mockProfile));
//        given(aplictRepository.existsByProfileAndClub_ClubId(mockProfile, 1L)).willReturn(false);
//        given(clubMembersRepository.existsByProfileAndClub_ClubId(mockProfile, 1L)).willReturn(true);
//
//        // when, then
//        assertThatThrownBy(() -> aplictService.checkIfCanApply(1L))
//                .isInstanceOf(ClubException.class)
//                .hasMessageContaining("이미 해당 동아리 회원입니다.");
//    }
//
//    @Test
//    @DisplayName("동아리의 구글 폼 URL 조회 시 URL이 없으면 GOOGLE_FORM_URL_NOT_EXISTS 예외 발생")
//    void getGoogleFormUrlByClubId_ThrowsExceptionWhenUrlDoesNotExist() {
//        // given
//        ClubIntro clubIntro = ClubIntro.builder().clubIntroId(1L).googleFormUrl("").build();
//        given(clubIntroRepository.findByClubClubId(1L)).willReturn(Optional.of(clubIntro));
//
//        // when, then
//        assertThatThrownBy(() -> aplictService.getGoogleFormUrlByClubId(1L))
//                .isInstanceOf(ClubIntroException.class)
//                .hasMessageContaining("구글 폼 URL이 존재하지 않습니다.");
//    }
//
//    @Test
//    @DisplayName("동아리 지원서 제출 시 사용자가 존재하지 않으면 USER_NOT_EXISTS 예외 발생")
//    void submitAplict_ThrowsUserExceptionWhenUserDoesNotExist() {
//        // given
//        setUpSecurityContext();
//        given(profileRepository.findByUser_UserUUID(mockUser.getUserUUID())).willReturn(Optional.empty());
//
//        // when, then
//        AplictRequest request = new AplictRequest();
//        assertThatThrownBy(() -> aplictService.submitAplict(1L, request))
//                .isInstanceOf(UserException.class)
//                .hasMessageContaining("사용자가 존재하지 않습니다.");
//    }
//}

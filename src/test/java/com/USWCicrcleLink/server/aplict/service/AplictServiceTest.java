package com.USWCicrcleLink.server.aplict.service;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.repository.AplictRepository;
import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.repository.ClubMembersRepository;
import com.USWCicrcleLink.server.club.club.repository.ClubRepository;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.global.exception.errortype.AplictException;
import com.USWCicrcleLink.server.global.exception.errortype.ClubException;
import com.USWCicrcleLink.server.global.security.details.CustomUserDetails;
import com.USWCicrcleLink.server.profile.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.profile.repository.ProfileRepository;
import com.USWCicrcleLink.server.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AplictServiceTest {

    @Mock
    private AplictRepository aplictRepository;
    @Mock
    private ClubRepository clubRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private ClubIntroRepository clubIntroRepository;
    @Mock
    private ClubMembersRepository clubMembersRepository;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private AplictService aplictService;

    private Profile profile;
    private User user;
    private UUID clubUUID;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder().userUUID(UUID.randomUUID()).build();
        profile = Profile.builder().user(user).studentNumber("12345678").userHp("01012345678").build();
        clubUUID = UUID.randomUUID();

        CustomUserDetails userDetails = new CustomUserDetails(user, List.of());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.setContext(securityContext);

        when(profileRepository.findByUser_UserUUID(user.getUserUUID())).thenReturn(Optional.of(profile));
    }

    @Test
    @DisplayName("지원 가능 체크 - 성공")
    void checkIfCanApply_success() {
        when(aplictRepository.existsByProfileAndClubUUID(profile, clubUUID)).thenReturn(false);
        when(clubMembersRepository.existsByProfileAndClubUUID(profile, clubUUID)).thenReturn(false);
        when(clubMembersRepository.findProfilesByClubUUID(clubUUID)).thenReturn(List.of());

        assertThatCode(() -> aplictService.checkIfCanApply(clubUUID)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("지원 가능 체크 - 이미 지원")
    void checkIfCanApply_alreadyApplied() {
        when(aplictRepository.existsByProfileAndClubUUID(profile, clubUUID)).thenReturn(true);

        assertThatThrownBy(() -> aplictService.checkIfCanApply(clubUUID))
                .isInstanceOf(AplictException.class);
    }

    @Test
    @DisplayName("구글 폼 URL 조회 성공")
    void getGoogleFormUrl_success() {
        ClubIntro intro = ClubIntro.builder().googleFormUrl("https://form.com").build();
        when(clubIntroRepository.findByClubUUID(clubUUID)).thenReturn(Optional.of(intro));

        String url = aplictService.getGoogleFormUrlByClubUUID(clubUUID);

        assertThat(url).isEqualTo("https://form.com");
    }

    @Test
    @DisplayName("구글 폼 URL 없음 예외")
    void getGoogleFormUrl_notExists() {
        ClubIntro intro = ClubIntro.builder().googleFormUrl(null).build();
        when(clubIntroRepository.findByClubUUID(clubUUID)).thenReturn(Optional.of(intro));

        assertThatThrownBy(() -> aplictService.getGoogleFormUrlByClubUUID(clubUUID))
                .isInstanceOf(ClubException.class);
    }

    @Test
    @DisplayName("지원서 제출 성공")
    void submitAplict_success() {
        Club club = Club.builder().clubUUID(clubUUID).build();
        when(clubRepository.findByClubUUID(clubUUID)).thenReturn(Optional.of(club));

        aplictService.submitAplict(clubUUID);

        verify(aplictRepository, times(1)).save(any(Aplict.class));
    }

    @Test
    @DisplayName("지원서 제출 실패 - 클럽 없음")
    void submitAplict_clubNotFound() {
        when(clubRepository.findByClubUUID(clubUUID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aplictService.submitAplict(clubUUID))
                .isInstanceOf(ClubException.class);
    }
}
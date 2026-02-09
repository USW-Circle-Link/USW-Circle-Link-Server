package com.USWCicrcleLink.server.club.leader.service;

import com.USWCicrcleLink.server.admin.domain.Admin;
import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.club.leader.domain.Leader;
import com.USWCicrcleLink.server.club.repository.ClubRepository;
import com.USWCicrcleLink.server.global.exception.errortype.ClubLeaderException;
import com.USWCicrcleLink.server.global.security.details.CustomAdminDetails;
import com.USWCicrcleLink.server.global.security.details.CustomLeaderDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ClubLeaderService 접근 권한 테스트")
class ClubLeaderServiceAccessTest {

    @Mock
    private ClubRepository clubRepository;

    @InjectMocks
    private ClubLeaderService clubLeaderService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setSecurityContext(Object principal) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, null);
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("Admin은 모든 동아리에 접근할 수 있다")
    void adminCanAccessAnyClub() {
        // given
        UUID testClubUUID = UUID.randomUUID();
        Club testClub = mock(Club.class);

        Admin admin = mock(Admin.class);
        CustomAdminDetails adminDetails = new CustomAdminDetails(admin);
        setSecurityContext(adminDetails);

        when(clubRepository.findByClubuuid(testClubUUID)).thenReturn(Optional.of(testClub));

        // when
        Club result = clubLeaderService.validateLeaderAccess(testClubUUID);

        // then
        assertThat(result).isEqualTo(testClub);
        verify(clubRepository).findByClubuuid(testClubUUID);
    }

    @Test
    @DisplayName("Leader는 자신의 동아리에 접근할 수 있다")
    void leaderCanAccessOwnClub() {
        // given
        UUID testClubUUID = UUID.randomUUID();
        Club testClub = mock(Club.class);

        Leader leader = mock(Leader.class);
        CustomLeaderDetails leaderDetails = new CustomLeaderDetails(leader, testClubUUID);
        setSecurityContext(leaderDetails);

        when(clubRepository.findByClubuuid(testClubUUID)).thenReturn(Optional.of(testClub));

        // when
        Club result = clubLeaderService.validateLeaderAccess(testClubUUID);

        // then
        assertThat(result).isEqualTo(testClub);
    }

    @Test
    @DisplayName("Leader는 다른 동아리에 접근할 수 없다")
    void leaderCannotAccessOtherClub() {
        // given
        UUID testClubUUID = UUID.randomUUID();
        UUID otherClubUUID = UUID.randomUUID();

        Leader leader = mock(Leader.class);
        CustomLeaderDetails leaderDetails = new CustomLeaderDetails(leader, otherClubUUID);
        setSecurityContext(leaderDetails);

        // when & then
        assertThatThrownBy(() -> clubLeaderService.validateLeaderAccess(testClubUUID))
                .isInstanceOf(ClubLeaderException.class);
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 접근할 수 없다")
    void unauthenticatedUserCannotAccess() {
        // given
        UUID testClubUUID = UUID.randomUUID();
        setSecurityContext("anonymousUser");

        // when & then
        assertThatThrownBy(() -> clubLeaderService.validateLeaderAccess(testClubUUID))
                .isInstanceOf(ClubLeaderException.class);
    }
}

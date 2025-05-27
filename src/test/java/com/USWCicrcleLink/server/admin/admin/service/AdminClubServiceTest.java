package com.USWCicrcleLink.server.admin.admin.service;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.admin.dto.AdminClubListResponse;
import com.USWCicrcleLink.server.admin.admin.dto.AdminClubPageListResponse;
import com.USWCicrcleLink.server.admin.admin.dto.AdminPwRequest;
import com.USWCicrcleLink.server.club.club.repository.ClubMainPhotoRepository;
import com.USWCicrcleLink.server.club.club.repository.ClubRepository;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroPhotoRepository;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.clubLeader.repository.LeaderRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.AdminException;
import com.USWCicrcleLink.server.global.exception.errortype.ClubException;
import com.USWCicrcleLink.server.global.security.details.CustomAdminDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminClubServiceTest {

    @Mock
    private LeaderRepository leaderRepository;
    @Mock
    private ClubRepository clubRepository;
    @Mock
    private ClubIntroRepository clubIntroRepository;
    @Mock
    private ClubMainPhotoRepository clubMainPhotoRepository;
    @Mock
    private ClubIntroPhotoRepository clubIntroPhotoRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminClubService adminClubService;

    private void mockAuthenticatedAdmin(String password) {
        Admin admin = Admin.builder().adminPw(password).build();
        Authentication authentication = mock(Authentication.class);
        CustomAdminDetails adminDetails = mock(CustomAdminDetails.class);

        when(authentication.getPrincipal()).thenReturn(adminDetails);
        when(adminDetails.admin()).thenReturn(admin);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("동아리 목록 조회")
    void testGetAllClubs() {
        Pageable pageable = PageRequest.of(0, 10);
        AdminClubListResponse response = mock(AdminClubListResponse.class);
        Page<AdminClubListResponse> page = new PageImpl<>(Collections.singletonList(response), pageable, 1);

        when(clubRepository.findAllWithMemberAndLeaderCount(pageable)).thenReturn(page);

        AdminClubPageListResponse result = adminClubService.getAllClubs(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getCurrentPage()).isEqualTo(0);
    }

    @Test
    @DisplayName("중복 계정 예외")
    void testValidateLeaderAccount_Duplicate() {
        when(leaderRepository.existsByLeaderAccount("leader1")).thenReturn(true);

        assertThatThrownBy(() -> adminClubService.validateLeaderAccount("leader1"))
                .isInstanceOf(ClubException.class)
                .hasMessageContaining(ExceptionType.LEADER_ACCOUNT_ALREADY_EXISTS.getMessage());
    }

    @Test
    @DisplayName("중복 이름 예외")
    void testValidateClubName_Duplicate() {
        when(clubRepository.existsByClubName("TestClub")).thenReturn(true);

        assertThatThrownBy(() -> adminClubService.validateClubName("TestClub"))
                .isInstanceOf(ClubException.class)
                .hasMessageContaining(ExceptionType.CLUB_NAME_ALREADY_EXISTS.getMessage());
    }

    @Test
    @DisplayName("비밀번호 불일치 예외")
    void testDeleteClub_AdminPasswordMismatch() {
        UUID clubUUID = UUID.randomUUID();
        AdminPwRequest request = new AdminPwRequest("wrongPw");

        mockAuthenticatedAdmin("adminPw");

        when(passwordEncoder.matches(eq("wrongPw"), eq("adminPw"))).thenReturn(false);

        assertThatThrownBy(() -> adminClubService.deleteClub(clubUUID, request))
                .isInstanceOf(AdminException.class)
                .hasMessageContaining(ExceptionType.ADMIN_PASSWORD_NOT_MATCH.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 동아리 예외")
    void testDeleteClub_ClubNotFound() {
        UUID clubUUID = UUID.randomUUID();
        AdminPwRequest request = new AdminPwRequest("adminPw");

        mockAuthenticatedAdmin("adminPw");

        when(passwordEncoder.matches(eq("adminPw"), eq("adminPw"))).thenReturn(true);
        when(clubRepository.findClubIdByUUID(clubUUID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminClubService.deleteClub(clubUUID, request))
                .isInstanceOf(ClubException.class)
                .hasMessageContaining(ExceptionType.CLUB_NOT_EXISTS.getMessage());
    }
}

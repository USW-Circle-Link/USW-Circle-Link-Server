//package com.USWCicrcleLink.server.admin.admin.service;
//
//import com.USWCicrcleLink.server.admin.admin.domain.Admin;
//import com.USWCicrcleLink.server.global.login.dto.AdminLoginRequest;
//import com.USWCicrcleLink.server.admin.admin.dto.ClubCreationRequest;
//import com.USWCicrcleLink.server.admin.admin.dto.ClubDetailResponse;
//import com.USWCicrcleLink.server.admin.admin.dto.ClubListResponse;
//import com.USWCicrcleLink.server.admin.admin.repository.AdminRepository;
//import com.USWCicrcleLink.server.aplict.repository.AplictRepository;
//import com.USWCicrcleLink.server.club.club.domain.Club;
//import com.USWCicrcleLink.server.club.club.repository.ClubRepositoryCustom;
//import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
//import com.USWCicrcleLink.server.club.club.domain.Department;
//import com.USWCicrcleLink.server.club.club.domain.RecruitmentStatus;
//import com.USWCicrcleLink.server.club.club.dto.ClubMembersLeaderCount;
//import com.USWCicrcleLink.server.admin.admin.dto.ClubMembersLeaderCount;
//import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
//import com.USWCicrcleLink.server.club.club.repository.ClubMembersRepository;
//import com.USWCicrcleLink.server.club.club.repository.ClubRepository;
//import com.USWCicrcleLink.server.clubLeader.domain.Leader;
//import com.USWCicrcleLink.server.clubLeader.repository.LeaderRepository;
//import com.USWCicrcleLink.server.clubLeader.repository.LeaderRepositoryCustom;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class AdminServiceTest {
//
//    @InjectMocks
//    private AdminService adminService;
//
//    @Mock
//    private AdminRepository adminRepository;
//
//    @Mock
//    private ClubRepository clubRepository;
//
//    @Mock
//    private ClubRepositoryCustom clubRepositoryCustom;
//
//    @Mock
//    private ClubIntroRepository clubIntroRepository;
//
//    @Mock
//    private LeaderRepository leaderRepository;
//
//    @Mock
//    private LeaderRepositoryCustom leaderRepositoryCustom;
//
//    @Mock
//    private ClubMembersRepository clubMembersRepository;
//
//    @Mock
//    private AplictRepository aplictRepository;
//
//    private Admin admin;
//    private Club club;
//    private ClubIntro clubIntro;
//
//    @BeforeEach
//    void setUp() {
//        //given
//        admin = Admin.builder()
//                .adminAccount("admin")
//                .adminPw("1234")
//                .build();
//
//        club = Club.builder()
//                .clubId(1L)
//                .clubName("Flag")
//                .department(Department.ART)
//                .leaderName("김지오")
//                .recruitmentStatus(RecruitmentStatus.CLOSE)
//                .build();
//
//        Leader.builder()
//                .leaderAccount("leader")
//                .leaderPw("leaderPw")
//                .build();
//
//        clubIntro = ClubIntro.builder()
//                .club(club)
//                .clubIntro("Flag소개")
//                .build();
//    }
//
//    @Test
//    void 관리자로그인_성공() {
//        //given
//        AdminLoginRequest request = new AdminLoginRequest("admin", "1234");
//        when(adminRepository.findByAdminAccount(request.getAdminAccount())).thenReturn(Optional.of(admin));
//
//        //when
//        //then
//        assertDoesNotThrow(() -> adminService.adminLogin(request));
//    }
//
//    @Test
//    void 관리자로그인_실패_잘못된비밀번호() {
//        //given
//        AdminLoginRequest request = new AdminLoginRequest("admin", "12");
//        when(adminRepository.findByAdminAccount(request.getAdminAccount())).thenReturn(Optional.of(admin));
//
//        //when
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> adminService.adminLogin(request));
//
//        //then
//        assertEquals("비밀번호 틀림", exception.getMessage());
//    }
//
//    @Test
//    void 동아리전체목록조회() {
//        //given
//        ClubMembersLeaderCount count = new ClubMembersLeaderCount(club.getClubId(), 10L, 1L);
//        when(clubRepositoryCustom.findAllWithMemberAndLeaderCount()).thenReturn(Arrays.asList(count));
//        when(clubRepository.findById(club.getClubId())).thenReturn(Optional.of(club));
//
//        //when
//        List<ClubListResponse> responses = adminService.getAllClubs();
//
//        //then
//        assertEquals(1, responses.size());
//        assertEquals(club.getClubName(), responses.get(0).getClubName());
//        assertEquals(11L, responses.get(0).getNumberOfClubMembers()); //동아리원 10명, 리더 1명
//    }
//
//    @Test
//    void 동아리상세페이지조회() {
//        //given
//        when(clubRepository.findById(club.getClubId())).thenReturn(Optional.of(club));
//        when(clubIntroRepository.findByClub(club)).thenReturn(Optional.of(clubIntro));
//
//        //when
//        ClubDetailResponse response = adminService.getClubById(club.getClubId());
//
//        //then
//        assertNotNull(response);
//        assertEquals(club.getClubName(), response.getClubName());
//        assertEquals(clubIntro.getClubIntro(), response.getIntroContent());
//    }
//
//    @Test
//    void 동아리생성_성공() {
//        //given
//        ClubCreationRequest request = ClubCreationRequest.builder()
//                .adminPw("1234")
//                .leaderAccount("newLeader")
//                .leaderPw("newLeaderPw")
//                .leaderPwConfirm("newLeaderPw")
//                .clubName("New Club")
//                .department(Department.ART)
//                .build();
//
//        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));
//
//        //when
//        //then
//        assertDoesNotThrow(() -> adminService.createClub(1L, request));
//        verify(leaderRepository, times(1)).save(any(Leader.class));
//        verify(clubRepository, times(1)).save(any(Club.class));
//        verify(clubIntroRepository, times(1)).save(any(ClubIntro.class));
//    }
//
//    @Test
//    void 동아리생성_실패_잘못된관리자비밀번호() {
//        //given
//        ClubCreationRequest request = ClubCreationRequest.builder()
//                .adminPw("12")
//                .leaderAccount("newLeader")
//                .leaderPw("newLeaderPw")
//                .leaderPwConfirm("newLeaderPw")
//                .clubName("New Club")
//                .department(Department.ART)
//                .build();
//
//        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));
//
//        //when
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> adminService.createClub(1L, request));
//
//        //then
//        assertEquals("비밀번호 틀림", exception.getMessage());
//    }
//
//    @Test
//    void 동아리삭제_성공() {
//        //given
//        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));
//
//        //when
//        //then
//        assertDoesNotThrow(() -> adminService.deleteClub(1L, club.getClubId(), "1234"));
//        verify(aplictRepository, times(1)).deleteByClubClubId(club.getClubId());
//        verify(clubIntroRepository, times(1)).deleteByClubClubId(club.getClubId());
//        verify(clubMembersRepository, times(1)).deleteByClubClubId(club.getClubId());
//        verify(leaderRepositoryCustom, times(1)).deleteByClubClubId(club.getClubId());
//        verify(clubRepository, times(1)).deleteById(club.getClubId());
//    }
//
//    @Test
//    void 동아리삭제_실패_잘못된관리자비밀번호() {
//        //given
//        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));
//
//        //when
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> adminService.deleteClub(1L, club.getClubId(), "wrongPassword"));
//
//        //then
//        assertEquals("비밀번호 틀림", exception.getMessage());
//    }
//}

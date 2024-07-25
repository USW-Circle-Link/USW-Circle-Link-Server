package com.USWCicrcleLink.server.club.service;

import com.USWCicrcleLink.server.club.clubIntro.service.ClubIntroService;
import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.club.domain.Department;
import com.USWCicrcleLink.server.club.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.club.club.dto.ClubByDepartmentResponse;
import com.USWCicrcleLink.server.club.clubIntro.dto.ClubIntroResponse;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.club.club.repository.ClubRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClubIntroServiceTest {

    @InjectMocks
    private ClubIntroService clubIntroService;

    @Mock
    private ClubIntroRepository clubIntroRepository;

    @Mock
    private ClubRepository clubRepository;

    private Club club;
    private ClubIntro clubIntro;

    @BeforeEach
    void setUp() {
        //given
        club = Club.builder()
                .clubId(1L)
                .clubName("Flag")
                .recruitmentStatus(RecruitmentStatus.OPEN)
                .department(Department.ART)
                .build();

        clubIntro = ClubIntro.builder()
                .club(club)
                .googleFormUrl("https://forms.gle/testForm")
                .build();
    }

    @Test
    void 동아리소개글조회_성공() {
        //given
        when(clubIntroRepository.findByClubClubId(anyLong())).thenReturn(Optional.of(clubIntro));

        //when
        ClubIntroResponse response = clubIntroService.getClubIntroByClubId(club.getClubId());

        //then
        assertNotNull(response);
        assertEquals(club.getClubId(), response.getClubId());
        assertEquals(club.getRecruitmentStatus(), response.getRecruitmentStatus());
        verify(clubIntroRepository, times(1)).findByClubClubId(anyLong());
    }

    @Test
    void 동아리소개글조회_실패_존재하지않는동아리() {
        //given
        when(clubIntroRepository.findByClubClubId(anyLong())).thenReturn(Optional.empty());

        //when
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () ->
                clubIntroService.getClubIntroByClubId(club.getClubId()));
        //then
        assertEquals("해당 동아리에 대한 소개를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void 분과별동아리조회_성공() {
        //given
        when(clubRepository.findByDepartment(any(Department.class))).thenReturn(Collections.singletonList(club));

        //when
        List<ClubByDepartmentResponse> responses = clubIntroService.getClubsByDepartment(Department.ART);

        //then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(club.getClubName(), responses.get(0).getClubName());
        verify(clubRepository, times(1)).findByDepartment(any(Department.class));
    }

    @Test
    void 분과별동아리조회_실패_존재하지않는분과() {
        //given
        when(clubRepository.findByDepartment(any(Department.class))).thenReturn(Collections.emptyList());

        //when
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () ->
                clubIntroService.getClubsByDepartment(Department.ART));
        //then
        assertEquals("해당 분과에 속하는 동아리가 없습니다.", exception.getMessage());
    }

    @Test
    void 모집상태및분과별동아리조회_성공() {
        //given
        when(clubRepository.findByRecruitmentStatusAndDepartment(any(RecruitmentStatus.class), any(Department.class)))
                .thenReturn(Collections.singletonList(club));

        //when
        List<ClubByDepartmentResponse> responses = clubIntroService.getClubsByRecruitmentStatusAndDepartment(RecruitmentStatus.OPEN, Department.ART);

        //then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(club.getClubName(), responses.get(0).getClubName());
        verify(clubRepository, times(1)).findByRecruitmentStatusAndDepartment(any(RecruitmentStatus.class), any(Department.class));
    }

    @Test
    void 모집상태및분과별동아리조회_실패_조건에맞는동아리없음() {
        //given
        when(clubRepository.findByRecruitmentStatusAndDepartment(any(RecruitmentStatus.class), any(Department.class)))
                .thenReturn(Collections.emptyList());

        //when
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () ->
                clubIntroService.getClubsByRecruitmentStatusAndDepartment(RecruitmentStatus.OPEN, Department.ART));
        //then
        assertEquals("해당 조건에 맞는 동아리가 없습니다.", exception.getMessage());
    }
}

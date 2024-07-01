package com.USWCicrcleLink.server.club.controller;

import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.club.domain.ClubIntro;
import com.USWCicrcleLink.server.club.domain.Department;
import com.USWCicrcleLink.server.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.club.service.ClubIntroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ClubIntroControllerTest {

    @Mock
    private ClubIntroService clubIntroService;

    @InjectMocks
    private ClubIntroController clubIntroController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(clubIntroController).build();
    }

    @Test
    void getClubIntroByClubId() throws Exception {
        Club club = Club.builder()
                .clubId(1L)
                .clubName("ART Club")
                .department(Department.ART)
                .build();

        ClubIntro clubIntro = ClubIntro.builder()
                .clubIntroId(1L)
                .club(club)
                .clubIntro("This is a club intro")
                .introPhotoPath("/path/to/intro/photo")
                .additionalPhotoPath1("/path/to/photo1")
                .additionalPhotoPath2("/path/to/photo2")
                .recruitmentStatus(RecruitmentStatus.OPEN)
                .googleFormUrl("http://example.com/form")
                .build();
        clubIntro.setRecruitmentStatus(RecruitmentStatus.OPEN); // 모집 상태 설정

        when(clubIntroService.getClubIntroByClubId(1L)).thenReturn(clubIntro);

        mockMvc.perform(get("/clubs/1/clubIntro")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("동아리 소개글 조회 성공")))
                .andExpect(jsonPath("$.data.clubIntroId", is(1)))
                .andExpect(jsonPath("$.data.club.clubId", is(1)))
                .andExpect(jsonPath("$.data.club.clubName", is("ART Club")))
                .andExpect(jsonPath("$.data.club.department", is("ART")))
                .andExpect(jsonPath("$.data.clubIntro", is("This is a club intro")))
                .andExpect(jsonPath("$.data.introPhotoPath", is("/path/to/intro/photo")))
                .andExpect(jsonPath("$.data.additionalPhotoPath1", is("/path/to/photo1")))
                .andExpect(jsonPath("$.data.additionalPhotoPath2", is("/path/to/photo2")))
                .andExpect(jsonPath("$.data.recruitmentStatus", is("OPEN")))
                .andExpect(jsonPath("$.data.googleFormUrl", is("http://example.com/form")))
                .andExpect(jsonPath("$.data.recruitmentStatus", is("OPEN")));
    }

    @Test
    void applyToClub() throws Exception {
        doNothing().when(clubIntroService).applyToClub(1L);

        mockMvc.perform(post("/clubs/1/apply")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("지원이 완료되었습니다.")));
    }
}

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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ClubIntroControllerTest {

    @Mock
    private ClubIntroService clubIntroService;

    @InjectMocks
    private ClubIntroController clubIntroController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(clubIntroController).build();
    }

    @Test
    public void 동아리소개글조회() throws Exception {
        ClubIntro clubIntro = ClubIntro.builder()
                .clubIntro("This is a club intro")
                .recruitmentStatus(RecruitmentStatus.OPEN)
                .googleFormUrl("https://google.com")
                .build();

        when(clubIntroService.getClubIntroByClubId(anyLong())).thenReturn(clubIntro);

        mockMvc.perform(get("/clubs/1/clubIntro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("동아리 소개글 조회 성공"))
                .andExpect(jsonPath("$.data.clubIntro").value("This is a club intro"));
    }

    @Test
    public void 동아리지원구글폼열기() throws Exception {
        ClubIntro clubIntro = ClubIntro.builder()
                .recruitmentStatus(RecruitmentStatus.OPEN)
                .googleFormUrl("https://google.com")
                .build();

        when(clubIntroService.getClubIntroByClubId(anyLong())).thenReturn(clubIntro);

        mockMvc.perform(get("/clubs/1/apply"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://google.com"));
    }

    @Test
    public void 동아리모집마감() throws Exception {
        ClubIntro clubIntro = ClubIntro.builder()
                .recruitmentStatus(RecruitmentStatus.CLOSE)
                .build();

        when(clubIntroService.getClubIntroByClubId(anyLong())).thenReturn(clubIntro);

        mockMvc.perform(get("/clubs/1/apply"))
                .andExpect(status().isForbidden());
    }
}
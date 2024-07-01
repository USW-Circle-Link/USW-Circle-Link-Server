package com.USWCicrcleLink.server.club.controller;

import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.club.domain.Department;
import com.USWCicrcleLink.server.club.service.ClubService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ClubControllerTest {

    @Mock
    private ClubService clubService;

    @InjectMocks
    private ClubController clubController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(clubController).build();
    }

    @Test
    void 모든동아리조회() throws Exception {
        List<Club> clubs = Arrays.asList(
                Club.builder()
                        .clubId(1L)
                        .clubName("Club1")
                        .department(Department.ART)
                        .build(),
                Club.builder()
                        .clubId(2L)
                        .clubName("Club2")
                        .department(Department.ACADEMIC)
                        .build()
        );

        when(clubService.getAllClubs()).thenReturn(clubs);

        mockMvc.perform(get("/clubs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("모든 동아리 조회 성공")))
                .andExpect(jsonPath("$.data[0].clubId", is(1)))
                .andExpect(jsonPath("$.data[0].clubName", is("Club1")))
                .andExpect(jsonPath("$.data[0].department", is("ART")))
                .andExpect(jsonPath("$.data[1].clubId", is(2)))
                .andExpect(jsonPath("$.data[1].clubName", is("Club2")))
                .andExpect(jsonPath("$.data[1].department", is("ACADEMIC")));
    }

    @Test
    void 동아리조회() throws Exception {
        Club club = Club.builder()
                .clubId(1L)
                .clubName("Club1")
                .department(Department.ART)
                .build();

        when(clubService.getClubById(1L)).thenReturn(club);

        mockMvc.perform(get("/clubs/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("동아리 조회 성공")))
                .andExpect(jsonPath("$.data.clubId", is(1)))
                .andExpect(jsonPath("$.data.clubName", is("Club1")))
                .andExpect(jsonPath("$.data.department", is("ART")));
    }

    @Test
    void 분과별동아리조회() throws Exception {
        List<Club> clubs = Arrays.asList(
                Club.builder()
                        .clubId(1L)
                        .clubName("Club1")
                        .department(Department.ART)
                        .build(),
                Club.builder()
                        .clubId(2L)
                        .clubName("Club2")
                        .department(Department.ART)
                        .build()
        );

        when(clubService.getClubsByDepartment(Department.ART)).thenReturn(clubs);

        mockMvc.perform(get("/clubs/department/ART")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("분과별 동아리 조회 성공")))
                .andExpect(jsonPath("$.data[0].clubId", is(1)))
                .andExpect(jsonPath("$.data[0].clubName", is("Club1")))
                .andExpect(jsonPath("$.data[0].department", is("ART")))
                .andExpect(jsonPath("$.data[1].clubId", is(2)))
                .andExpect(jsonPath("$.data[1].clubName", is("Club2")))
                .andExpect(jsonPath("$.data[1].department", is("ART")));
    }
}

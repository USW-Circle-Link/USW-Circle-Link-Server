//package com.USWCicrcleLink.server.club.api;
//
//import com.USWCicrcleLink.server.club.clubIntro.api.ClubIntroController;
//import com.USWCicrcleLink.server.club.club.domain.Department;
//import com.USWCicrcleLink.server.club.club.domain.RecruitmentStatus;
//import com.USWCicrcleLink.server.club.club.dto.ClubByDepartmentResponse;
//import com.USWCicrcleLink.server.club.clubIntro.dto.ClubIntroResponse;
//import com.USWCicrcleLink.server.club.clubIntro.service.ClubIntroService;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.Collections;
//import java.util.List;
//
//import static org.hamcrest.Matchers.is;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@ExtendWith(SpringExtension.class)
//@WebMvcTest(ClubIntroController.class)
//public class ClubIntroControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private ClubIntroService clubIntroService;
//
//    @Test
//    void 분과별동아리조회_성공() throws Exception {
//        //given
//        ClubByDepartmentResponse clubResponse = ClubByDepartmentResponse.builder()
//                .clubId(1L)
//                .clubName("Flag")
//                .build();
//        List<ClubByDepartmentResponse> clubResponses = Collections.singletonList(clubResponse);
//        Mockito.when(clubIntroService.getClubsByDepartment(any(Department.class))).thenReturn(clubResponses);
//
//        //when
//        mockMvc.perform(get("/clubs/department/ART")
//                        .contentType(MediaType.APPLICATION_JSON))
//                //then
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message", is("분과별 동아리 조회 성공")))
//                .andExpect(jsonPath("$.data[0].clubName", is("Flag")));
//    }
//
//    @Test
//    void 모집상태및분과별동아리조회_성공() throws Exception {
//        //given
//        ClubByDepartmentResponse clubResponse = ClubByDepartmentResponse.builder()
//                .clubId(1L)
//                .clubName("Flag")
//                .build();
//        List<ClubByDepartmentResponse> clubResponses = Collections.singletonList(clubResponse);
//        Mockito.when(clubIntroService.getClubsByRecruitmentStatusAndDepartment(any(RecruitmentStatus.class), any(Department.class)))
//                .thenReturn(clubResponses);
//
//        //when
//        mockMvc.perform(get("/clubs/department/ART/OPEN")
//                        .contentType(MediaType.APPLICATION_JSON))
//                //then
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message", is("모집 상태 및 분과별 동아리 조회 성공")))
//                .andExpect(jsonPath("$.data[0].clubName", is("Flag")));
//    }
//
//    @Test
//    void 동아리소개글조회_성공() throws Exception {
//        //given
//        ClubIntroResponse clubIntroResponse = ClubIntroResponse.builder()
//                .clubId(1L)
//                .introContent("Flag")
//                .build();
//        Mockito.when(clubIntroService.getClubIntroByClubId(anyLong())).thenReturn(clubIntroResponse);
//
//        //when
//        mockMvc.perform(get("/clubs/1/clubIntro")
//                        .contentType(MediaType.APPLICATION_JSON))
//                //then
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message", is("동아리 소개글 조회 성공")))
//                .andExpect(jsonPath("$.data.introContent", is("Flag")));
//    }
//}

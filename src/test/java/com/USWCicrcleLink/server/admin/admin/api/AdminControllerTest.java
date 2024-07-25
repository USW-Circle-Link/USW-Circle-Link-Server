package com.USWCicrcleLink.server.admin.admin.api;

import com.USWCicrcleLink.server.admin.admin.dto.*;
import com.USWCicrcleLink.server.admin.admin.service.AdminService;
import com.USWCicrcleLink.server.club.club.domain.Department;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AdminController.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 관리자로그인_성공() throws Exception {
        //given
        AdminLoginRequest request = new AdminLoginRequest("admin", "1234");

        //when
        mockMvc.perform(post("/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("로그인 성공")));
    }

    @Test
    void 동아리전체리스트조회_성공() throws Exception {
        //given
        ClubListResponse club = ClubListResponse.builder()
                .clubId(1L)
                .department(Department.ART)
                .clubName("Flag")
                .leaderName("김지오")
                .numberOfClubMembers(10L)
                .build();

        List<ClubListResponse> clubs = Arrays.asList(club);
        Mockito.when(adminService.getAllClubs()).thenReturn(clubs);

        //when
        mockMvc.perform(get("/admin/clubs")
                        .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("동아리 전체 리스트 조회 성공")))
                .andExpect(jsonPath("$.data[0].clubName", is("Flag")));
    }

    @Test
    void 동아리상세페이지조회_성공() throws Exception {
        //given
        ClubDetailResponse clubDetail = ClubDetailResponse.builder()
                .clubId(1L)
                .clubName("Flag")
                .leaderName("김지오")
                .phone("010-1234-5678")
                .instagram("test_insta")
                .mainPhotoPath("/path/to/photo")
                .chatRoomUrl("https://chat.url")
                .introContent("Intro content")
                .build();

        Mockito.when(adminService.getClubById(1L)).thenReturn(clubDetail);

        //when
        mockMvc.perform(get("/admin/clubs/1")
                        .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("동아리 상세 조회 성공")))
                .andExpect(jsonPath("$.data.clubName", is("Flag")));
    }

    @Test
    void 동아리생성_성공() throws Exception {
        //given
        ClubCreationRequest request = ClubCreationRequest.builder()
                .adminPw("1234")
                .leaderAccount("leader")
                .leaderPw("leaderPw")
                .leaderPwConfirm("leaderPw")
                .clubName("New Club")
                .department(Department.ART)
                .build();

        //when
        mockMvc.perform(post("/admin/club/create")
                        .header("admin_Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("동아리 생성 성공")));
    }

    @Test
    void 동아리삭제_성공() throws Exception {
        //given
        AdminPwRequest request = new AdminPwRequest("1234");

        //when
        mockMvc.perform(delete("/admin/club/delete/1")
                        .header("admin_Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("동아리 삭제 성공")))
                .andExpect(jsonPath("$.data", is(1)));
    }
}

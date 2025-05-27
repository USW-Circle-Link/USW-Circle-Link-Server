package com.USWCicrcleLink.server.admin.admin.api;

import com.USWCicrcleLink.server.admin.admin.dto.AdminClubCreationRequest;
import com.USWCicrcleLink.server.admin.admin.dto.AdminPwRequest;
import com.USWCicrcleLink.server.admin.admin.service.AdminClubService;
import com.USWCicrcleLink.server.club.club.service.ClubService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class AdminClubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminClubService adminClubService;

    @Autowired
    private ClubService clubService;

    @Test
    @DisplayName("동아리 전체 목록 조회")
    void getAllClubs() throws Exception {
        mockMvc.perform(get("/admin/clubs")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("동아리 리스트 조회 성공"));
    }

    @Test
    @DisplayName("동아리 소개 조회")
    void getClubById() throws Exception {
        UUID testUUID = UUID.randomUUID();

        mockMvc.perform(get("/admin/clubs/" + testUUID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("동아리 생성")
    void createClub() throws Exception {
        AdminClubCreationRequest request = new AdminClubCreationRequest();

        mockMvc.perform(post("/admin/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("동아리 생성 성공"));
    }

    @Test
    @DisplayName("동아리 삭제")
    void deleteClub() throws Exception {
        UUID testUUID = UUID.randomUUID();
        AdminPwRequest request = new AdminPwRequest();

        mockMvc.perform(delete("/admin/clubs/" + testUUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("동아리 삭제 성공"));
    }

    @Test
    @DisplayName("회장 아이디 중복 확인")
    void checkLeaderAccountDuplicate() throws Exception {
        mockMvc.perform(get("/admin/clubs/leader/check")
                        .param("leaderAccount", "testLeader"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("사용 가능한 동아리 회장 아이디입니다."));
    }

    @Test
    @DisplayName("동아리 이름 중복 확인")
    void checkClubNameDuplicate() throws Exception {
        mockMvc.perform(get("/admin/clubs/name/check")
                        .param("clubName", "테스트동아리"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("사용 가능한 동아리 이름입니다."));
    }
}

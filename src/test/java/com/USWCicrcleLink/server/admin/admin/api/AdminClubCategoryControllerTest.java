package com.USWCicrcleLink.server.admin.admin.api;

import com.USWCicrcleLink.server.admin.admin.dto.AdminClubCategoryCreationRequest;
import com.USWCicrcleLink.server.admin.admin.service.AdminClubCategoryService;
import com.USWCicrcleLink.server.club.club.dto.ClubCategoryResponse;
import com.USWCicrcleLink.server.club.club.service.ClubService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AdminClubCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminClubCategoryService adminClubCategoryService;

    @MockBean
    private ClubService clubService;

    @Test
    @DisplayName("카테고리 리스트 조회 성공")
    void getAllClubCategories() throws Exception {
        ClubCategoryResponse dummy = new ClubCategoryResponse(UUID.randomUUID(), "Tech");
        when(clubService.getAllClubCategories()).thenReturn(List.of(dummy));

        mockMvc.perform(get("/admin/clubs/category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("카테고리 리스트 조회 성공")))
                .andExpect(jsonPath("$.data[0].clubCategoryName", is("Tech")));
    }

    @Test
    @DisplayName("카테고리 추가 성공")
    void addClubCategory() throws Exception {
        AdminClubCategoryCreationRequest request = new AdminClubCategoryCreationRequest("Art");
        ClubCategoryResponse response = new ClubCategoryResponse(UUID.randomUUID(), "Art");

        when(adminClubCategoryService.addClubCategory(any())).thenReturn(response);

        mockMvc.perform(post("/admin/clubs/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("카테고리 추가 성공")))
                .andExpect(jsonPath("$.data.clubCategoryName", is("Art")));
    }

    @Test
    @DisplayName("카테고리 삭제 성공")
    void deleteClubCategory() throws Exception {
        UUID categoryId = UUID.randomUUID();
        ClubCategoryResponse deleted = new ClubCategoryResponse(categoryId, "Sports");

        when(adminClubCategoryService.deleteClubCategory(categoryId)).thenReturn(deleted);

        mockMvc.perform(delete("/admin/clubs/category/{uuid}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("카테고리 삭제 성공")))
                .andExpect(jsonPath("$.data.clubCategoryName", is("Sports")));
    }
}
package com.USWCicrcleLink.server.admin.admin.api;

import com.USWCicrcleLink.server.admin.admin.dto.AdminFloorPhotoCreationResponse;
import com.USWCicrcleLink.server.admin.admin.service.AdminFloorPhotoService;
import com.USWCicrcleLink.server.club.club.domain.FloorPhotoEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AdminFloorPhotoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminFloorPhotoService adminFloorPhotoService;

    @Test
    @DisplayName("층별 사진 업로드 성공")
    void uploadFloorPhoto_success() throws Exception {
        MockMultipartFile photo = new MockMultipartFile("photo", "photo.jpg", MediaType.IMAGE_JPEG_VALUE, "mock-image".getBytes());
        AdminFloorPhotoCreationResponse response = AdminFloorPhotoCreationResponse.builder()
                .floor(FloorPhotoEnum.B1)
                .presignedUrl("http://example.com/photo.jpg")
                .build();

        when(adminFloorPhotoService.uploadPhoto(any(), any())).thenReturn(response);

        mockMvc.perform(multipart("/admin/floor/photo/{floor}", FloorPhotoEnum.B1.name())
                        .file(photo)
                        .with(req -> {
                            req.setMethod("PUT");
                            return req;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("해당 층 사진 업로드 성공")))
                .andExpect(jsonPath("$.data.presignedUrl", is("http://example.com/photo.jpg")));
    }

    @Test
    @DisplayName("층별 사진 조회 성공")
    void getPhotoByFloor_success() throws Exception {
        AdminFloorPhotoCreationResponse response = AdminFloorPhotoCreationResponse.builder()
                .floor(FloorPhotoEnum.F1)
                .presignedUrl("http://example.com/photo.jpg")
                .build();

        when(adminFloorPhotoService.getPhotoByFloor(FloorPhotoEnum.F1)).thenReturn(response);

        mockMvc.perform(get("/admin/floor/photo/{floor}", FloorPhotoEnum.F1.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("해당 층 사진 조회 성공")))
                .andExpect(jsonPath("$.data.presignedUrl", is("http://example.com/photo.jpg")));
    }

    @Test
    @DisplayName("층별 사진 삭제 성공")
    void deletePhotoByFloor_success() throws Exception {
        mockMvc.perform(delete("/admin/floor/photo/{floor}", FloorPhotoEnum.F2.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("해당 층 사진 삭제 성공")))
                .andExpect(jsonPath("$.data", is("Floor: F2")));
    }
}
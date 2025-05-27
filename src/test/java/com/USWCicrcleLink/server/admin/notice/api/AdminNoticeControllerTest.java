package com.USWCicrcleLink.server.admin.notice.api;

import com.USWCicrcleLink.server.admin.notice.dto.*;
import com.USWCicrcleLink.server.admin.notice.service.AdminNoticeService;
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

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AdminNoticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminNoticeService adminNoticeService;

    @Test
    @DisplayName("공지사항 리스트 조회 성공")
    void getNotices_success() throws Exception {
        AdminNoticePageListResponse response = AdminNoticePageListResponse.builder()
                .content(List.of())
                .totalPages(1)
                .totalElements(0L)
                .currentPage(0)
                .build();

        when(adminNoticeService.getNotices(any())).thenReturn(response);

        mockMvc.perform(get("/notices?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("공지사항 리스트 조회 성공")));
    }

    @Test
    @DisplayName("공지사항 상세 조회 성공")
    void getNoticeByUUID_success() throws Exception {
        UUID noticeUUID = UUID.randomUUID();
        NoticeDetailResponse detail = new NoticeDetailResponse(noticeUUID, "title", "content", List.of(), null, "관리자");
        when(adminNoticeService.getNoticeByUUID(noticeUUID)).thenReturn(detail);

        mockMvc.perform(get("/notices/{noticeUUID}", noticeUUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("공지사항 조회 성공")))
                .andExpect(jsonPath("$.data.noticeTitle", is("title")));
    }

    @Test
    @DisplayName("공지사항 생성 성공")
    void createNotice_success() throws Exception {
        AdminNoticeCreationRequest request = new AdminNoticeCreationRequest("title", "content", List.of(1));
        MockMultipartFile requestPart = new MockMultipartFile("request", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(request));
        MockMultipartFile photo = new MockMultipartFile("photos", "photo.jpg", MediaType.IMAGE_JPEG_VALUE, "mock image".getBytes());

        when(adminNoticeService.createNotice(any(), any())).thenReturn(List.of("http://mock-url"));

        mockMvc.perform(multipart("/notices")
                        .file(requestPart)
                        .file(photo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("공지사항 생성 성공")));
    }

    @Test
    @DisplayName("공지사항 수정 성공")
    void updateNotice_success() throws Exception {
        UUID noticeUUID = UUID.randomUUID();
        AdminNoticeUpdateRequest request = new AdminNoticeUpdateRequest("updated", "updated", List.of(1));
        MockMultipartFile requestPart = new MockMultipartFile("request", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(request));
        MockMultipartFile photo = new MockMultipartFile("photos", "photo.jpg", MediaType.IMAGE_JPEG_VALUE, "mock image".getBytes());

        when(adminNoticeService.updateNotice(any(), any(), any())).thenReturn(List.of("http://updated-url"));

        mockMvc.perform(multipart("/notices/{noticeUUID}", noticeUUID)
                        .file(requestPart)
                        .file(photo)
                        .with(req -> {
                            req.setMethod("PUT");
                            return req;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("공지사항 수정 성공")));
    }

    @Test
    @DisplayName("공지사항 삭제 성공")
    void deleteNotice_success() throws Exception {
        UUID noticeUUID = UUID.randomUUID();

        mockMvc.perform(delete("/notices/{noticeUUID}", noticeUUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("공지사항 삭제 성공")));
    }
}
//package com.USWCicrcleLink.server.admin.notice.api;
//
//import com.USWCicrcleLink.server.admin.notice.dto.NoticeListResponse;
//import com.USWCicrcleLink.server.admin.notice.service.NoticeService;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.data.domain.*;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(NoticeController.class)
//class NoticeControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private NoticeService noticeService;
//
//    @Test
//    void getNotices_ShouldReturnPagedNotices() throws Exception {
//        // Given
//        List<NoticeListResponse> notices = List.of(
//                new NoticeListResponse(1L, "공지사항 제목 1", "관리자 A", LocalDateTime.now()),
//                new NoticeListResponse(2L, "공지사항 제목 2", "관리자 A", LocalDateTime.now().minusDays(1))
//        );
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<NoticeListResponse> pagedNotices = new PageImpl<>(notices, pageable, notices.size());
//
//        when(noticeService.getNotices(pageable)).thenReturn(pagedNotices);
//
//        // When & Then
//        mockMvc.perform(get("/admin/notices")
//                        .param("page", "0")
//                        .param("size", "10")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("공지사항 리스트 조회 성공"))
//                .andExpect(jsonPath("$.data.content[0].noticeTitle").value("공지사항 제목 1"))
//                .andExpect(jsonPath("$.data.content[1].noticeTitle").value("공지사항 제목 2"));
//    }
//}

//package com.USWCicrcleLink.server.admin.notice.api;
//
//import com.USWCicrcleLink.server.admin.notice.dto.NoticeCreationRequest;
//import com.USWCicrcleLink.server.admin.notice.dto.NoticeDetailResponse;
//import com.USWCicrcleLink.server.admin.notice.dto.NoticeListResponse;
//import com.USWCicrcleLink.server.admin.notice.service.NoticeService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.web.PagedResourcesAssembler;
//import org.springframework.hateoas.PagedModel;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.time.LocalDateTime;
//import java.util.Collections;
//import java.util.List;
//
//import static org.hamcrest.Matchers.is;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@ExtendWith(SpringExtension.class)
//@WebMvcTest(NoticeController.class)
//public class NoticeControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private NoticeService noticeService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Test
//    void 공지사항전체리스트조회_성공() throws Exception {
//        //given
//        NoticeListResponse notice = NoticeListResponse.builder()
//                .noticeId(1L)
//                .noticeTitle("공지사항")
//                .adminName("관리자")
//                .noticeCreatedAt(LocalDateTime.now())
//                .build();
//
//        List<NoticeListResponse> notices = Collections.singletonList(notice);
//        Mockito.when(noticeService.getAllNotices()).thenReturn(notices);
//
//        //when
//        mockMvc.perform(get("/admin/notices")
//                        .contentType(MediaType.APPLICATION_JSON))
//                //then
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message", is("공지사항 리스트 조회 성공")))
//                .andExpect(jsonPath("$.data[0].noticeTitle", is("공지사항")));
//    }
//
//    @Test
//    void 공지사항리스트조회_페이징_성공() throws Exception {
//        //given
//        NoticeListResponse notice = NoticeListResponse.builder()
//                .noticeId(1L)
//                .noticeTitle("공지사항")
//                .adminName("관리자")
//                .noticeCreatedAt(LocalDateTime.now())
//                .build();
//
//        PageImpl<NoticeListResponse> page = new PageImpl<>(Collections.singletonList(notice), PageRequest.of(0, 10), 1);
//        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(10, 0, 1);
//        PagedModel<NoticeListResponse> pagedModel = PagedModel.of(Collections.singletonList(notice), metadata);
//        Mockito.when(noticeService.getNotices(any(), any())).thenReturn(pagedModel);
//
//        //when
//        mockMvc.perform(get("/admin/notices/paged")
//                        .contentType(MediaType.APPLICATION_JSON))
//                //then
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.page.totalElements", is(1)))
//                .andExpect(jsonPath("$._embedded.noticeListResponseList[0].noticeTitle", is("공지사항")));
//    }
//
//    @Test
//    void 공지사항조회_성공() throws Exception {
//        //given
//        NoticeDetailResponse notice = NoticeDetailResponse.builder()
//                .noticeId(1L)
//                .noticeTitle("공지사항")
//                .noticeContent("내용")
//                .noticeCreatedAt(LocalDateTime.now())
//                .build();
//
//        Mockito.when(noticeService.getNoticeById(anyLong())).thenReturn(notice);
//
//        //when
//        mockMvc.perform(get("/admin/notice/get/1")
//                        .contentType(MediaType.APPLICATION_JSON))
//                //then
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message", is("공지사항 조회 성공")))
//                .andExpect(jsonPath("$.data.noticeTitle", is("공지사항")));
//    }
//
//    @Test
//    void 공지사항생성_성공() throws Exception {
//        //given
//        NoticeCreationRequest request = NoticeCreationRequest.builder()
//                .noticeTitle("새 공지사항")
//                .noticeContent("내용")
//                .build();
//
//        NoticeDetailResponse response = NoticeDetailResponse.builder()
//                .noticeId(1L)
//                .noticeTitle("새 공지사항")
//                .noticeContent("내용")
//                .noticeCreatedAt(LocalDateTime.now())
//                .build();
//
//        Mockito.when(noticeService.createNotice(any(), any())).thenReturn(response);
//
//        //when
//        mockMvc.perform(post("/admin/notice/create")
//                        .header("admin_id", 1L)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                //then
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message", is("공지사항 생성 성공")))
//                .andExpect(jsonPath("$.data.noticeTitle", is("새 공지사항")));
//    }
//
//    @Test
//    void 공지사항수정_성공() throws Exception {
//        //given
//        NoticeCreationRequest request = NoticeCreationRequest.builder()
//                .noticeTitle("공지사항 수정")
//                .noticeContent("수정된 내용")
//                .build();
//
//        NoticeDetailResponse response = NoticeDetailResponse.builder()
//                .noticeId(1L)
//                .noticeTitle("공지사항 수정")
//                .noticeContent("수정된 내용")
//                .noticeCreatedAt(LocalDateTime.now())
//                .build();
//
//        Mockito.when(noticeService.updateNotice(anyLong(), any())).thenReturn(response);
//
//        //when
//        mockMvc.perform(patch("/admin/notice/update/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                //then
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message", is("공지사항 수정 성공")))
//                .andExpect(jsonPath("$.data.noticeTitle", is("공지사항 수정")));
//    }
//
//    @Test
//    void 공지사항삭제_성공() throws Exception {
//        //given
//        //when
//        mockMvc.perform(delete("/admin/notice/delete/1")
//                        .contentType(MediaType.APPLICATION_JSON))
//                //then
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message", is("공지사항 삭제 성공")))
//                .andExpect(jsonPath("$.data", is(1)));
//    }
//}
//package com.USWCicrcleLink.server.admin.notice.service;
//
//import com.USWCicrcleLink.server.admin.notice.dto.NoticeListResponse;
//import com.USWCicrcleLink.server.admin.notice.repository.NoticeRepository;
//import com.USWCicrcleLink.server.global.exception.ExceptionType;
//import com.USWCicrcleLink.server.global.exception.errortype.NoticeException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.data.domain.*;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.Mockito.*;
//
//class NoticeServiceTest {
//
//    @Mock
//    private NoticeRepository noticeRepository;
//
//    @InjectMocks
//    private NoticeService noticeService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void getNotices_ShouldReturnNotices() {
//        // Given
//        List<NoticeListResponse> notices = List.of(
//                new NoticeListResponse(1L, "공지사항 제목 1", "관리자 A", LocalDateTime.now()),
//                new NoticeListResponse(2L, "공지사항 제목 2", "관리자 A", LocalDateTime.now().minusDays(1))
//        );
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<NoticeListResponse> pagedNotices = new PageImpl<>(notices, pageable, notices.size());
//
//        when(noticeRepository.findAllNotices(pageable)).thenReturn(pagedNotices);
//
//        // When
//        Page<NoticeListResponse> result = noticeService.getNotices(pageable);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.getContent()).hasSize(2);
//        assertThat(result.getContent().get(0).getNoticeTitle()).isEqualTo("공지사항 제목 1");
//        assertThat(result.getContent().get(1).getNoticeTitle()).isEqualTo("공지사항 제목 2");
//
//        verify(noticeRepository, times(1)).findAllNotices(pageable);
//    }
//
//    @Test
//    void getNotices_ShouldThrowExceptionWhenRepositoryFails() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 10);
//        when(noticeRepository.findAllNotices(pageable)).thenThrow(new RuntimeException("Database error"));
//
//        // When & Then
//        assertThatThrownBy(() -> noticeService.getNotices(pageable))
//                .isInstanceOf(NoticeException.class)
//                .hasMessage(ExceptionType.NOTICE_CHECKING_ERROR.getMessage());
//
//        verify(noticeRepository, times(1)).findAllNotices(pageable);
//    }
//}

package com.USWCicrcleLink.server.admin.notice.service;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.notice.domain.Notice;
import com.USWCicrcleLink.server.admin.notice.dto.AdminNoticeCreationRequest;
import com.USWCicrcleLink.server.admin.notice.dto.AdminNoticePageListResponse;
import com.USWCicrcleLink.server.admin.notice.dto.AdminNoticeUpdateRequest;
import com.USWCicrcleLink.server.admin.notice.repository.NoticePhotoRepository;
import com.USWCicrcleLink.server.admin.notice.repository.NoticeRepository;
import com.USWCicrcleLink.server.global.exception.errortype.NoticeException;
import com.USWCicrcleLink.server.global.s3File.Service.S3FileUploadService;
import com.USWCicrcleLink.server.global.security.details.CustomAdminDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AdminNoticeServiceTest {

    @InjectMocks
    private AdminNoticeService adminNoticeService;

    @Mock
    private NoticeRepository noticeRepository;

    @Mock
    private NoticePhotoRepository noticePhotoRepository;

    @Mock
    private S3FileUploadService s3FileUploadService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        CustomAdminDetails adminDetails = mock(CustomAdminDetails.class);
        Admin mockAdmin = Admin.builder()
                .adminId(1L)
                .adminName("Mock Admin")
                .build();
        when(adminDetails.admin()).thenReturn(mockAdmin);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(adminDetails);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("공지사항 목록 조회 성공")
    void getNotices_success() {
        Notice notice = Notice.builder()
                .noticeUUID(UUID.randomUUID())
                .noticeTitle("Test Title")
                .noticeContent("Test Content")
                .noticeCreatedAt(LocalDateTime.now())
                .admin(Admin.builder().adminName("관리자").build())
                .build();

        Page<Notice> noticePage = new PageImpl<>(List.of(notice));
        Pageable pageable = PageRequest.of(0, 10);

        when(noticeRepository.findAll(pageable)).thenReturn(noticePage);

        AdminNoticePageListResponse result = adminNoticeService.getNotices(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNoticeTitle()).isEqualTo("Test Title");
        verify(noticeRepository).findAll(pageable);
    }

    @Test
    @DisplayName("공지사항 조회 실패 - 존재하지 않음")
    void getNoticeByUUID_notFound() {
        UUID uuid = UUID.randomUUID();
        when(noticeRepository.findByNoticeUUID(uuid)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> adminNoticeService.getNoticeByUUID(uuid))
                .isInstanceOf(NoticeException.class);
    }

    @Test
    @DisplayName("공지사항 삭제 성공")
    void deleteNotice_success() {
        UUID uuid = UUID.randomUUID();
        Notice notice = Notice.builder()
                .noticeUUID(uuid)
                .noticeTitle("삭제 대상")
                .noticeContent("삭제 내용")
                .noticeCreatedAt(LocalDateTime.now())
                .admin(Admin.builder().adminName("관리자").build())
                .build();

        when(noticeRepository.findByNoticeUUID(uuid)).thenReturn(Optional.of(notice));
        when(noticePhotoRepository.findByNotice(notice)).thenReturn(Collections.emptyList());

        adminNoticeService.deleteNotice(uuid);

        verify(noticePhotoRepository).findByNotice(notice);
        verify(noticeRepository).delete(notice);
    }

    @Test
    @DisplayName("공지사항 생성 실패 - 순서 수 맞지 않음")
    void createNotice_orderMismatch() {
        AdminNoticeCreationRequest request = new AdminNoticeCreationRequest("제목", "내용", List.of(1, 2));
        List<MultipartFile> photos = List.of(mock(MultipartFile.class));

        assertThatThrownBy(() -> adminNoticeService.createNotice(request, photos))
                .isInstanceOf(NoticeException.class);
    }

    @Test
    @DisplayName("공지사항 수정 실패 - 존재하지 않음")
    void updateNotice_notFound() {
        UUID uuid = UUID.randomUUID();
        AdminNoticeUpdateRequest request = new AdminNoticeUpdateRequest("new title", "new content", List.of());
        when(noticeRepository.findByNoticeUUID(uuid)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> adminNoticeService.updateNotice(uuid, request, List.of()))
                .isInstanceOf(NoticeException.class);
    }

    @Test
    @DisplayName("공지사항 사진 최대 업로드 제한 초과")
    void createNotice_photoLimitExceeded() {
        List<MultipartFile> photos = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            MultipartFile photo = mock(MultipartFile.class);
            when(photo.isEmpty()).thenReturn(false);
            photos.add(photo);
        }

        AdminNoticeCreationRequest request = new AdminNoticeCreationRequest("제목", "내용", List.of(0, 1, 2, 3, 4, 5));
        assertThatThrownBy(() -> adminNoticeService.createNotice(request, photos))
                .isInstanceOf(NoticeException.class);
    }
}
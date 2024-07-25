//package com.USWCicrcleLink.server.admin.notice.service;
//
//import com.USWCicrcleLink.server.admin.admin.domain.Admin;
//import com.USWCicrcleLink.server.admin.notice.domain.Notice;
//import com.USWCicrcleLink.server.admin.notice.domain.NoticePhoto;
//import com.USWCicrcleLink.server.admin.notice.dto.NoticeCreationRequest;
//import com.USWCicrcleLink.server.admin.notice.dto.NoticeDetailResponse;
//import com.USWCicrcleLink.server.admin.notice.dto.NoticeListResponse;
//import com.USWCicrcleLink.server.admin.notice.dto.NoticeListResponseAssembler;
//import com.USWCicrcleLink.server.admin.notice.repository.NoticePhotoRepository;
//import com.USWCicrcleLink.server.admin.notice.repository.NoticeRepository;
//import com.USWCicrcleLink.server.admin.admin.repository.AdminRepository;
//import com.USWCicrcleLink.server.global.FileStorageService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.web.PagedResourcesAssembler;
//import org.springframework.hateoas.PagedModel;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//class NoticeServiceTest {
//
//    @Mock
//    private NoticeRepository noticeRepository;
//
//    @Mock
//    private NoticeListResponseAssembler noticeListResponseAssembler;
//
//    @Mock
//    private AdminRepository adminRepository;
//
//    @Mock
//    private NoticePhotoRepository noticePhotoRepository;
//
//    @Mock
//    private FileStorageService fileStorageService;
//
//    @Mock
//    private PagedResourcesAssembler<Notice> pagedResourcesAssembler;
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
//    void 공지사항_전체_리스트_조회() {
//        //given
//        Admin admin = Admin.builder().adminName("관리자").build();
//        Notice notice = Notice.builder()
//                .noticeTitle("공지사항")
//                .noticeContent("공지사항 내용")
//                .noticeCreatedAt(LocalDateTime.now())
//                .admin(admin)
//                .build();
//
//        when(noticeRepository.findAll()).thenReturn(Collections.singletonList(notice));
//
//        //when
//        List<NoticeListResponse> response = noticeService.getAllNotices();
//
//        //then
//        assertNotNull(response);
//        assertEquals(1, response.size());
//        assertEquals("공지사항", response.get(0).getNoticeTitle());
//    }
//
//    @Test
//    void 공지사항_리스트_조회_페이징() {
//        //given
//        Notice notice = Notice.builder()
//                .noticeTitle("공지사항")
//                .noticeContent("공지사항 내용")
//                .noticeCreatedAt(LocalDateTime.now())
//                .build();
//
//        Page<Notice> noticePage = new PageImpl<>(Collections.singletonList(notice), PageRequest.of(0, 10), 1);
//        PagedModel<NoticeListResponse> pagedModel = PagedModel.of(Collections.singletonList(new NoticeListResponse()), new PagedModel.PageMetadata(10, 0, 1));
//
//        when(noticeRepository.findAll(any(PageRequest.class))).thenReturn(noticePage);
//        when(pagedResourcesAssembler.toModel(noticePage, noticeListResponseAssembler)).thenReturn(pagedModel);
//
//        //when
//        PagedModel<NoticeListResponse> response = noticeService.getNotices(PageRequest.of(0, 10), pagedResourcesAssembler);
//
//        //then
//        assertNotNull(response);
//        assertEquals(1, response.getMetadata().getTotalElements());
//    }
//
//    @Test
//    void 공지사항_내용_조회() {
//        //given
//        Notice notice = Notice.builder()
//                .noticeTitle("공지사항")
//                .noticeContent("공지사항 내용")
//                .noticeCreatedAt(LocalDateTime.now())
//                .build();
//
//        NoticePhoto noticePhoto = NoticePhoto.builder()
//                .noticePhotoPath("test/path")
//                .notice(notice)
//                .build();
//
//        when(noticeRepository.findById(any(Long.class))).thenReturn(Optional.of(notice));
//        when(noticePhotoRepository.findByNotice(any(Notice.class))).thenReturn(Collections.singletonList(noticePhoto));
//
//        //when
//        NoticeDetailResponse response = noticeService.getNoticeById(1L);
//
//        //then
//        assertNotNull(response);
//        assertEquals("공지사항", response.getNoticeTitle());
//        assertEquals(1, response.getNoticePhotos().size());
//    }
//
//    @Test
//    void 공지사항_생성() throws IOException {
//        //given
//        Admin admin = Admin.builder().adminName("관리자").build();
//        MultipartFile photo1 = new MockMultipartFile("photo1", "photo1.jpg", "image/jpeg", "test photo 1".getBytes());
//        MultipartFile photo2 = new MockMultipartFile("photo2", "photo2.jpg", "image/jpeg", "test photo 2".getBytes());
//
//        NoticeCreationRequest request = NoticeCreationRequest.builder()
//                .noticeTitle("새 공지사항")
//                .noticeContent("새 공지사항 내용")
//                .noticePhotos(Arrays.asList(photo1, photo2))
//                .build();
//
//        Notice notice = Notice.builder()
//                .noticeTitle(request.getNoticeTitle())
//                .noticeContent(request.getNoticeContent())
//                .noticeCreatedAt(LocalDateTime.now())
//                .admin(admin)
//                .build();
//
//        when(adminRepository.findById(any(Long.class))).thenReturn(Optional.of(admin));
//        when(noticeRepository.save(any(Notice.class))).thenReturn(notice);
//        when(noticePhotoRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
//        doNothing().when(fileStorageService).createDirectory(anyString());
//        when(fileStorageService.saveFile(any(MultipartFile.class), anyString(), anyString()))
//                .thenAnswer(invocation -> {
//                    MultipartFile file = invocation.getArgument(0);
//                    return "/test/path/" + file.getOriginalFilename();
//                });
//
//        //when
//        NoticeDetailResponse response = noticeService.createNotice(1L, request);
//
//        //then
//        assertNotNull(response);
//        assertEquals("새 공지사항", response.getNoticeTitle());
//    }
//
//    @Test
//    void 공지사항_수정() throws IOException {
//        //given
//        Notice notice = Notice.builder()
//                .noticeTitle("전 공지사항")
//                .noticeContent("전 공지사항 내용")
//                .noticeCreatedAt(LocalDateTime.now())
//                .build();
//        MultipartFile photo1 = new MockMultipartFile("photo1", "photo1.jpg", "image/jpeg", "test photo 1".getBytes());
//        MultipartFile photo2 = new MockMultipartFile("photo2", "photo2.jpg", "image/jpeg", "test photo 2".getBytes());
//
//        NoticeCreationRequest request = NoticeCreationRequest.builder()
//                .noticeTitle("수정된 공지사항")
//                .noticeContent("수정된 공지사항 내용")
//                .noticePhotos(Arrays.asList(photo1, photo2))
//                .build();
//
//        when(noticeRepository.findById(any(Long.class))).thenReturn(Optional.of(notice));
//        when(noticePhotoRepository.findByNotice(any(Notice.class))).thenReturn(Collections.emptyList());
//        when(noticeRepository.save(any(Notice.class))).thenReturn(notice);
//        when(noticePhotoRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
//        doNothing().when(fileStorageService).createDirectory(anyString());
//        when(fileStorageService.saveFile(any(MultipartFile.class), anyString(), anyString()))
//                .thenAnswer(invocation -> {
//                    MultipartFile file = invocation.getArgument(0);
//                    return "/test/path/" + file.getOriginalFilename();
//                });
//
//        //when
//        NoticeDetailResponse response = noticeService.updateNotice(1L, request);
//
//        //then
//        assertNotNull(response);
//        assertEquals("수정된 공지사항", response.getNoticeTitle());
//    }
//
//    @Test
//    void 공지사항_삭제() {
//        //given
//        Notice notice = Notice.builder()
//                .noticeTitle("공지사항")
//                .noticeContent("공지사항 내용")
//                .noticeCreatedAt(LocalDateTime.now())
//                .build();
//
//        when(noticeRepository.findById(any(Long.class))).thenReturn(Optional.of(notice));
//
//        doNothing().when(noticePhotoRepository).deleteByNotice(any(Notice.class));
//        doNothing().when(noticeRepository).delete(any(Notice.class));
//
//        //when
//        noticeService.deleteNotice(1L);
//
//        //then
//        verify(noticeRepository, times(1)).delete(any(Notice.class));
//        verify(noticePhotoRepository, times(1)).deleteByNotice(any(Notice.class));
//    }
//}

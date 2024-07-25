package com.USWCicrcleLink.server.admin.notice.service;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.admin.repository.AdminRepository;
import com.USWCicrcleLink.server.admin.notice.domain.Notice;
import com.USWCicrcleLink.server.admin.notice.domain.NoticePhoto;
import com.USWCicrcleLink.server.admin.notice.dto.NoticeCreationRequest;
import com.USWCicrcleLink.server.admin.notice.dto.NoticeDetailResponse;
import com.USWCicrcleLink.server.admin.notice.dto.NoticeListResponse;
import com.USWCicrcleLink.server.admin.notice.dto.NoticeListResponseAssembler;
import com.USWCicrcleLink.server.admin.notice.repository.NoticePhotoRepository;
import com.USWCicrcleLink.server.admin.notice.repository.NoticeRepository;
import com.USWCicrcleLink.server.global.util.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final NoticeListResponseAssembler noticeListResponseAssembler;
    private final AdminRepository adminRepository;
    private final NoticePhotoRepository noticePhotoRepository;
    private final FileUploadService fileUploadService;

    @Value("${file.noticePhoto-dir}")
    private String noticePhotoDir;

    //공지사항 전체 리스트 조회
    @Transactional(readOnly = true)
    public List<NoticeListResponse> getAllNotices() {
        return noticeRepository.findAll().stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    //공지사항 리스트 조회(페이징)
    @Transactional(readOnly = true)
    public PagedModel<NoticeListResponse> getNotices(Pageable pageable, PagedResourcesAssembler<Notice> pagedResourcesAssembler) {
        Page<Notice> noticePage = noticeRepository.findAll(pageable);
        return pagedResourcesAssembler.toModel(noticePage, noticeListResponseAssembler);
    }

    //공지사항 내용 조회
    @Transactional(readOnly = true)
    public NoticeDetailResponse getNoticeById(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다. ID: " + noticeId));

        //공지사항에 연결된 사진 조회
        List<String> noticePhotoPaths = noticePhotoRepository.findByNotice(notice).stream()
                .map(NoticePhoto::getNoticePhotoPath)
                .collect(Collectors.toList());

        return convertToDetailResponse(notice, noticePhotoPaths);
    }

    //공지사항 생성
    public NoticeDetailResponse createNotice(Long adminId, NoticeCreationRequest request, MultipartFile[] noticePhotos) throws IOException {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("관리자를 찾을 수 없습니다. ID: " + adminId));

        Notice notice = Notice.builder()
                .noticeTitle(request.getNoticeTitle())
                .noticeContent(request.getNoticeContent())
                .noticeCreatedAt(LocalDateTime.now())
                .admin(admin)
                .build();
        Notice savedNotice = noticeRepository.save(notice);

        fileUploadService.createDirectory(noticePhotoDir);

        List<NoticePhoto> savedNoticePhotos = saveNoticePhotos(noticePhotos, savedNotice);
        noticePhotoRepository.saveAll(savedNoticePhotos);

        return convertToDetailResponse(savedNotice, getPhotoPaths(savedNoticePhotos));
    }

    //공지사항 수정
    public NoticeDetailResponse updateNotice(Long noticeId, NoticeCreationRequest request, MultipartFile[] noticePhotos) throws IOException {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다. ID: " + noticeId));

        notice.updateTitle(request.getNoticeTitle());
        notice.updateContent(request.getNoticeContent());

        updateNoticePhotos(notice, noticePhotos);

        Notice updatedNotice = noticeRepository.save(notice);
        List<String> photoPaths = getNoticePhotoPaths(updatedNotice);

        return convertToDetailResponse(updatedNotice, photoPaths);
    }

    //공지사항 삭제
    public void deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다. ID: " + noticeId));

        List<NoticePhoto> photos = noticePhotoRepository.findByNotice(notice);

        //기존 사진 파일 삭제
        photos.forEach(photo -> deletePhotoFile(photo.getNoticePhotoPath()));

        //공지사항에 연결된 사진 정보 삭제
        noticePhotoRepository.deleteByNotice(notice);

        //공지사항 삭제
        noticeRepository.delete(notice);
    }

    //공지사항 사진 업로드
    private List<NoticePhoto> saveNoticePhotos(MultipartFile[] photos, Notice notice) {
        if (photos == null) {
            return new ArrayList<>();
        }
        
        //사진 배열 업로드
        return Arrays.stream(photos)
                .map(photo -> {
                    try {
                        String photoPath = fileUploadService.saveFile(photo, null, noticePhotoDir);
                        return NoticePhoto.builder()
                                .noticePhotoPath(photoPath)
                                .notice(notice)
                                .build();
                    } catch (IOException e) {
                        log.error("사진 파일 저장 중 오류가 발생했습니다.", e);
                        throw new RuntimeException("사진 파일 저장 중 오류가 발생했습니다.", e);
                    }
                })
                .collect(Collectors.toList());
    }

    //공지사항 사진 업데이트
    private void updateNoticePhotos(Notice notice, MultipartFile[] newPhotos) {
        if (newPhotos == null) {
            return;
        }

        List<NoticePhoto> existingPhotos = noticePhotoRepository.findByNotice(notice);
        List<NoticePhoto> updatedPhotos = saveNoticePhotos(newPhotos, notice);

        noticePhotoRepository.saveAll(updatedPhotos);

        List<NoticePhoto> photosToRemove = existingPhotos.stream()
                .filter(existingPhoto -> !updatedPhotos.contains(existingPhoto))
                .collect(Collectors.toList());

        photosToRemove.forEach(photo -> deletePhotoFile(photo.getNoticePhotoPath()));

        noticePhotoRepository.deleteAll(photosToRemove);
    }

    //기존 사진 삭제
    private void deletePhotoFile(String photoPath) {
        try {
            fileUploadService.deleteFile(Path.of(photoPath));
        } catch (IOException e) {
            log.error("사진 파일 삭제 중 오류가 발생했습니다.", e);
        }
    }

    //공지사항의 사진 파일 경로 목록 조회
    private List<String> getNoticePhotoPaths(Notice notice) {
        return noticePhotoRepository.findByNotice(notice).stream()
                .map(NoticePhoto::getNoticePhotoPath)
                .collect(Collectors.toList());
    }

    //공지사항 사진 객체의 파일 경로 목록 조회
    private List<String> getPhotoPaths(List<NoticePhoto> photos) {
        return photos.stream()
                .map(NoticePhoto::getNoticePhotoPath)
                .collect(Collectors.toList());
    }

    //공지사항 상세 내용 변환
    private NoticeDetailResponse convertToDetailResponse(Notice notice, List<String> noticePhotoPath) {
        return NoticeDetailResponse.builder()
                .noticeId(notice.getNoticeId())
                .noticeTitle(notice.getNoticeTitle())
                .noticeContent(notice.getNoticeContent())
                .noticePhotos(noticePhotoPath)
                .noticeCreatedAt(notice.getNoticeCreatedAt())
                .build();
    }

    //공지사항 리스트 변환
    private NoticeListResponse convertToListResponse(Notice notice) {
        return NoticeListResponse.builder()
                .noticeId(notice.getNoticeId())
                .noticeTitle(notice.getNoticeTitle())
                .adminName(notice.getAdmin().getAdminName())
                .noticeCreatedAt(notice.getNoticeCreatedAt())
                .build();
    }
}

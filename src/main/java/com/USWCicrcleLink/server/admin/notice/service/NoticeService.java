package com.USWCicrcleLink.server.admin.notice.service;

import com.USWCicrcleLink.server.admin.club.domain.Admin;
import com.USWCicrcleLink.server.admin.notice.domain.Notice;
import com.USWCicrcleLink.server.admin.notice.domain.NoticePhoto;
import com.USWCicrcleLink.server.admin.notice.dto.NoticeCreationRequest;
import com.USWCicrcleLink.server.admin.notice.dto.NoticeDetailResponse;
import com.USWCicrcleLink.server.admin.notice.dto.NoticeListResponse;
import com.USWCicrcleLink.server.admin.notice.dto.NoticeListResponseAssembler;
import com.USWCicrcleLink.server.admin.notice.repository.NoticePhotoRepository;
import com.USWCicrcleLink.server.admin.notice.repository.NoticeRepository;
import com.USWCicrcleLink.server.admin.club.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    //공지사항 전체 리스트 조회
    public List<NoticeListResponse> getAllNotices() {
        return noticeRepository.findAll().stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());
    }

    //공지사항 리스트 조회(페이징)
    public PagedModel<NoticeListResponse> getNotices(Pageable pageable, PagedResourcesAssembler<Notice> pagedResourcesAssembler) {
        Page<Notice> noticePage = noticeRepository.findAll(pageable);
        return pagedResourcesAssembler.toModel(noticePage, noticeListResponseAssembler);
    }


    //공지사항 내용 조회
    public NoticeDetailResponse getNoticeById(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId).orElse(null);
        if (notice != null) {
            List<String> photoPaths = noticePhotoRepository.findByNotice(notice).stream()
                    .map(NoticePhoto::getPhotoPath)
                    .collect(Collectors.toList());
            return convertToDetailResponse(notice, photoPaths);
        }
        return null;
    }

    //공지사항 생성
    public NoticeDetailResponse createNotice(NoticeCreationRequest request, Long adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("관리자를 찾을 수 없습니다."));

        Notice notice = Notice.builder()
                .noticeTitle(request.getNoticeTitle())
                .noticeContent(request.getNoticeContent())
                .noticeCreatedAt(LocalDateTime.now())
                .admin(admin)
                .build();

        Notice savedNotice = noticeRepository.save(notice);

        List<NoticePhoto> photos = request.getNoticePhotos().stream()
                .map(photoPath -> NoticePhoto.builder()
                        .photoPath(photoPath)
                        .notice(savedNotice)
                        .build())
                .collect(Collectors.toList());

        noticePhotoRepository.saveAll(photos);

        return convertToDetailResponse(savedNotice, photos.stream()
                .map(NoticePhoto::getPhotoPath)
                .collect(Collectors.toList()));
    }


    //공지사항 수정
    public NoticeDetailResponse updateNotice(Long noticeId, NoticeCreationRequest request) {
        Notice notice = noticeRepository.findById(noticeId).orElse(null);
        if (notice != null) {
            if (request.getNoticeTitle() != null) {
                notice.updateTitle(request.getNoticeTitle());
            }
            if (request.getNoticeContent() != null) {
                notice.updateContent(request.getNoticeContent());
            }
            if (request.getNoticePhotos() != null) {
                List<NoticePhoto> existingPhotos = noticePhotoRepository.findByNotice(notice);

                List<NoticePhoto> newPhotos = request.getNoticePhotos().stream()
                        .map(photoPath -> {
                            for (NoticePhoto existingPhoto : existingPhotos) {
                                if (existingPhoto.getPhotoPath().equals(photoPath)) {
                                    return existingPhoto;
                                }
                            }
                            return NoticePhoto.builder()
                                    .photoPath(photoPath)
                                    .notice(notice)
                                    .build();
                        })
                        .collect(Collectors.toList());

                noticePhotoRepository.saveAll(newPhotos);

                List<NoticePhoto> photosToRemove = existingPhotos.stream()
                        .filter(existingPhoto -> !newPhotos.contains(existingPhoto))
                        .collect(Collectors.toList());

                noticePhotoRepository.deleteAll(photosToRemove);
            }
            Notice updatedNotice = noticeRepository.save(notice);
            List<String> photoPaths = noticePhotoRepository.findByNotice(updatedNotice).stream()
                    .map(NoticePhoto::getPhotoPath)
                    .collect(Collectors.toList());
            return convertToDetailResponse(updatedNotice, photoPaths);
        }
        return null;
    }

    //공지사항 삭제
    public void deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId).orElse(null);
        if (notice != null) {
            noticePhotoRepository.deleteByNotice(notice);
            noticeRepository.delete(notice);
        }
    }
    
    //공지사항 상세 내용
    private NoticeDetailResponse convertToDetailResponse(Notice notice, List<String> photoPaths) {
        return NoticeDetailResponse.builder()
                .noticeId(notice.getNoticeId())
                .noticeTitle(notice.getNoticeTitle())
                .noticeContent(notice.getNoticeContent())
                .noticePhotos(photoPaths)
                .noticeCreatedAt(notice.getNoticeCreatedAt())
                .build();
    }
    
    //공지사항 리스트
    private NoticeListResponse convertToListResponse(Notice notice) {
        return NoticeListResponse.builder()
                .noticeId(notice.getNoticeId())
                .noticeTitle(notice.getNoticeTitle())
                .adminName(notice.getAdmin().getAdminName())
                .noticeCreatedAt(notice.getNoticeCreatedAt())
                .build();
    }
}

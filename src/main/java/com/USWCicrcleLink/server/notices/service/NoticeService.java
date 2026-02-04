package com.USWCicrcleLink.server.notices.service;

import com.USWCicrcleLink.server.admin.domain.Admin;
import com.USWCicrcleLink.server.notices.domain.Notice;
import com.USWCicrcleLink.server.notices.domain.NoticePhoto;
import com.USWCicrcleLink.server.notices.dto.*;
import com.USWCicrcleLink.server.notices.repository.NoticePhotoRepository;
import com.USWCicrcleLink.server.notices.repository.NoticeRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.NoticeException;
import com.USWCicrcleLink.server.global.security.details.CustomAdminDetails;
import com.USWCicrcleLink.server.global.s3File.Service.S3FileUploadService;
import com.USWCicrcleLink.server.global.s3File.dto.S3FileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class NoticeService {

    private static final int FILE_LIMIT = 5;
    private static final String S3_NOTICE_PHOTO_DIR = "noticePhoto/";
    private final NoticeRepository noticeRepository;
    private final NoticePhotoRepository noticePhotoRepository;
    private final S3FileUploadService s3FileUploadService;

    /**
     * 공지사항 리스트 조회 (ADMIN)
     */
    @Transactional(readOnly = true)
    public NoticePageResponse getNotices(Pageable pageable) {
        Page<Notice> notices = noticeRepository.findAll(pageable);

        log.debug("공지사항 목록 조회 성공 - 총 {}개", notices.getTotalElements());

        List<NoticeListResponse> content = notices.getContent().stream()
                .map(notice -> NoticeListResponse.builder()
                        .noticeUUID(notice.getNoticeUUID())
                        .noticeTitle(notice.getNoticeTitle())
                        .authorName(notice.getAdmin() != null ? notice.getAdmin().getAdminName() : "Unknown")
                        .noticeCreatedAt(notice.getNoticeCreatedAt())
                        .build())
                .toList();

        return NoticePageResponse.builder()
                .content(content)
                .totalPages(notices.getTotalPages())
                .totalElements(notices.getTotalElements())
                .currentPage(notices.getNumber())
                .build();
    }

    /**
     * 공지사항 조회 (ADMIN, USER)
     */
    @Transactional(readOnly = true)
    public NoticeDetailResponse getNoticeByUUID(UUID noticeUUID) {
        Notice notice = noticeRepository.findByNoticeUUID(noticeUUID)
                .orElseThrow(() -> new NoticeException(ExceptionType.NOTICE_NOT_EXISTS));

        List<String> noticePhotoUrls = noticePhotoRepository.findByNotice(notice).stream()
                .sorted(Comparator.comparingInt(NoticePhoto::getOrder))
                .map(photo -> s3FileUploadService.generatePresignedGetUrl(photo.getNoticePhotoS3Key()))
                .collect(Collectors.toList());

        log.debug("공지사항 상세 조회 성공 - ID: {}", notice.getNoticeId());

        return new NoticeDetailResponse(
                notice.getNoticeUUID(),
                notice.getNoticeTitle(),
                notice.getNoticeContent(),
                noticePhotoUrls,
                notice.getNoticeCreatedAt(),
                notice.getAdmin() != null ? notice.getAdmin().getAdminName() : "Unknown");
    }

    /**
     * 공지사항 생성 (ADMIN)
     */
    public List<String> createNotice(NoticeRequest request, List<MultipartFile> noticePhotos) {
        if (request == null) {
            throw new NoticeException(ExceptionType.INVALID_INPUT);
        }
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Notice.NoticeBuilder noticeBuilder = Notice.builder()
                .noticeTitle(request.getNoticeTitle())
                .noticeContent(request.getNoticeContent())
                .noticeCreatedAt(LocalDateTime.now());

        if (principal instanceof CustomAdminDetails) {
            noticeBuilder.admin(((CustomAdminDetails) principal).admin());
        } else {
            throw new NoticeException(ExceptionType.NOTICE_NOT_AUTHOR);
        }

        Notice savedNotice = noticeRepository.save(noticeBuilder.build());

        // 사진 순서와 파일 개수 검증
        log.info("공지사항 생성 요청 - NoticeID: {}, 사진 수: {}, 순서 정보 수: {}",
                savedNotice.getNoticeId(),
                noticePhotos == null ? 0 : noticePhotos.size(),
                request.getPhotoOrders() == null ? 0 : request.getPhotoOrders().size());

        validatePhotoOrdersAndPhotos(request.getPhotoOrders(), noticePhotos);

        // 사진 처리
        List<String> presignedUrls = handleNoticePhotos(savedNotice, noticePhotos, request.getPhotoOrders());

        log.info("공지사항 생성 완료 - NoticeID: {}, 첨부된 사진 수: {}", savedNotice.getNoticeId(),
                noticePhotos == null ? 0 : noticePhotos.size());
        return presignedUrls;
    }

    /**
     * 공지사항 수정 (ADMIN)
     */
    public NoticeDetailResponse updateNotice(UUID noticeUUID, NoticeUpdateRequest request,
            List<MultipartFile> noticePhotos) {
        if (request == null) {
            throw new NoticeException(ExceptionType.INVALID_INPUT);
        }

        // 공지사항 조회
        Notice notice = noticeRepository.findByNoticeUUID(noticeUUID)
                .orElseThrow(() -> new NoticeException(ExceptionType.NOTICE_NOT_EXISTS));

        // 작성자 권한 확인
        validateNoticeAuthor(notice);

        notice.updateTitle(request.getNoticeTitle());
        notice.updateContent(request.getNoticeContent());

        List<String> photoUrls;

        // 사진 변경 요청이 있는 경우에만 처리 (사진 or 순서 정보가 존재할 때)
        boolean hasPhotos = noticePhotos != null && !noticePhotos.isEmpty();
        boolean hasOrders = request.getPhotoOrders() != null && !request.getPhotoOrders().isEmpty();

        if (hasPhotos || hasOrders) {
            // 기존 사진 삭제
            deleteExistingPhotos(notice);

            // 사진 순서와 파일 개수 검증
            validatePhotoOrdersAndPhotos(request.getPhotoOrders(), noticePhotos);

            // 사진 처리 (Presigned URLs 반환)
            photoUrls = handleNoticePhotos(notice, noticePhotos, request.getPhotoOrders());
            log.info("공지사항 수정 완료 - ID: {}, 첨부된 사진 수: {}", notice.getNoticeId(),
                    noticePhotos == null ? 0 : noticePhotos.size());

        } else {
            // 사진 변경 없음 - 기존 사진 Presigned URL 재발급
            photoUrls = noticePhotoRepository.findByNotice(notice).stream()
                    .sorted(Comparator.comparingInt(NoticePhoto::getOrder))
                    .map(photo -> s3FileUploadService.generatePresignedGetUrl(photo.getNoticePhotoS3Key()))
                    .collect(Collectors.toList());

            log.info("공지사항 수정 완료 (사진 변경 없음) - ID: {}", notice.getNoticeId());
        }

        return new NoticeDetailResponse(
                notice.getNoticeUUID(),
                notice.getNoticeTitle(),
                notice.getNoticeContent(),
                photoUrls,
                notice.getNoticeCreatedAt(),
                notice.getAdmin() != null ? notice.getAdmin().getAdminName() : "Unknown");
    }

    /**
     * 공지사항 삭제 (ADMIN)
     */
    public void deleteNotice(UUID noticeUUID) {

        Notice notice = noticeRepository.findByNoticeUUID(noticeUUID)
                .orElseThrow(() -> new NoticeException(ExceptionType.NOTICE_NOT_EXISTS));

        // 작성자 권한 확인
        validateNoticeAuthor(notice);

        deleteExistingPhotos(notice);

        noticeRepository.delete(notice);
        log.info("공지사항 삭제 완료 - ID: {}", notice.getNoticeId());
    }

    /**
     * 인증된 사용자 정보 가져오기 (Helper method if needed, currently replaced by inline
     * checks)
     */
    // Removed getAuthenticatedAdmin as it's no longer sufficient for both types

    // 사진 순서와 파일 개수 검증
    private void validatePhotoOrdersAndPhotos(List<Integer> photoOrders, List<MultipartFile> noticePhotos) {
        boolean hasPhotos = noticePhotos != null && !noticePhotos.isEmpty();
        boolean hasOrders = photoOrders != null && !photoOrders.isEmpty();

        if (hasPhotos) {
            // 사진은 있는데 순서가 없거나 개수가 안 맞는 경우
            if (!hasOrders || noticePhotos.size() != photoOrders.size()) {
                throw new NoticeException(ExceptionType.PHOTO_ORDER_MISMATCH);
            }
            // 사진 개수 제한 확인
            if (noticePhotos.size() > FILE_LIMIT) {
                throw new NoticeException(ExceptionType.UP_TO_5_PHOTOS_CAN_BE_UPLOADED);
            }
        } else {
            // 사진은 없는데 순서 정보만 있는 경우
            if (hasOrders) {
                throw new NoticeException(ExceptionType.PHOTO_ORDER_MISMATCH);
            }
        }
    }

    // 기존 사진 일괄 삭제
    private void deleteExistingPhotos(Notice notice) {
        List<NoticePhoto> existingPhotos = noticePhotoRepository.findByNotice(notice);
        if (existingPhotos.isEmpty())
            return;

        List<String> fileNames = existingPhotos.stream()
                .map(NoticePhoto::getNoticePhotoS3Key)
                .toList();

        s3FileUploadService.deleteFiles(fileNames);
        noticePhotoRepository.deleteAllInBatch(existingPhotos);

        log.debug("공지사항 사진 일괄 삭제 완료 - ID: {}, 삭제된 파일 수: {}", notice.getNoticeId(), fileNames.size());
    }

    // 사진 업로드
    private List<String> handleNoticePhotos(Notice notice, List<MultipartFile> noticePhotos,
            List<Integer> photoOrders) {
        if (noticePhotos == null || noticePhotos.isEmpty()) {
            log.info("처리할 사진이 없습니다.");
            return List.of();
        }

        List<NoticePhoto> newPhotoList = new ArrayList<>();
        List<String> presignedUrls = new ArrayList<>();

        for (int i = 0; i < noticePhotos.size(); i++) {
            MultipartFile noticePhoto = noticePhotos.get(i);
            int order = photoOrders.get(i);

            if (noticePhoto == null || noticePhoto.isEmpty()) {
                log.warn("공지사항 사진 업로드 실패 - 빈 파일 포함됨, 순서: {}", order);
                continue;
            }

            S3FileResponse s3FileResponse = s3FileUploadService.uploadFile(noticePhoto, S3_NOTICE_PHOTO_DIR);

            NoticePhoto newPhoto = NoticePhoto.builder()
                    .notice(notice)
                    .noticePhotoName(noticePhoto.getOriginalFilename())
                    .noticePhotoS3Key(s3FileResponse.getS3FileName())
                    .order(order)
                    .build();

            newPhotoList.add(newPhoto);
            presignedUrls.add(s3FileResponse.getPresignedUrl());
        }

        noticePhotoRepository.saveAll(newPhotoList);

        log.debug("공지사항 사진 일괄 업로드 완료 - 총 {}개", newPhotoList.size());
        log.debug("공지사항 사진 일괄 업로드 완료 - 총 {}개", newPhotoList.size());
        return presignedUrls;
    }

    // 작성자 권한 검증
    private void validateNoticeAuthor(Notice notice) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof CustomAdminDetails) {
            Admin currentAdmin = ((CustomAdminDetails) principal).admin();
            if (notice.getAdmin() == null || !notice.getAdmin().getAdminUUID().equals(currentAdmin.getAdminUUID())) {
                throw new NoticeException(ExceptionType.NOTICE_NOT_AUTHOR);
            }
        } else {
            throw new NoticeException(ExceptionType.NOTICE_NOT_AUTHOR);
        }
    }
}

package com.USWCicrcleLink.server.admin.notice.service;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.notice.domain.Notice;
import com.USWCicrcleLink.server.admin.notice.domain.NoticePhoto;
import com.USWCicrcleLink.server.admin.notice.dto.*;
import com.USWCicrcleLink.server.admin.notice.repository.NoticePhotoRepository;
import com.USWCicrcleLink.server.admin.notice.repository.NoticeRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.NoticeException;
import com.USWCicrcleLink.server.global.security.util.CustomAdminDetails;
import com.USWCicrcleLink.server.global.util.s3File.Service.S3FileUploadService;
import com.USWCicrcleLink.server.global.util.s3File.dto.S3FileResponse;
import com.USWCicrcleLink.server.global.util.validator.InputValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class NoticeService {

    private static final int FILE_LIMIT = 5; // 최대 업로드 가능한 파일 수
    private static final String S3_NOTICE_PHOTO_DIR = "noticePhoto/"; // 공지사항 사진 경로
    private final NoticeRepository noticeRepository;
    private final NoticeListResponseAssembler noticeListResponseAssembler;
    private final NoticePhotoRepository noticePhotoRepository;
    private final S3FileUploadService s3FileUploadService;

    // 공지사항 리스트 조회(페이징)
    @Transactional(readOnly = true)
    public PagedModel<NoticeListResponse> getNotices(Pageable pageable, PagedResourcesAssembler<Notice> pagedResourcesAssembler) {
        Page<Notice> noticePage = noticeRepository.findAll(pageable);
        return pagedResourcesAssembler.toModel(noticePage, noticeListResponseAssembler);
    }

    // 공지사항 세부 내용 조회
    @Transactional(readOnly = true)
    public NoticeDetailResponse getNoticeById(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeException(ExceptionType.NOTICE_NOT_EXISTS));

        List<String> noticePhotoUrls = noticePhotoRepository.findByNotice(notice).stream()
                .sorted(Comparator.comparingInt(NoticePhoto::getOrder))
                .map(photo -> s3FileUploadService.generatePresignedGetUrl(photo.getNoticePhotoS3Key()))
                .collect(Collectors.toList());

        return NoticeDetailResponse.from(notice, noticePhotoUrls);
    }

    // 공지사항 생성(웹)
    public NoticeDetailResponse createNotice(NoticeCreationRequest request, List<MultipartFile> noticePhotos) {
        Admin admin = getAuthenticatedAdmin();

        // 제목과 내용이 null이거나 비어있을 경우 예외 처리
        validateNoticeTitleAndContent(request.getNoticeTitle(), request.getNoticeContent());

        // 입력값 검증 (XSS 공격 방지)
        String sanitizedTitle = InputValidator.sanitizeContent(request.getNoticeTitle());
        String sanitizedContent = InputValidator.sanitizeContent(request.getNoticeContent());

        Notice notice = Notice.builder()
                .noticeTitle(sanitizedTitle)
                .noticeContent(sanitizedContent)
                .noticeCreatedAt(LocalDateTime.now())
                .admin(admin)
                .build();
        Notice savedNotice = noticeRepository.save(notice);

        // 사진 처리
        List<String> presignedUrls = handleNoticePhotos(savedNotice, noticePhotos, request.getPhotoOrders());

        return NoticeDetailResponse.from(savedNotice, presignedUrls);
    }

    // 공지사항 수정(웹)
    public NoticeDetailResponse updateNotice(Long noticeId, NoticeUpdateRequest request, List<MultipartFile> noticePhotos, List<Long> deletedPhotos) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeException(ExceptionType.NOTICE_NOT_EXISTS));

        // 제목과 내용이 null이거나 비어있을 경우 예외 처리
        validateNoticeTitleAndContent(request.getNoticeTitle(), request.getNoticeContent());

        String sanitizedTitle = InputValidator.sanitizeContent(request.getNoticeTitle());
        String sanitizedContent = InputValidator.sanitizeContent(request.getNoticeContent());

        notice.updateTitle(sanitizedTitle);
        notice.updateContent(sanitizedContent);

        // 실제로 삭제할 사진 처리
        if (deletedPhotos != null && !deletedPhotos.isEmpty()) {
            for (Long photoId : deletedPhotos) {
                NoticePhoto photo = noticePhotoRepository.findById(photoId)
                        .orElseThrow(() -> new NoticeException(ExceptionType.NOTICE_PHOTO_NOT_EXISTS));

                s3FileUploadService.deleteFile(photo.getNoticePhotoS3Key());  // S3에서 삭제
                noticePhotoRepository.delete(photo);  // DB에서 삭제
            }
        }

        // 새로운 사진 추가 및 순서 지정
        List<String> presignedUrls = handleNoticePhotos(notice, noticePhotos, request.getPhotoOrders());

        // 기존 및 새 사진 URL 생성
        List<String> photoUrls = noticePhotoRepository.findByNotice(notice).stream()
                .sorted(Comparator.comparingInt(NoticePhoto::getOrder))
                .map(photo -> s3FileUploadService.generatePresignedGetUrl(photo.getNoticePhotoS3Key()))
                .collect(Collectors.toList());
        photoUrls.addAll(presignedUrls);

        return NoticeDetailResponse.from(notice, photoUrls);
    }

    // 공지사항 삭제(웹)
    public void deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeException(ExceptionType.NOTICE_NOT_EXISTS));

        List<NoticePhoto> photos = noticePhotoRepository.findByNotice(notice);

        photos.forEach(photo -> s3FileUploadService.deleteFile(photo.getNoticePhotoS3Key()));
        noticePhotoRepository.deleteAll(photos);
        noticeRepository.delete(notice);
    }

    // 새로운 파일 업로드 및 사진 정보 업데이트
    private S3FileResponse updateNoticePhotoAndS3File(MultipartFile noticePhoto, NoticePhoto newPhoto, int order) {
        S3FileResponse s3FileResponse = s3FileUploadService.uploadFile(noticePhoto, S3_NOTICE_PHOTO_DIR);
        newPhoto.updateNoticePhoto(noticePhoto.getOriginalFilename(), s3FileResponse.getS3FileName(), order);
        noticePhotoRepository.save(newPhoto);
        log.debug("새로운 사진 정보 저장 및 업데이트 완료: {}", s3FileResponse.getS3FileName());
        return s3FileResponse;
    }

    // 인증된 관리자 정보 가져오기
    private Admin getAuthenticatedAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomAdminDetails adminDetails = (CustomAdminDetails) authentication.getPrincipal();
        return adminDetails.admin();
    }

    // 제목과 내용 검증
    private void validateNoticeTitleAndContent(String title, String content) {
        if (!StringUtils.hasText(title) || !StringUtils.hasText(content)) {
            throw new NoticeException(ExceptionType.TITEL_AND_CONENT_REQUIRED);
        }
    }

    // 사진 처리 로직
    private List<String> handleNoticePhotos(Notice notice, List<MultipartFile> noticePhotos, List<Integer> photoOrders) {
        List<String> presignedUrls = new ArrayList<>();
        if (noticePhotos != null && !noticePhotos.isEmpty() && photoOrders != null) {
            if (noticePhotos.size() > FILE_LIMIT) {
                throw new NoticeException(ExceptionType.UP_TO_5_PHOTOS_CAN_BE_UPLOADED);
            }

            for (int i = 0; i < noticePhotos.size(); i++) {
                MultipartFile noticePhoto = noticePhotos.get(i);
                int order = photoOrders.get(i);

                if (noticePhoto == null || noticePhoto.isEmpty()) {
                    continue;
                }

                NoticePhoto newPhoto = new NoticePhoto();
                newPhoto.setNotice(notice);
                newPhoto.setOrder(order);

                S3FileResponse s3FileResponse = updateNoticePhotoAndS3File(noticePhoto, newPhoto, order);
                presignedUrls.add(s3FileResponse.getPresignedUrl());
            }
        }
        return presignedUrls;
    }
}
package com.USWCicrcleLink.server.notices.api;

import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.validation.ValidationSequence;
import com.USWCicrcleLink.server.notices.dto.NoticeDetailResponse;
import com.USWCicrcleLink.server.notices.dto.NoticePageResponse;
import com.USWCicrcleLink.server.notices.dto.NoticeRequest;
import com.USWCicrcleLink.server.notices.dto.NoticeUpdateRequest;
import com.USWCicrcleLink.server.notices.service.NoticeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
@Tag(name = "Notices", description = "공지사항 API")
public class NoticeController {

    private final NoticeService noticeService;

    // 공지사항 리스트 조회 (Public - Paged)
    @GetMapping
    public ResponseEntity<ApiResponse<NoticePageResponse>> getNotices(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("noticeCreatedAt").descending());
        NoticePageResponse pagedNotices = noticeService.getNotices(pageable);
        return ResponseEntity.ok(new ApiResponse<>("공지사항 리스트 조회 성공", pagedNotices));
    }

    // 공지사항 상세 조회 (Public)
    @GetMapping("/{noticeUUID}")
    public ResponseEntity<ApiResponse<NoticeDetailResponse>> getNoticeByUUID(
            @PathVariable("noticeUUID") UUID noticeUUID) {
        NoticeDetailResponse notice = noticeService.getNoticeByUUID(noticeUUID);
        return ResponseEntity.ok(new ApiResponse<>("공지사항 조회 성공", notice));
    }

    // 공지사항 생성 (Admin)
    @PostMapping
    public ResponseEntity<ApiResponse<List<String>>> createNotice(
            @RequestPart(value = "request", required = false) @Validated(ValidationSequence.class) NoticeRequest request,
            @RequestPart(value = "photos", required = false) List<MultipartFile> noticePhotos) {
        List<String> presignedUrls = noticeService.createNotice(request, noticePhotos);
        return ResponseEntity.ok(new ApiResponse<>("공지사항 생성 성공", presignedUrls));
    }

    // 공지사항 수정 (Admin)
    @PutMapping("/{noticeUUID}")
    public ResponseEntity<ApiResponse<List<String>>> updateNotice(
            @PathVariable("noticeUUID") UUID noticeUUID,
            @RequestPart(value = "request", required = false) @Validated(ValidationSequence.class) NoticeUpdateRequest request,
            @RequestPart(value = "photos", required = false) List<MultipartFile> noticePhotos) {

        List<String> presignedUrls = noticeService.updateNotice(noticeUUID, request, noticePhotos);
        return ResponseEntity.ok(new ApiResponse<>("공지사항 수정 성공", presignedUrls));
    }

    // 공지사항 삭제 (Admin)
    @DeleteMapping("/{noticeUUID}")
    public ResponseEntity<ApiResponse<UUID>> deleteNotice(@PathVariable("noticeUUID") UUID noticeUUID) {
        noticeService.deleteNotice(noticeUUID);
        return ResponseEntity.ok(new ApiResponse<>("공지사항 삭제 성공", noticeUUID));
    }
}


package com.USWCicrcleLink.server.admin.notice.api;

import com.USWCicrcleLink.server.admin.notice.domain.Notice;
import com.USWCicrcleLink.server.admin.notice.dto.NoticeCreationRequest;
import com.USWCicrcleLink.server.admin.notice.dto.NoticeDetailResponse;
import com.USWCicrcleLink.server.admin.notice.dto.NoticeListResponse;
import com.USWCicrcleLink.server.admin.notice.dto.NoticeUpdateRequest;
import com.USWCicrcleLink.server.admin.notice.service.NoticeService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {
    private final NoticeService noticeService;

    //공지사항 리스트 조회(웹, 페이징)
    @GetMapping()
    public ResponseEntity<ApiResponse<Page<NoticeListResponse>>> getNotices(
            @RequestParam(name = "page",defaultValue = "0") int page,
            @RequestParam(name = "size",defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("noticeCreatedAt").descending());
        Page<NoticeListResponse> pagedNotices = noticeService.getNotices(pageable);
        ApiResponse<Page<NoticeListResponse>> response = new ApiResponse<>("공지사항 리스트 조회 성공", pagedNotices);
        return ResponseEntity.ok(response);
    }

    //공지사항 세부내용 조회(웹)
    @GetMapping("/{noticeUUID}")
    public ResponseEntity<ApiResponse<NoticeDetailResponse>> getNoticeByUUID(@PathVariable("noticeUUID") UUID noticeUUID) {
        NoticeDetailResponse notice = noticeService.getNoticeByUUID(noticeUUID);
        ApiResponse<NoticeDetailResponse> response = new ApiResponse<>("공지사항 조회 성공", notice);
        return ResponseEntity.ok(response);
    }

    // 공지사항 생성(웹)
    @PostMapping()
    public ResponseEntity<ApiResponse<List<String>>> createNotice(
            @RequestPart(value = "request", required = false) @Valid NoticeCreationRequest request,
            @RequestPart(value = "photos", required = false) List<MultipartFile> noticePhotos) {
        List<String> presignedUrls = noticeService.createNotice(request, noticePhotos);
        ApiResponse<List<String>> response = new ApiResponse<>("공지사항 생성 성공", presignedUrls);
        return ResponseEntity.ok(response);
    }

    // 공지사항 수정(웹)
    @PutMapping("/{noticeUUID}")
    public ResponseEntity<ApiResponse<List<String>>> updateNotice(
            @PathVariable("noticeUUID") UUID noticeUUID,
            @RequestPart(value = "request", required = false) @Valid NoticeUpdateRequest request,
            @RequestPart(value = "photos", required = false) List<MultipartFile> noticePhotos) {
        List<String> presignedUrls = noticeService.updateNotice(noticeUUID, request, noticePhotos);
        ApiResponse<List<String>> response = new ApiResponse<>("공지사항 수정 성공", presignedUrls);
        return ResponseEntity.ok(response);
    }

    //공지사항 삭제(웹)
    @DeleteMapping("/{noticeUUID}")
    public ResponseEntity<ApiResponse<UUID>> deleteNotice(@PathVariable("noticeUUID") UUID noticeUUID) {
        noticeService.deleteNotice(noticeUUID);
        ApiResponse<UUID> response = new ApiResponse<>("공지사항 삭제 성공", noticeUUID);
        return ResponseEntity.ok(response);
    }
}

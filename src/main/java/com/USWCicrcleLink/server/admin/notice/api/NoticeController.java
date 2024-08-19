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
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {
    private final NoticeService noticeService;

    //공지사항 리스트 조회(페이징)(웹)
    @GetMapping("/paged")
    public ResponseEntity<PagedModel<NoticeListResponse>> getNotices(Pageable pageable, PagedResourcesAssembler<Notice> pagedResourcesAssembler) {
        PagedModel<NoticeListResponse> pagedNotices = noticeService.getNotices(pageable, pagedResourcesAssembler);
        return ResponseEntity.ok(pagedNotices);
    }

    //공지사항 내용 조회(웹)
    @GetMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<NoticeDetailResponse>> getNoticeById(@PathVariable("noticeId") Long noticeId) {
        NoticeDetailResponse notice = noticeService.getNoticeById(noticeId);
        ApiResponse<NoticeDetailResponse> response = new ApiResponse<>("공지사항 조회 성공", notice);
        return ResponseEntity.ok(response);
    }

    //공지사항 생성(웹)
    @PostMapping()
    public ResponseEntity<ApiResponse<NoticeDetailResponse>> createNotice(
            @RequestPart(value = "request", required = false) @Valid NoticeCreationRequest request,
            //사진 배열 처리
            @RequestPart(value = "photos", required = false) List<MultipartFile> noticePhotos) {

        NoticeDetailResponse createdNotice = noticeService.createNotice(request, noticePhotos);
        ApiResponse<NoticeDetailResponse> response = new ApiResponse<>("공지사항 생성 성공", createdNotice);
        return ResponseEntity.ok(response);
    }

    //공지사항 수정(웹)
    @PatchMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<NoticeDetailResponse>> updateNotice(
            @PathVariable("noticeId") Long noticeId,
            @RequestPart(value = "request", required = false) @Valid NoticeUpdateRequest request,
            //사진 배열 처리
            @RequestPart(value = "photos", required = false) List<MultipartFile> noticePhotos) {

        NoticeDetailResponse updatedNotice = noticeService.updateNotice(noticeId, request, noticePhotos);
        ApiResponse<NoticeDetailResponse> response = new ApiResponse<>("공지사항 수정 성공", updatedNotice);
        return ResponseEntity.ok(response);
    }

    //공지사항 삭제(웹)
    @DeleteMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<Long>> deleteNotice(@PathVariable("noticeId") Long noticeId) {
        noticeService.deleteNotice(noticeId);
        ApiResponse<Long> response = new ApiResponse<>("공지사항 삭제 성공", noticeId);
        return ResponseEntity.ok(response);
    }
}

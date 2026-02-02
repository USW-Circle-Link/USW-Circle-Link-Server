package com.USWCicrcleLink.server.user.api;

import com.USWCicrcleLink.server.notices.dto.NoticeDetailResponse;
import com.USWCicrcleLink.server.notices.service.NoticeService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.user.dto.MyNoticeResponse;
import com.USWCicrcleLink.server.user.service.MyNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/my-notices")
@RequiredArgsConstructor
public class MyNoticeController {
    private final MyNoticeService myNoticeService;
    private final NoticeService noticeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MyNoticeResponse>>> getNotices() {
        List<MyNoticeResponse> notices = myNoticeService.getNotices();
        ApiResponse<List<MyNoticeResponse>> response = new ApiResponse<>("공지사항 조회 성공", notices);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{noticeUUID}/details")
    public ResponseEntity<ApiResponse<NoticeDetailResponse>> getNoticeByUUID(
            @PathVariable("noticeUUID") UUID noticeUUID) {
        NoticeDetailResponse notice = noticeService.getNoticeByUUID(noticeUUID);
        ApiResponse<NoticeDetailResponse> response = new ApiResponse<>("공지사항 세부 조회 성공", notice);
        return ResponseEntity.ok(response);
    }
}

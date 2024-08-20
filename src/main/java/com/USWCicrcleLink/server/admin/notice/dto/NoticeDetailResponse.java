package com.USWCicrcleLink.server.admin.notice.dto;

import com.USWCicrcleLink.server.admin.notice.domain.Notice;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoticeDetailResponse {
    private Long noticeId;
    private String noticeTitle;
    private String noticeContent;
    private List<String> noticePhotos;
    private LocalDateTime noticeCreatedAt;
    private String adminName;

    public static NoticeDetailResponse from(Notice notice, List<String> noticePhotoPath) {
        return NoticeDetailResponse.builder()
                .noticeId(notice.getNoticeId())
                .noticeTitle(notice.getNoticeTitle())
                .noticeContent(notice.getNoticeContent())
                .noticePhotos(noticePhotoPath)
                .noticeCreatedAt(notice.getNoticeCreatedAt())
                .adminName(notice.getAdmin().getAdminName())
                .build();
    }
}
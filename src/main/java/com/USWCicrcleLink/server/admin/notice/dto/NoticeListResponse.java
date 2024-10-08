package com.USWCicrcleLink.server.admin.notice.dto;

import com.USWCicrcleLink.server.admin.notice.domain.Notice;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoticeListResponse extends RepresentationModel<NoticeListResponse> {
    private Long noticeId;
    private String noticeTitle;
    private String adminName;
    private LocalDateTime noticeCreatedAt;

    public static NoticeListResponse from(Notice notice) {
        return NoticeListResponse.builder()
                .noticeId(notice.getNoticeId())
                .noticeTitle(notice.getNoticeTitle())
                .adminName(notice.getAdmin().getAdminName())
                .noticeCreatedAt(notice.getNoticeCreatedAt())
                .build();
    }
}
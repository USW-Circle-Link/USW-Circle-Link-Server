package com.USWCicrcleLink.server.admin.notice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminNoticeListResponse {
    private UUID noticeUUID;
    private String noticeTitle;
    private String adminName;
    private LocalDateTime noticeCreatedAt;
}

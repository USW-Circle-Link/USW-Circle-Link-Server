package com.USWCicrcleLink.server.notices.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoticeListResponse {
    private UUID noticeUUID;
    private String noticeTitle;
    private String adminName;
    private LocalDateTime noticeCreatedAt;
}


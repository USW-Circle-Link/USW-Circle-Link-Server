package com.USWCicrcleLink.server.admin.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoticeDetailResponse {
    private UUID noticeUUID;
    private String noticeTitle;
    private String noticeContent;
    private List<String> noticePhotos;
    private LocalDateTime noticeCreatedAt;
    private String adminName;
}
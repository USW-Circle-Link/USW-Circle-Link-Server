package com.USWCicrcleLink.server.admin.notice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoticeDetailResponse{
    private Long noticeId;
    private String noticeTitle;
    private String noticeContent;
    private List<String> noticePhotos;
    private LocalDateTime noticeCreatedAt;
}
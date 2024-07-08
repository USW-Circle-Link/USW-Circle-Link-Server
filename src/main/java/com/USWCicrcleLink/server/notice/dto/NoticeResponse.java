package com.USWCicrcleLink.server.notice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeResponse {
    private String noticeTitle;

    private String noticeContent;

    private String adminNickname;

    private LocalDateTime noticeCreatedAt;

    private LocalDateTime noticeUpdatedAt;
}

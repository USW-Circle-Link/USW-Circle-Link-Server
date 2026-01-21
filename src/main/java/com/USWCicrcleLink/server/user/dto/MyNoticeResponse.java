package com.USWCicrcleLink.server.user.dto;

import com.USWCicrcleLink.server.admin.notice.domain.Notice;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyNoticeResponse {
    private UUID noticeUUID;
    private String noticeTitle;
    private String adminName;
    private LocalDateTime noticeCreatedAt;

    public static MyNoticeResponse from(Notice notice) {
        return new MyNoticeResponse(
                notice.getNoticeUUID(),
                notice.getNoticeTitle(),
                notice.getAdmin().getAdminName(),
                notice.getNoticeCreatedAt()
        );
    }
}

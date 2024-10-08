package com.USWCicrcleLink.server.admin.notice.domain;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "NOTICE_TABLE")
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long noticeId;

    @Column(name = "notice_title", length = 100, nullable = false)
    private String noticeTitle;

    @Lob
    @Column(name = "notice_content", nullable = false)
    private String noticeContent;

    @Column(name = "notice_created_at")
    private LocalDateTime noticeCreatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    public void updateTitle(String noticeTitle) {
        this.noticeTitle = noticeTitle;
    }

    public void updateContent(String noticeContent) {
        this.noticeContent = noticeContent;
    }
}
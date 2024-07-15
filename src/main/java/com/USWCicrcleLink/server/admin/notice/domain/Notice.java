package com.USWCicrcleLink.server.admin.notice.domain;

import com.USWCicrcleLink.server.admin.club.domain.Admin;
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

    @Column(name = "notice_title")
    private String noticeTitle;

    @Column(name = "notice_content")
    private String noticeContent;

    @Column(name = "notice_created_at")
    private LocalDateTime noticeCreatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    public void updateTitle(String noticeTitle) {
        this.noticeTitle = noticeTitle;
    }

    public void updateContent(String noticeContent) {
        this.noticeContent = noticeContent;
    }
}
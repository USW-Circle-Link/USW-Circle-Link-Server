package com.USWCicrcleLink.server.notices.domain;

import com.USWCicrcleLink.server.admin.domain.Admin;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

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

    @Builder.Default
    @Column(name = "notice_uuid", unique = true, nullable = false, updatable = false)
    private UUID noticeUUID = UUID.randomUUID();

    @Column(name = "notice_title", length = 200, nullable = false)
    private String noticeTitle;

    @Column(name = "notice_content", length = 3000, nullable = false)
    private String noticeContent;

    @Column(name = "notice_created_at", nullable = false, updatable = false)
    private LocalDateTime noticeCreatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Admin admin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id")
    private com.USWCicrcleLink.server.club.leader.domain.Leader leader;

    @PrePersist
    public void prePersist() {
        if (noticeUUID == null) {
            this.noticeUUID = UUID.randomUUID();
        }
        if (this.noticeCreatedAt == null) {
            this.noticeCreatedAt = LocalDateTime.now();
        }
    }

    public void updateTitle(String noticeTitle) {
        this.noticeTitle = noticeTitle;
    }

    public void updateContent(String noticeContent) {
        this.noticeContent = noticeContent;
    }
}

package com.USWCicrcleLink.server.notice.domain;

import com.USWCicrcleLink.server.admin.domain.Admin;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private Long noticeId;

    @ManyToOne
    @JoinColumn(name = "admin_id", referencedColumnName = "adminId")
    private Admin admin;

    private String noticeTitle;

    private String noticeContent;

    private LocalDateTime noticeCreatedAt;

    private LocalDateTime noticeUpdatedAt;

}

package com.USWCicrcleLink.server.admin.notice.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "NOTICE_PHOTO_TABLE")
public class NoticePhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_photo_id")
    private Long noticePhotoId;

    @Column(name = "notice_photo_path", nullable = false)
    private String noticePhotoPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;
}

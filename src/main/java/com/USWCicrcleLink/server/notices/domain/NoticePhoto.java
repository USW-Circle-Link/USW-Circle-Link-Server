package com.USWCicrcleLink.server.notices.domain;

import jakarta.persistence.*;
import com.USWCicrcleLink.server.notices.domain.Notice;
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

    @Column(name = "notice_photo_name")
    private String noticePhotoName;

    @Column(name = "notice_photo_s3key")
    private String noticePhotoS3Key;

    @Column(name = "photo_order", nullable = false)
    private int order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;
}


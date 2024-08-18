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

    @Column(name = "notice_photo_name")
    private String noticePhotoName;

    @Column(name = "notice_photo_s3key")
    private String noticePhotoS3Key;

    @Column(name = "photo_order")
    private int order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;

    public void updateNoticePhoto(String noticePhotoName, String noticePhotoS3Key, int order) {
        this.noticePhotoName = noticePhotoName;
        this.noticePhotoS3Key = noticePhotoS3Key;
        this.order = order;
    }
}

package com.USWCicrcleLink.server.club.clubInfo.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "CLUB_INTRO_PHOTO_TABLE")
public class ClubInfoPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_info_photo_id")
    private Long clubInfoPhotoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_info_id", nullable = false)
    private ClubInfo clubInfo;

    @Column(name = "club_info_photo_name")
    private String clubInfoPhotoName;

    @Column(name = "club_info_photo_s3key")
    private String clubInfoPhotoS3Key;

    @Column(name = "photo_order", nullable = false)
    private int order;

    public void updateClubInfoPhoto(String clubInfoPhotoName, String clubInfoPhotoS3Key, int order) {
        this.clubInfoPhotoName = clubInfoPhotoName;
        this.clubInfoPhotoS3Key = clubInfoPhotoS3Key;
        this.order = order;
    }
}

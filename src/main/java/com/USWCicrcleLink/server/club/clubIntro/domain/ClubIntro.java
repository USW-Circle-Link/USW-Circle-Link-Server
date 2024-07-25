package com.USWCicrcleLink.server.club.clubIntro.domain;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.RecruitmentStatus;
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
@Table(name = "CLUB_INTRO_TABLE")
public class ClubIntro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_intro_id")
    private Long clubIntroId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @Column(name = "club_intro")
    private String clubIntro;

    @Column(name = "club_intro_photo_path")
    private String clubIntroPhotoPath;

    @Column(name = "additional_photo_path1")
    private String additionalPhotoPath1;

    @Column(name = "additional_photo_path2")
    private String additionalPhotoPath2;

    @Column(name = "additional_photo_path3")
    private String additionalPhotoPath3;

    @Column(name = "additional_photo_path4")
    private String additionalPhotoPath4;

    @Column(name = "googleForm_url")
    private String googleFormUrl;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecruitmentStatus recruitmentStatus = RecruitmentStatus.CLOSE;

    public void updateClubIntro(Club club, String clubIntro, String googleFormUrl,
                                String clubIntroPhotoPath, String additionalPhotoPath1,
                                String additionalPhotoPath2, String additionalPhotoPath3,
                                String additionalPhotoPath4) {
        this.club = club;
        this.clubIntro = clubIntro;
        this.googleFormUrl = googleFormUrl;
        this.clubIntroPhotoPath = clubIntroPhotoPath;
        this.additionalPhotoPath1 = additionalPhotoPath1;
        this.additionalPhotoPath2 = additionalPhotoPath2;
        this.additionalPhotoPath3 = additionalPhotoPath3;
        this.additionalPhotoPath4 = additionalPhotoPath4;
    }
}
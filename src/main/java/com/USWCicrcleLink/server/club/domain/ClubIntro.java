package com.USWCicrcleLink.server.club.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "CLUB_INTRO_TABLE")
public class ClubIntro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_intro_id")
    private Long clubIntroId;

    @OneToOne
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @Column(name = "club_intro")
    private String clubIntro;

    @Column(name = "club_intro_photo")
    private String introPhotoPath;

    @Column(name = "additional_photo_path1")
    private String additionalPhotoPath1;

    @Column(name = "additional_photo_path2")
    private String additionalPhotoPath2;

    @Transient
    private RecruitmentStatus recruitmentStatus;

    @Column(name = "googleForm_url")
    private String googleFormUrl;
}
package com.USWCicrcleLink.server.club.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "CLUB_TABLE")
public class Club {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_id")
    private Long clubId;

    @Column(name = "club_name")
    private String clubName;

    @Column(name = "main_photo_path")
    private String mainPhotoPath;

    @Column(name = "chat_room_url")
    private String chatRoomUrl;

    @Column(name = "leader_name")
    private String leaderName;

    @Column(name = "katalk_id")
    private String katalkID;

    @Column(name = "club_insta")
    private String clubInsta;

    @Column(name = "department", nullable = false)
    @Enumerated(EnumType.STRING)
    private Department department;

    @Column(name = "recruitment_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private RecruitmentStatus recruitmentStatus;

    public void updateClubInfo(String mainPhotoPath, String chatRoomURL, String katalkID, String clubInsta) {
        this.mainPhotoPath = mainPhotoPath;
        this.chatRoomUrl = chatRoomURL;
        this.katalkID = katalkID;
        this.clubInsta = clubInsta;
    }
}
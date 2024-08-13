package com.USWCicrcleLink.server.club.club.domain;

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

    @Column(name = "leader_name")
    private String leaderName;

    @Column(name = "leader_hp")
    private String leaderHp;

    @Column(name = "club_insta")
    private String clubInsta;

    @Column(name = "department", nullable = false)
    @Enumerated(EnumType.STRING)
    private Department department;

//    @Column(name = "chat_room_url")
//    private String chatRoomUrl;

    public void updateClubInfo(String mainPhotoPath, String leaderName,
                               String leaderHp, String clubInsta) {
        this.mainPhotoPath = mainPhotoPath;
        this.leaderName = leaderName;
        this.leaderHp = leaderHp;
        this.clubInsta = clubInsta;
    }
}
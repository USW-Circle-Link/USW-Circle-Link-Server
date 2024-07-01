package com.USWCicrcleLink.server.club.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
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

    @Column(name = "department")
    private Department department;

    @Column(name = "president_name")
    private String presidentName;

    @Column(name = "main_photo_path")
    private String mainPhotoPath;

    @Column(name = "chat_room_url")
    private String chatRoomUrl;

    @Column(name = "katalik_id")
    private String katalikId;

    @Column(name = "insta_url")
    private String instaUrl;

    @OneToOne(mappedBy = "club", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ClubIntro clubIntro;
}

package com.USWCicrcleLink.server.clubLeaders.domain;

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
@Table(name = "CLUB_TABLE")
public class Club {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clubId;

    private String clubName;

    private String leaderName;

    private int totalMembers;

    @Enumerated(EnumType.STRING)
    private Department department;

    private String description;

    @Enumerated(EnumType.STRING)
    private RecruitmentStatus recruitmentStatus;

    private String chatRoomURL;

    // + 동아리 소개 사진
}

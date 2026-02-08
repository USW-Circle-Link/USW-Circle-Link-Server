package com.USWCicrcleLink.server.club.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ClubListResponse {
    private UUID clubUUID;
    private String clubName;
    private String mainPhotoUrl;
    private String department;
    private List<String> hashtags;
    private String leaderName;
    private String leaderHp;
    private Long memberCount;
    private String recruitmentStatus;
}

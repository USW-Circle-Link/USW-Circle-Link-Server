package com.USWCicrcleLink.server.admin.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClubDetailResponse {
    private Long clubId;
    private String clubName;
    private String leaderName;
    private String phone;
    private String instagram;
    private String mainPhotoPath;
    private String introPhotoPath;
    private String chatRoomUrl;
    private String introContent;
}
package com.USWCicrcleLink.server.admin.dto;

import com.USWCicrcleLink.server.club.domain.RecruitmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AdminClubInfoResponse {
    private UUID clubUUID;
    private String mainPhoto;
    private List<String> infoPhotos;
    private String clubName;
    private String leaderName;
    private String leaderHp;
    private String clubInsta;
    private String clubInfo;
    private RecruitmentStatus recruitmentStatus;
    private String googleFormUrl;
    private List<String> clubHashtags;
    private List<String> clubCategoryNames;
    private String clubRoomNumber;
    private String clubRecruitment;
}

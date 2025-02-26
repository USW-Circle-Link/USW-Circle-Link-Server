package com.USWCicrcleLink.server.admin.admin.dto;

import com.USWCicrcleLink.server.club.club.domain.RecruitmentStatus;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class AdminClubSummaryResponse {

    // club
    private UUID clubUUID;
    private String clubName;
    private String leaderName;
    private String leaderHp;
    private String clubInsta;
    private String clubRoomNumber;

    // clubHashtag
    private List<String> clubHashtag;

    // clubCategory
    private List<String> clubCategories;

    // clubIntro
    private String clubIntro;
    private String clubRecruitment;
    private RecruitmentStatus recruitmentStatus;
    private String googleFormUrl;

    // photo
    private String mainPhoto;
    private List<String> introPhotos;
}

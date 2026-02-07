package com.USWCicrcleLink.server.club.leader.dto.club;

import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.club.clubInfo.domain.ClubInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaderClubInfoResponse {

    private UUID clubuuid;

    private String clubInfo;

    private String clubRecruitment;

    private RecruitmentStatus recruitmentStatus;

    private String googleFormUrl;

    private List<String> infoPhotos;

    public LeaderClubInfoResponse(Club club, ClubInfo clubInfo, List<String> infoPhotoUrls) {
        this.clubuuid = club.getClubuuid();
        this.clubInfo = clubInfo.getClubInfo();
        this.clubRecruitment = clubInfo.getClubRecruitment();
        this.recruitmentStatus = clubInfo.getRecruitmentStatus();
        this.googleFormUrl = clubInfo.getGoogleFormUrl();
        this.infoPhotos = infoPhotoUrls;
    }
}

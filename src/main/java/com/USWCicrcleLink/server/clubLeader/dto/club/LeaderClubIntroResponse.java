package com.USWCicrcleLink.server.clubLeader.dto.club;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaderClubIntroResponse {

    private long clubId;

    private String clubIntro;

    private String clubRecruitment;

    private RecruitmentStatus recruitmentStatus;

    private String googleFormUrl;

    private List<String> introPhotos;

    public LeaderClubIntroResponse(Club club, ClubIntro clubIntro, List<String> introPhotoUrls) {
        this.clubId = club.getClubId();
        this.clubIntro = clubIntro.getClubIntro();
        this.clubRecruitment = clubIntro.getClubRecruitment();
        this.recruitmentStatus = clubIntro.getRecruitmentStatus();
        this.googleFormUrl = clubIntro.getGoogleFormUrl();
        this.introPhotos = introPhotoUrls;
    }
}

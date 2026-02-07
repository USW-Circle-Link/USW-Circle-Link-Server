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
public class ClubSummaryResponse {

    // club
    private UUID clubuuid;
    private String clubName;
    private String leaderName;
    private String leaderHp;
    private String clubInsta;
    private String clubRoomNumber;

    // clubHashtag
    private List<String> clubHashtag;

    // club
    private List<String> clubCategories;

    // clubInfo (was ClubIntro)
    private String clubInfo;
    private String clubRecruitment;
    private RecruitmentStatus recruitmentStatus;
    private String googleFormUrl;

    // photo
    private String mainPhoto;
    private List<String> infoPhotos; // Renamed from introPhotos

    public ClubSummaryResponse(Club club, List<String> clubHashtag, List<String> clubCategories,
            ClubInfo clubInfo, String mainPhotoUrl, List<String> infoPhotoUrls) {
        // club
        this.clubuuid = club.getClubuuid();
        this.clubName = club.getClubName();
        this.leaderName = club.getLeaderName();
        this.leaderHp = club.getLeaderHp();
        this.clubInsta = club.getClubInsta();
        this.clubRoomNumber = club.getClubRoomNumber();
        // clubHashtag
        this.clubHashtag = clubHashtag;
        // clubCategories
        this.clubCategories = clubCategories;
        // clubInfo
        this.clubInfo = clubInfo.getClubInfo();
        this.clubRecruitment = clubInfo.getClubRecruitment();
        this.recruitmentStatus = clubInfo.getRecruitmentStatus();
        this.googleFormUrl = clubInfo.getGoogleFormUrl();
        // photo
        this.mainPhoto = mainPhotoUrl;
        this.infoPhotos = infoPhotoUrls;
    }
}

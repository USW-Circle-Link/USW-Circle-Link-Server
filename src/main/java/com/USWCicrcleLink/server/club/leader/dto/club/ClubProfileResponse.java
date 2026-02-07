package com.USWCicrcleLink.server.club.leader.dto.club;

import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.club.domain.Department;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClubProfileResponse {

    private String mainPhotoUrl;

    private String clubName;

    private String leaderName;

    private String leaderHp;

    private String clubInsta;

    private String clubRoomNumber;

    private List<String> clubHashtag;

    private List<String> clubCategoryName;

    private Department department;

    // Renamed constructor to match class name
    public ClubProfileResponse(String mainPhotoUrl, Club club, List<String> clubHashtag,
            List<String> clubCategoryName) {
        this.mainPhotoUrl = mainPhotoUrl;
        this.clubName = club.getClubName();
        this.leaderName = club.getLeaderName();
        this.leaderHp = club.getLeaderHp();
        this.clubInsta = club.getClubInsta();
        this.clubRoomNumber = club.getClubRoomNumber();
        this.clubHashtag = clubHashtag;
        this.clubCategoryName = clubCategoryName;
        this.department = club.getDepartment();
    }

}

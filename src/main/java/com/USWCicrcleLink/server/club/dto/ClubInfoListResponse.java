package com.USWCicrcleLink.server.club.dto;

import com.USWCicrcleLink.server.club.domain.Club;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ClubInfoListResponse {
    private UUID clubuuid;
    private String clubName;
    private String department;
    private String leaderName;
    private String leaderHp;
    private String clubInsta;
    private String clubRoomNumber;
    private String mainPhotoUrl;

    public ClubInfoListResponse(Club club, String mainPhotoUrl) {
        this.clubuuid = club.getClubuuid();
        this.clubName = club.getClubName();
        this.department = club.getDepartment() != null ? club.getDepartment().name() : null;
        this.leaderName = club.getLeaderName();
        this.leaderHp = club.getLeaderHp();
        this.clubInsta = club.getClubInsta();
        this.clubRoomNumber = club.getClubRoomNumber();
        this.mainPhotoUrl = mainPhotoUrl;
    }
}

package com.USWCicrcleLink.server.admin.admin.dto;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.Department;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClubCreationResponse {
    private Long clubId;
    private String leaderAccount;
    private String clubName;
    private Department department;

    public ClubCreationResponse(Club club) {
        this.clubId = club.getClubId();
        this.leaderAccount = club.getLeaderName();
        this.clubName = club.getClubName();
        this.department = club.getDepartment();
    }
}

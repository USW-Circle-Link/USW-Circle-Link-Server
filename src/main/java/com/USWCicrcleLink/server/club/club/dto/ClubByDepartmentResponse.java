package com.USWCicrcleLink.server.club.club.dto;

import com.USWCicrcleLink.server.club.club.domain.Club;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClubByDepartmentResponse {
    private Long clubId;
    private String clubName;
    private String mainPhotoPath;

    public ClubByDepartmentResponse(Club club) {
        this.clubId = club.getClubId();
        this.clubName = club.getClubName();
        this.mainPhotoPath = club.getMainPhotoPath();
    }
}


package com.USWCicrcleLink.server.club.club.dto;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.ClubMainPhoto;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClubByDepartmentResponse {
    private Long clubId;
    private String clubName;
    private String mainPhoto;
    private String departmentName;

    // Club 엔티티를 인수로 받는 생성자
    public ClubByDepartmentResponse(Club club, String mainPhotoUrl) {
        this.clubName = club.getClubName();
        this.mainPhoto = mainPhotoUrl;
        this.departmentName = club.getDepartment().name();
        this.clubId = club.getClubId();
    }
}


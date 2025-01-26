package com.USWCicrcleLink.server.user.dto;

import com.USWCicrcleLink.server.club.club.domain.Club;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClubInfoListResponse {
    private Long clubId;
    private String clubName;
    private String mainPhoto;

    // Club 엔티티를 인수로 받는 생성자
    public ClubInfoListResponse(Club club, String mainPhoto) {
        this.clubName = club.getClubName();
        this.mainPhoto = mainPhoto;
        this.clubId = club.getClubId();
    }
}


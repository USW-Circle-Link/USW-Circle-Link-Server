package com.USWCicrcleLink.server.clubLeader.dto;

import com.USWCicrcleLink.server.club.club.domain.Department;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClubInfoResponse {

    private String mainPhotoUrl;

    private String clubName;

    private String leaderName;

    private String leaderHp;

    private String clubInsta;

    private Department department;

}

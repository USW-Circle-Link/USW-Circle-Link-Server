package com.USWCicrcleLink.server.clubLeader.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClubInfoResponse {

    private String mainPhoto;

    private String clubName;

    private String leaderName;

    private String leaderHp;

    private String katalkID;

    private String clubInsta;

}

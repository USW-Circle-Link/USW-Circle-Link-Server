package com.USWCicrcleLink.server.user.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyClubResponse {

    private UUID clubUUID;

    private String mainPhotoPath;

    private String clubName;

    private String leaderName;

    private String leaderHp;

    private String clubInsta;

    private String clubRoomNumber;

}

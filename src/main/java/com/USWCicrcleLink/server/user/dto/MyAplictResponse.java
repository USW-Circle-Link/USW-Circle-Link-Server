package com.USWCicrcleLink.server.user.dto;

import com.USWCicrcleLink.server.club.application.domain.AplictStatus;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyAplictResponse {

    private UUID clubUUID;

    private String mainPhotoPath;

    private String clubName;

    private String leaderName;

    private String leaderHp;

    private String clubInsta;

    private String ClubRoomNumber;

    private AplictStatus publicStatus;

    private UUID aplictUUID;

}

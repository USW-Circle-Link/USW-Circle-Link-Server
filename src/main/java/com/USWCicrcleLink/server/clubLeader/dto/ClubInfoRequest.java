package com.USWCicrcleLink.server.clubLeader.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ClubInfoRequest {

    //2MB
    private MultipartFile mainPhoto;

    private String leaderName;

    private String leaderHp;

    private String katalkID;

    private String clubInsta;

    //    private String chatRoomURL;
}

package com.USWCicrcleLink.server.user.dto;

import com.USWCicrcleLink.server.aplict.domain.AplictStatus;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
public class MyAplictResponse {

    private Long clubId;

    private String mainPhotoPath;

    private String clubName;

    private String leaderName;

    private String katalkID;

    private String clubInsta;

    private AplictStatus aplictStatus;

}

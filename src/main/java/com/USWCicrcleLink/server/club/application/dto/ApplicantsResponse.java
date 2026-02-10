package com.USWCicrcleLink.server.club.application.dto;

import com.USWCicrcleLink.server.club.application.domain.AplictStatus;
import com.USWCicrcleLink.server.user.profile.domain.Profile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicantsResponse {

    private UUID aplictUUID;

    private String userName;

    private String major;

    private String studentNumber;

    private String userHp;

    private AplictStatus privateStatus;

    public ApplicantsResponse(UUID aplictUUID, Profile profile, AplictStatus privateStatus) {
        this.aplictUUID = aplictUUID;
        this.userName = profile.getUserName();
        this.major = profile.getMajor();
        this.studentNumber = profile.getStudentNumber();
        this.userHp = profile.getUserHp();
        this.privateStatus = privateStatus;
    }
}

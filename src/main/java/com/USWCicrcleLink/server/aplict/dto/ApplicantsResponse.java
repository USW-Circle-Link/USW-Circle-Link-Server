package com.USWCicrcleLink.server.aplict.dto;

import com.USWCicrcleLink.server.profile.profile.domain.Profile;
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

    public ApplicantsResponse(UUID aplictUUID, Profile profile) {
        this.aplictUUID = aplictUUID;
        this.userName = profile.getUserName();
        this.major = profile.getMajor();
        this.studentNumber = profile.getStudentNumber();
        this.userHp = profile.getUserHp();
    }
}

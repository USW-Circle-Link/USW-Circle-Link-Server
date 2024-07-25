package com.USWCicrcleLink.server.profile.dto;

import com.USWCicrcleLink.server.profile.domain.Profile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private String userName;

    private String studentNumber;

    private String userHp;

    private String major;

    public ProfileResponse(Profile profile){
        this.userName=profile.getUserName();
        this.studentNumber=profile.getStudentNumber();
        this.userHp=profile.getUserHp();
        this.major=profile.getMajor();
    }
}

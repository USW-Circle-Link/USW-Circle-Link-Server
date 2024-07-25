package com.USWCicrcleLink.server.clubLeader.dto;

import com.USWCicrcleLink.server.profile.domain.Profile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClubMembersResponse {

    private Long clubMemberId;

    private String userName;

    private String major;

    private String studentNumber;

    private String userHp;

    public ClubMembersResponse(Long clubMemberId, Profile profile) {
        this.clubMemberId = clubMemberId;
        this.userName = profile.getUserName();
        this.major = profile.getMajor();
        this.studentNumber = profile.getStudentNumber();
        this.userHp = profile.getUserHp();
    }
}

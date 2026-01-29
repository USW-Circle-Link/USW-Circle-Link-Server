package com.USWCicrcleLink.server.club.leader.dto.clubMembers;

import com.USWCicrcleLink.server.user.domain.ExistingMember.ClubMemberTemp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequestResponse {

    private UUID clubMemberAccountStatusUUID;

    private String profileTempName;

    private String profileTempStudentNumber;

    private String profileTempMajor;

    private String profileTempHp;

    public SignUpRequestResponse(UUID clubMemberAccountStatusUUID, ClubMemberTemp clubMemberTemp) {
        this.clubMemberAccountStatusUUID = clubMemberAccountStatusUUID;
        this.profileTempName = clubMemberTemp.getProfileTempName();
        this.profileTempStudentNumber = clubMemberTemp.getProfileTempStudentNumber();
        this.profileTempMajor = clubMemberTemp.getProfileTempMajor();
        this.profileTempHp = clubMemberTemp.getProfileTempHp();
    }
}


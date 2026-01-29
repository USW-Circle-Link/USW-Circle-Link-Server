package com.USWCicrcleLink.server.club.leader.dto.clubMembers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClubMembersAcceptSignUpRequest {
    @NotNull
    @Valid
    ClubMemberProfileRequest signUpProfileRequest;

    @NotNull
    @Valid
    ClubMemberProfileRequest clubNonMemberProfileRequest;
}


package com.USWCicrcleLink.server.club.leader.dto.clubMembers;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ClubMembersDeleteRequest {

    @NotNull(message = "삭제할 동아리 회원을 선택해주세요.")
    private UUID clubMemberUUID;
}


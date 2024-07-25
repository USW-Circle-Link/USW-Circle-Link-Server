package com.USWCicrcleLink.server.club.club.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ClubMembersLeaderCount {
    private Long clubId;
    private long memberCount;
    private long leaderCount;
}

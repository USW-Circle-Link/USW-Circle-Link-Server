package com.USWCicrcleLink.server.club.club.repository;

import com.USWCicrcleLink.server.club.club.dto.ClubMembersLeaderCount;

import java.util.List;

public interface ClubRepositoryCustom {
    List<ClubMembersLeaderCount> findAllWithMemberAndLeaderCount();
}

package com.USWCicrcleLink.server.club.club.repository;

import com.USWCicrcleLink.server.club.club.domain.ClubMembers;

import java.util.List;

public interface ClubMembersRepositoryCustom {
    List<ClubMembers> findAllWithProfile(Long clubId);

    void deleteByClubClubId(Long clubId);
}

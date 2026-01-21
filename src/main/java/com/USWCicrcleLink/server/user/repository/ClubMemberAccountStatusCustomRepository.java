package com.USWCicrcleLink.server.user.repository;

import com.USWCicrcleLink.server.user.domain.ExistingMember.ClubMemberAccountStatus;

import java.util.List;

public interface ClubMemberAccountStatusCustomRepository {
    List<ClubMemberAccountStatus> findAllWithClubMemberTemp(Long clubId);
}
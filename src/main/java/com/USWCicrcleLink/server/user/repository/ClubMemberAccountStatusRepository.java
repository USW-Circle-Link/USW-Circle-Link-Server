package com.USWCicrcleLink.server.user.repository;

import com.USWCicrcleLink.server.user.domain.ExistingMember.ClubMemberAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClubMemberAccountStatusRepository
        extends JpaRepository<ClubMemberAccountStatus, Long>, ClubMemberAccountStatusCustomRepository {
    Long countByClubMemberTemp_ClubMemberTempId(Long clubMemberTempId);

    List<ClubMemberAccountStatus> findAllByClubMemberTemp_ClubMemberTempId(Long clubMemberTempId);

    Optional<ClubMemberAccountStatus> findByClubMemberAccountStatusUUIDAndClub_ClubId(UUID clubMemberAccountStatusUUID,
            Long clubId);

    Optional<ClubMemberAccountStatus> findByClubMemberAccountStatusUUIDAndClub_Clubuuid(
            UUID clubMemberAccountStatusUUID, UUID clubuuid);

}
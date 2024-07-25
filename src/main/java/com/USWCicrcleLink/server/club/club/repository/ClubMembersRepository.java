package com.USWCicrcleLink.server.club.club.repository;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.ClubMembers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ClubMembersRepository extends JpaRepository<ClubMembers,Long>, ClubMembersRepositoryCustom {

    //동아리원 조회 성능 비교
    List<ClubMembers> findByClub(Club club);
    List<ClubMembers> findByProfileProfileId(Long profileId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ClubMembers cm WHERE cm.club.clubId = :clubId")
    void deleteByClubClubId(@Param("clubId")Long clubId);

}

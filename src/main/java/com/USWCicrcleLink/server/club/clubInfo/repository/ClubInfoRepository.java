package com.USWCicrcleLink.server.club.clubInfo.repository;

import com.USWCicrcleLink.server.club.clubInfo.domain.ClubInfo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClubInfoRepository extends JpaRepository<ClubInfo, Long> {
    @Query("SELECT ci FROM ClubInfo ci WHERE ci.club.clubId = :clubId")
    Optional<ClubInfo> findByClubClubId(@Param("clubId") Long clubId);

    @Query("SELECT ci.club.clubId FROM ClubInfo ci WHERE ci.recruitmentStatus = com.USWCicrcleLink.server.club.domain.RecruitmentStatus.OPEN")
    List<Long> findOpenClubIds();

    @Query("SELECT ci FROM ClubInfo ci WHERE ci.club.clubuuid = :clubuuid")
    Optional<ClubInfo> findByClubuuid(@Param("clubuuid") UUID clubuuid);

    @Query("SELECT ci.club.clubId, ci.recruitmentStatus FROM ClubInfo ci WHERE ci.club.clubId IN :clubIds")
    List<Object[]> findRecruitmentStatusByClubIds(@Param("clubIds") List<Long> clubIds);
}

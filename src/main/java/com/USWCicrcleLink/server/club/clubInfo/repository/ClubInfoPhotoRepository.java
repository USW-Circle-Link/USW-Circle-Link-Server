package com.USWCicrcleLink.server.club.clubInfo.repository;

import com.USWCicrcleLink.server.club.clubInfo.domain.ClubInfo;
import com.USWCicrcleLink.server.club.clubInfo.domain.ClubInfoPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubInfoPhotoRepository extends JpaRepository<ClubInfoPhoto, Long> {
    Optional<ClubInfoPhoto> findByClubInfo_ClubInfoIdAndOrder(Long clubInfoId, int order);

    List<ClubInfoPhoto> findByClubInfo(ClubInfo clubInfo);

    @Query("SELECT cip FROM ClubInfoPhoto cip WHERE cip.clubInfo.club.clubId = :clubId ORDER BY cip.order")
    List<ClubInfoPhoto> findByClubInfoClubId(@Param("clubId") Long clubId);
}

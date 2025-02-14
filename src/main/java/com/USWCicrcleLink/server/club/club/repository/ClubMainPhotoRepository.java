package com.USWCicrcleLink.server.club.club.repository;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.ClubMainPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClubMainPhotoRepository extends JpaRepository<ClubMainPhoto, Long> {
    ClubMainPhoto findByClub_ClubId(Long clubId);

    Optional<ClubMainPhoto> findByClub(Club club);

    @Query("SELECT cmp.clubMainPhotoS3Key FROM ClubMainPhoto cmp WHERE cmp.club.clubId = :clubId")
    List<String> findS3KeysByClubId(@Param("clubId") Long clubId);
}

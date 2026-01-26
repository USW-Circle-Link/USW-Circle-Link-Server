package com.USWCicrcleLink.server.club.form.repository;

import com.USWCicrcleLink.server.club.form.domain.ClubForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ClubFormRepository extends JpaRepository<ClubForm, Long> {

    @Query("SELECT f FROM ClubForm f " +
            "WHERE f.clubId = (SELECT c.clubId FROM Club c WHERE c.clubUUID = :clubUUID) " +
            "AND f.status = 'PUBLISHED'")
    Optional<ClubForm> findActiveFormByClubUUID(@Param("clubUUID") UUID clubUUID);
}
package com.USWCicrcleLink.server.club.form.repository;

import com.USWCicrcleLink.server.club.leader.domain.ClubForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ClubFormRepository extends JpaRepository<ClubForm, Long> {

    @Query("SELECT f FROM ClubForm f " +
            "JOIN f.club c " +
            "WHERE c.clubuuid = :clubuuid " +
            "AND f.status = 'PUBLISHED'")
    Optional<ClubForm> findActiveFormByClubUUID(@Param("clubuuid") UUID clubuuid);
}

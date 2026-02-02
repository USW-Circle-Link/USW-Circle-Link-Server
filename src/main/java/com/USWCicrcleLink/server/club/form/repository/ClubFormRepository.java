package com.USWCicrcleLink.server.club.form.repository;

import com.USWCicrcleLink.server.club.leader.domain.ClubForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClubFormRepository extends JpaRepository<ClubForm, Long> {

    @Query("SELECT f FROM ClubForm f " +
            "JOIN f.club c " +
            "WHERE c.clubuuid = :clubuuid " +
            "ORDER BY f.updatedAt DESC")
    List<ClubForm> findFormsByClubUUID(@Param("clubuuid") UUID clubuuid);
}

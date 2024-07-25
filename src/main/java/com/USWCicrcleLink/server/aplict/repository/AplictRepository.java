package com.USWCicrcleLink.server.aplict.repository;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AplictRepository extends JpaRepository<Aplict, Long>, AplictRepositoryCustom {
    List<Aplict> findByProfileProfileId(Long profileId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Aplict a WHERE a.club.clubId = :clubId")
    void deleteByClubClubId(@Param("clubId") Long clubId);
}

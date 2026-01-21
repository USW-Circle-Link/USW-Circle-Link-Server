package com.USWCicrcleLink.server.clubLeader.repository;

import com.USWCicrcleLink.server.clubLeader.domain.Leader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface LeaderRepository extends JpaRepository<Leader,Long> {
    Optional<Leader> findByLeaderUUID(UUID leaderUUID);

    Optional<Leader> findByLeaderAccount(String account);

    boolean existsByLeaderAccount(String account);

    @Query("SELECT l.club.clubUUID FROM Leader l WHERE l.leaderUUID = :leaderUUID")
    Optional<UUID> findClubUUIDByLeaderUUID(@Param("leaderUUID") UUID leaderUUID);

    @Query("SELECT l FROM Leader l WHERE l.club.clubUUID = :clubUUID")
    Optional<Leader> findByClubUUID(@Param("clubUUID") UUID clubUUID);
}

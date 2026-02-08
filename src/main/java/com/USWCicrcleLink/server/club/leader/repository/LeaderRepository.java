package com.USWCicrcleLink.server.club.leader.repository;

import com.USWCicrcleLink.server.club.leader.domain.Leader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaderRepository extends JpaRepository<Leader, Long> {
    Optional<Leader> findByLeaderUUID(UUID leaderUUID);

    Optional<Leader> findByLeaderAccount(String account);

    boolean existsByLeaderAccount(String account);

    @Query("SELECT l.club.clubuuid FROM Leader l WHERE l.leaderUUID = :leaderUUID")
    Optional<UUID> findClubuuidByLeaderUUID(@Param("leaderUUID") UUID leaderUUID);

    @Query("SELECT l FROM Leader l WHERE l.club.clubuuid = :clubuuid")
    Optional<Leader> findByClubuuid(@Param("clubuuid") java.util.UUID clubuuid);

    java.util.List<Leader> findAllByClubClubIdIn(java.util.List<Long> clubIds);
}

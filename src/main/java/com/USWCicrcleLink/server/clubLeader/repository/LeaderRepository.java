package com.USWCicrcleLink.server.clubLeader.repository;

import com.USWCicrcleLink.server.clubLeader.domain.Leader;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface LeaderRepository extends JpaRepository<Leader,Long> {
    Optional<Leader> findByLeaderUUID(UUID leaderUUID);

    Optional<Leader> findByLeaderAccount(String account);

    // 추가: Leader 계정 중복 확인 메서드
    boolean existsByLeaderAccount(String account);
}

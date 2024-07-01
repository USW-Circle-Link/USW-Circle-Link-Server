package com.USWCicrcleLink.server.aplict.repository;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.club.domain.Club;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AplictRepository extends JpaRepository<Aplict, Long> {
    List<Aplict> findByClub(Club club);
}

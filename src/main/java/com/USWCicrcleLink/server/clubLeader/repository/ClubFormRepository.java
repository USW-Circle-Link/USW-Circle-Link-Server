package com.USWCicrcleLink.server.clubLeader.repository;

import com.USWCicrcleLink.server.clubLeader.domain.ClubForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubFormRepository extends JpaRepository<ClubForm, Long> {

}
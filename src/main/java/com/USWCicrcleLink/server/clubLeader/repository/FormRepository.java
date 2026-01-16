package com.USWCicrcleLink.server.clubLeader.repository;

import com.USWCicrcleLink.server.clubLeader.domain.ClubForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormRepository extends JpaRepository<ClubForm, Long> {
    // 기본적인 CRUD(저장, 조회, 삭제) 기능이 자동으로 제공됩니다.
}
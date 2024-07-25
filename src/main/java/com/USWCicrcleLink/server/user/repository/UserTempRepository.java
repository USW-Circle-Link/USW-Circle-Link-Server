package com.USWCicrcleLink.server.user.repository;

import com.USWCicrcleLink.server.user.domain.UserTemp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface UserTempRepository extends JpaRepository<UserTemp, Long> {

    // 이메일로 임시 회원 조회
    Optional<UserTemp> findByTempEmail(String email);

    Boolean existsByTempEmail(String email);
}


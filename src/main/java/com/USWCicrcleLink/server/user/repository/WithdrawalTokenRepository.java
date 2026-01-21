package com.USWCicrcleLink.server.user.repository;

import com.USWCicrcleLink.server.user.domain.WithdrawalToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WithdrawalTokenRepository extends JpaRepository<WithdrawalToken,Long> {

    // 회원 탈퇴시 전송 되는 인증 코드 저장
    Optional<WithdrawalToken> findByUserUserUUID(UUID uuid);

}

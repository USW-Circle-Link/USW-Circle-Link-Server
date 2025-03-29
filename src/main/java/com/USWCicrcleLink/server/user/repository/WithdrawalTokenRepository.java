package com.USWCicrcleLink.server.user.repository;

import com.USWCicrcleLink.server.user.domain.WithdrawalToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WithdrawalTokenRepository extends CrudRepository<WithdrawalToken,UUID> {
    Optional<WithdrawalToken>findByUserUUID(UUID uuid);

}

package com.USWCicrcleLink.server.user.repository;

import com.USWCicrcleLink.server.user.domain.AuthToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthTokenRepository extends CrudRepository<AuthToken,UUID> {
    Optional<AuthToken> findByUserUUID(UUID uuid);
}

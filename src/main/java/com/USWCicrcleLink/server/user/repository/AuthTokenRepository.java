package com.USWCicrcleLink.server.user.repository;

import com.USWCicrcleLink.server.user.domain.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken,Long> {
    Optional<AuthToken> findByUserUserUUID(UUID uuid);
}

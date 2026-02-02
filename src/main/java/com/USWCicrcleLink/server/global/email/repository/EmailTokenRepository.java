package com.USWCicrcleLink.server.global.email.repository;

import com.USWCicrcleLink.server.global.email.domain.EmailToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailTokenRepository extends JpaRepository<EmailToken, Long> {

    Optional<EmailToken> findByEmailTokenUUID (UUID emailTokenUUID);
    List<EmailToken> findAllByExpirationTimeBefore(LocalDateTime time);

    Optional<EmailToken> findByEmail(String email);
}


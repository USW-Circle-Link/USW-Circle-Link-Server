package com.USWCicrcleLink.server.user.repository;

import com.USWCicrcleLink.server.user.domain.EventVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EventVerificationRepository extends JpaRepository<EventVerification, Long> {
    boolean existsByUserUUID(UUID userUUID);

    Optional<EventVerification> findByUserUUID(UUID userUUID);
}

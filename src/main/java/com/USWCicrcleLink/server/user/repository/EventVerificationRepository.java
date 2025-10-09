package com.USWCicrcleLink.server.user.repository;

import com.USWCicrcleLink.server.user.domain.EventVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventVerificationRepository extends JpaRepository<EventVerification, Long> {
    boolean existsByUserUUIDAndClubUUID(UUID userUUID, UUID clubUUID);
}

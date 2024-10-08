package com.USWCicrcleLink.server.profile.repository;

import com.USWCicrcleLink.server.profile.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUser_UserUUID(UUID userUUID);
    Optional<Profile> findByUserUserId(Long userId);
    List<Profile> findAllByFcmTokenCertificationTimestampBefore(LocalDateTime dateTime);
}

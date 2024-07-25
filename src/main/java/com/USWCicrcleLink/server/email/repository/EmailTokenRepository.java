package com.USWCicrcleLink.server.email.repository;

import com.USWCicrcleLink.server.email.domain.EmailToken;

import com.USWCicrcleLink.server.user.domain.UserTemp;
import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailTokenRepository extends JpaRepository<EmailToken, UUID> {

    @EntityGraph(attributePaths = "userTemp")
    Optional<EmailToken> findByEmailTokenId (UUID emailTokenId);
    EmailToken findByUserTemp(UserTemp userTemp);
    List<EmailToken> findAllByCertificationTimeBefore(LocalDateTime time);


}

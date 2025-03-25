package com.USWCicrcleLink.server.user.repository;

import com.USWCicrcleLink.server.user.domain.SignupToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface SignupTokenRepository extends CrudRepository<SignupToken, UUID> {

    Optional<SignupToken> findByEmail(String email);

    Optional<SignupToken> findByEmailTokenUUID(UUID emailTokenUUID);
}

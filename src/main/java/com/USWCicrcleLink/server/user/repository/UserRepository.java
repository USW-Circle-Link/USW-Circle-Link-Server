package com.USWCicrcleLink.server.user.repository;

import com.USWCicrcleLink.server.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository <User,Long> {
    User findByUserUUID(UUID uuid);
    Boolean existsByUserAccount(String account);

    Optional<User> findByUserAccount(String account);
    Boolean existsByEmail(String email);
}


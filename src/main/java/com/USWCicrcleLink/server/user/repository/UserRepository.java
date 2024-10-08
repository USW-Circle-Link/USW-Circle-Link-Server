package com.USWCicrcleLink.server.user.repository;

import com.USWCicrcleLink.server.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository <User,Long> {
    Optional<User> findByUserUUID(UUID uuid);
    Optional<User> findByUserAccount(String account);
    Optional <User> findByEmail(String email);
    Optional <User> findByUserAccountAndEmail(String account, String email);
}


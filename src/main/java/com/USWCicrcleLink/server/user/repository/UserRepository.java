package com.USWCicrcleLink.server.user.repository;

import com.USWCicrcleLink.server.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository <User,Long> {
    Optional<User> findByUserUUID(UUID uuid);
    Optional<User> findByUserAccount(String account);
    Optional <User> findByEmail(String email);
    Optional <User> findByUserAccountAndEmail(String account, String email);

    void deleteByUserUUID(UUID uuid);
}


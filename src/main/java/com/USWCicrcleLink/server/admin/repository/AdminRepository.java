package com.USWCicrcleLink.server.admin.repository;

import com.USWCicrcleLink.server.admin.domain.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByAdminAccount(String adminAccount);

    Optional<Admin> findByAdminUUID(UUID adminUUID);

    Admin findTop1ByOrderByAdminIdAsc();
}

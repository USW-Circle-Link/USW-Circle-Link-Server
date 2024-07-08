package com.USWCicrcleLink.server.admin.repository;

import com.USWCicrcleLink.server.admin.domain.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin,Long> {
}

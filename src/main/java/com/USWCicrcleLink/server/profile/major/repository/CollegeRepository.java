package com.USWCicrcleLink.server.profile.major.repository;

import com.USWCicrcleLink.server.profile.major.domain.College;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollegeRepository extends JpaRepository<College, Long> {
}

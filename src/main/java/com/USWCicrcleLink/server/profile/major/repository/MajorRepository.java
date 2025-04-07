package com.USWCicrcleLink.server.profile.major.repository;

import com.USWCicrcleLink.server.profile.major.domain.College;
import com.USWCicrcleLink.server.profile.major.domain.Major;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MajorRepository extends JpaRepository<Major, Long> {
    List<Major> findByCollege(College college);
}

package com.USWCicrcleLink.server.club.club.repository;

import com.USWCicrcleLink.server.club.club.domain.ClubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClubCategoryRepository extends JpaRepository<ClubCategory, Long> {
    List<ClubCategory> findByClubCategoryNameIn(List<String> categories);
    Optional<ClubCategory> findByClubCategoryName(String clubCategoryName);
}

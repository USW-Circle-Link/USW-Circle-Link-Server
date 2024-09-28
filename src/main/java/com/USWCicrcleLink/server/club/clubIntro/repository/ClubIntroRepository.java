package com.USWCicrcleLink.server.club.clubIntro.repository;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.Department;
import com.USWCicrcleLink.server.club.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClubIntroRepository extends JpaRepository<ClubIntro, Long> {
    Optional<ClubIntro> findByClubClubId(Long clubId);

    Optional<ClubIntro> findByClub(Club club);

    List<ClubIntro> findByRecruitmentStatus(RecruitmentStatus recruitmentStatus);
}

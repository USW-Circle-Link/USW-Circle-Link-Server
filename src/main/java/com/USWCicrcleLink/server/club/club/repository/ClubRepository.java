package com.USWCicrcleLink.server.club.club.repository;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.Department;
import com.USWCicrcleLink.server.club.club.domain.RecruitmentStatus;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClubRepository extends JpaRepository<Club, Long>, ClubRepositoryCustom{
    List<Club> findByDepartment(Department department);

    @NonNull
    Page<Club> findAll(@NonNull Pageable pageable);

    // 추가: 동아리 이름 중복 확인 메서드
    boolean existsByClubName(String clubName);
}

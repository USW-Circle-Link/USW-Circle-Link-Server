package com.USWCicrcleLink.server.user.repository;

import com.USWCicrcleLink.server.user.domain.ExistingMember.ClubMemberTemp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubMemberTempRepository extends JpaRepository<ClubMemberTemp,Long> {
}

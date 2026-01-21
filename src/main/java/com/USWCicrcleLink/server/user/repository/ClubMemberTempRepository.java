package com.USWCicrcleLink.server.user.repository;

import com.USWCicrcleLink.server.user.domain.ExistingMember.ClubMemberAccountStatus;
import com.USWCicrcleLink.server.user.domain.ExistingMember.ClubMemberTemp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClubMemberTempRepository extends JpaRepository<ClubMemberTemp,Long> {
    List<ClubMemberTemp> findAllByClubMemberTempExpiryDateBefore(LocalDateTime dateTime);
    Optional<ClubMemberTemp> findByProfileTempAccount(String account);
    Optional<ClubMemberTemp> findByProfileTempEmail(String email);
    Optional<ClubMemberTemp> findByProfileTempNameAndProfileTempStudentNumberAndAndProfileTempHp(String name,String studentNumber,String hp);

}

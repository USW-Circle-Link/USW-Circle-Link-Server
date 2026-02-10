package com.USWCicrcleLink.server.club.application.repository;

import com.USWCicrcleLink.server.club.application.domain.Aplict;
import com.USWCicrcleLink.server.club.application.domain.AplictStatus;
import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.user.profile.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AplictRepository extends JpaRepository<Aplict, Long>, AplictRepositoryCustom {
        List<Aplict> findByProfileProfileId(Long profileId); // Restored

        Optional<Aplict> findByClub_ClubIdAndAplictUUID(Long clubId, UUID aplictUUID); // Restored

        List<Aplict> findByClub_ClubIdAndPrivateStatus(Long clubId, AplictStatus status); // Restored

        Optional<Aplict> findByClub_ClubIdAndAplictUUIDAndPrivateStatus(Long clubId, UUID aplictUUID,
                        AplictStatus status); // Restored

        List<Aplict> findAllWithProfileByClubIdAndStatus(Long clubId, AplictStatus status);

        List<Aplict> findAllWithProfileByClubId(Long clubId);

        List<Aplict> findAllByDeleteDateBefore(LocalDateTime dateTime);

        void deleteAllByProfile(Profile profile);

        @Query("SELECT COUNT(a) > 0 FROM Aplict a WHERE a.profile = :profile AND a.club.clubuuid = :clubuuid")
        boolean existsByProfileAndClubuuid(@Param("profile") Profile profile,
                        @Param("clubuuid") UUID clubuuid);

        Optional<Aplict> findByProfileAndClub_Clubuuid(Profile profile, UUID clubuuid);

        @Query("SELECT a.club FROM Aplict a WHERE a.aplictId = :aplictId")
        Optional<Club> findClubByAplictId(@Param("aplictId") Long aplictId);

        Optional<Aplict> findByAplictUUID(UUID aplictUUID);
}

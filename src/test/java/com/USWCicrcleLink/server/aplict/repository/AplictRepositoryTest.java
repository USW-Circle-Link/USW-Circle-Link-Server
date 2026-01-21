//package com.USWCicrcleLink.server.aplict.repository;
//
//import com.USWCicrcleLink.server.aplict.domain.Aplict;
//import com.USWCicrcleLink.server.aplict.domain.AplictStatus;
//import com.USWCicrcleLink.server.profile.domain.Profile;
//import jakarta.persistence.EntityManager;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DataJpaTest
//public class AplictRepositoryTest {
//
//    @Autowired
//    private AplictRepository aplictRepository;
//
//    @Qualifier("aplictRepositoryCustomImpl")
//    @Autowired
//    private AplictRepositoryCustom aplictRepositoryCustom;
//
//    @Autowired
//    private EntityManager em;
//
//    private Profile profile;
//    private Aplict aplict;
//
//    @BeforeEach
//    void setUp() {
//        profile = Profile.builder()
//                .userName("Test User")
//                .build();
//        em.persist(profile);
//
//        aplict = Aplict.builder()
//                .profile(profile)
//                .checked(true)
//                .aplictStatus(AplictStatus.WAIT)
//                .deleteDate(LocalDateTime.now().minusDays(1))
//                .build();
//
//        em.persist(aplict);
//        em.flush();
//        em.clear();
//    }
//
//    @Test
//    @DisplayName("Profile ID로 Aplict 목록 조회 테스트")
//    void testFindByProfileProfileId() {
//        List<Aplict> aplictList = aplictRepository.findByProfileProfileId(profile.getProfileId());
//        assertThat(aplictList).isNotEmpty();
//        assertThat(aplictList.get(0).getProfile().getProfileId()).isEqualTo(profile.getProfileId());
//    }
//
//    @Test
//    @DisplayName("동아리 ID와 Checked 여부로 Aplict 조회 테스트")
//    void testFindByClubIdAndChecked() {
//        Long clubId = 1L; // 가정된 clubId
//        List<Aplict> aplictList = aplictRepository.findByClub_ClubIdAndChecked(clubId, true);
//        assertThat(aplictList).isEmpty(); // 조건에 맞는 데이터가 없다면 비어 있음
//    }
//
//    @Test
//    @DisplayName("Club ID와 AplictStatus로 Aplict 조회 테스트")
//    void testFindByClubIdAndStatus() {
//        Long clubId = 1L; // 가정된 clubId
//        Optional<Aplict> result = aplictRepository.findByClub_ClubIdAndIdAndCheckedAndAplictStatus(clubId, aplict.getId(), true, AplictStatus.PASS);
//        assertThat(result).isPresent();
//        assertThat(result.get().getAplictStatus()).isEqualTo(AplictStatus.PASS);
//    }
//
//    @Test
//    @DisplayName("Custom Repository deleteByClubId 테스트")
//    void testDeleteByClubId() {
//        aplictRepositoryCustom.deleteByClubClubId(1L); // 클럽 ID가 1인 모든 Aplict 삭제
//        em.flush();
//        em.clear();
//
//        List<Aplict> aplictList = aplictRepository.findByClub_ClubIdAndChecked(1L, true);
//        assertThat(aplictList).isEmpty();
//    }
//
//    @Test
//    @DisplayName("동아리 ID로 지원자 조회 테스트")
//    void testFindAllWithProfileByClubId() {
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<Aplict> resultPage = aplictRepositoryCustom.findAllWithProfileCollectionsInByClubClubId(1L, pageable, true);
//
//        assertThat(resultPage.getTotalElements()).isGreaterThanOrEqualTo(0);
//    }
//
//    @Test
//    @DisplayName("불합격자 동아리 지원자 조회 테스트")
//    void testFindAllWithProfileByClubIdAndFailed() {
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<Aplict> resultPage = aplictRepositoryCustom.findAllWithProfileByClubIdAndFailed(1L, pageable, true, AplictStatus.WAIT);
//
//        assertThat(resultPage.getTotalElements()).isGreaterThanOrEqualTo(0);
//    }
//}

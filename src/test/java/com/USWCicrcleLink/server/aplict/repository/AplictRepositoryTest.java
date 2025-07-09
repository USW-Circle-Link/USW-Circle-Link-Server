package com.USWCicrcleLink.server.aplict.repository;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.domain.AplictStatus;
import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.profile.profile.domain.Profile;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource("classpath:application-local.yml")
class AplictRepositoryTest {

    @Autowired
    private AplictRepository aplictRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("existsByProfileAndClubUUID: 특정 프로필과 클럽 UUID로 존재 여부를 확인한다")
    void existsByProfileAndClubUUID() {
        // given
        Profile profile = Profile.builder().studentNumber("12345").build();
        Club club = Club.builder().clubUUID(UUID.randomUUID()).build();
        em.persist(profile);
        em.persist(club);

        Aplict aplict = Aplict.builder()
                .profile(profile)
                .club(club)
                .aplictStatus(AplictStatus.WAIT)
                .build();
        em.persist(aplict);
        em.flush();
        em.clear();

        // when
        boolean exists = aplictRepository.existsByProfileAndClubUUID(profile, club.getClubUUID());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("findClubByAplictId: 지원서 ID로 클럽을 조회한다")
    void findClubByAplictId() {
        // given
        Profile profile = Profile.builder().studentNumber("98765").build();
        Club club = Club.builder().clubUUID(UUID.randomUUID()).build();
        em.persist(profile);
        em.persist(club);

        Aplict aplict = Aplict.builder()
                .profile(profile)
                .club(club)
                .aplictStatus(AplictStatus.WAIT)
                .build();
        em.persist(aplict);
        em.flush();
        em.clear();

        // when
        Optional<Club> result = aplictRepository.findClubByAplictId(aplict.getAplictId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getClubUUID()).isEqualTo(club.getClubUUID());
    }
}

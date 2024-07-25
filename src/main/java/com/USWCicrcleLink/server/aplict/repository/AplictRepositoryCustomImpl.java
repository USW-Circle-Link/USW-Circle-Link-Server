package com.USWCicrcleLink.server.aplict.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class AplictRepositoryCustomImpl implements AplictRepositoryCustom {

    @PersistenceContext
    private final EntityManager em;

    @Override
    @Transactional
    public void deleteByClubClubId(Long clubId) {
        String jpql = "DELETE FROM Aplict a WHERE a.club.clubId = :clubId";
        em.createQuery(jpql)
                .setParameter("clubId", clubId)
                .executeUpdate();
    }
}

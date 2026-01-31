package com.USWCicrcleLink.server.club.application.repository;

import com.USWCicrcleLink.server.club.application.domain.Aplict;
import com.USWCicrcleLink.server.club.application.domain.AplictStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class AplictRepositoryCustomImpl implements AplictRepositoryCustom {

        @PersistenceContext
        private final EntityManager em;

        // 동아리 지원자 조회 (상태별)
        @Override
        public List<Aplict> findAllWithProfileByClubIdAndStatus(Long clubId, AplictStatus status) {
                return em.createQuery(
                                "SELECT ap FROM Aplict ap JOIN FETCH ap.profile p" +
                                                " WHERE ap.club.id = :clubId AND ap.aplictStatus = :status",
                                Aplict.class).setParameter("clubId", clubId)
                                .setParameter("status", status)
                                .getResultList();
        }

        @Override
        public List<Aplict> findAllWithProfileByClubId(Long clubId) {
                return em.createQuery(
                                "SELECT ap FROM Aplict ap JOIN FETCH ap.profile p" +
                                                " WHERE ap.club.id = :clubId",
                                Aplict.class).setParameter("clubId", clubId)
                                .getResultList();
        }

}

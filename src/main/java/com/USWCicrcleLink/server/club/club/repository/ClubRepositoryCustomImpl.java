package com.USWCicrcleLink.server.club.club.repository;

import com.USWCicrcleLink.server.club.club.dto.ClubMembersLeaderCount;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ClubRepositoryCustomImpl implements ClubRepositoryCustom {

    @PersistenceContext
    private final EntityManager em;

    @Override
    public List<ClubMembersLeaderCount> findAllWithMemberAndLeaderCount() {
        String jpql = "SELECT new com.USWCicrcleLink.server.club.club.dto.ClubMembersLeaderCount(c.clubId, COUNT(DISTINCT cm.clubMemberId), COUNT(DISTINCT l.leaderId)) " +
                "FROM Club c " +
                "LEFT JOIN ClubMembers cm ON c.clubId = cm.club.clubId " +
                "LEFT JOIN Leader l ON c.clubId = l.club.clubId " +
                "GROUP BY c.clubId";

        TypedQuery<ClubMembersLeaderCount> query = em.createQuery(jpql, ClubMembersLeaderCount.class);
        return query.getResultList();
    }
}

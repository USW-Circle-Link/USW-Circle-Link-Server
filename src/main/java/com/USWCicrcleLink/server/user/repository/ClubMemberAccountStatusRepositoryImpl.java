package com.USWCicrcleLink.server.user.repository;

import com.USWCicrcleLink.server.user.domain.ExistingMember.ClubMemberAccountStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

public class ClubMemberAccountStatusRepositoryImpl implements ClubMemberAccountStatusCustomRepository {

    @PersistenceContext
    private EntityManager em;

    // 동아리 회원 가입 요청과 임시 동아리 회원 정보 조회
    @Override
    public List<ClubMemberAccountStatus> findAllWithClubMemberTemp(Long clubId) {
        return em.createQuery(
                        "select cmas from ClubMemberAccountStatus cmas" +
                                " join fetch cmas.clubMemberTemp cmt" +
                                " where cmas.club.clubId = :clubId",
                        ClubMemberAccountStatus.class
                ).setParameter("clubId", clubId)
                .getResultList();
    }
}
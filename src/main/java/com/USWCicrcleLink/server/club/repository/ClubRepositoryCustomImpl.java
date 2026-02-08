package com.USWCicrcleLink.server.club.repository;

import com.USWCicrcleLink.server.admin.dto.AdminClubListResponse;
import com.USWCicrcleLink.server.global.s3File.Service.S3FileUploadService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import com.USWCicrcleLink.server.club.domain.RecruitmentStatus;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Transactional
public class ClubRepositoryCustomImpl implements ClubRepositoryCustom {

        @PersistenceContext
        private EntityManager em;

        private final S3FileUploadService s3FileUploadService;

        @Override
        public Page<AdminClubListResponse> findAllWithMemberAndLeaderCount(Pageable pageable) {
                String jpql = "SELECT new com.USWCicrcleLink.server.admin.dto.AdminClubListResponse(" +
                                "c.clubuuid, c.department, c.clubName, c.leaderName, " +
                                "(COUNT(DISTINCT cm.clubMemberId) + MAX(CASE WHEN l IS NOT NULL THEN 1 ELSE 0 END))) " +
                                "FROM Club c " +
                                "LEFT JOIN ClubMembers cm ON cm.clubId = c.clubId " +
                                "LEFT JOIN Leader l ON l.clubId = c.clubId " +
                                "GROUP BY c.clubId, c.clubuuid, c.department, c.clubName, c.leaderName";

                String countJpql = "SELECT COUNT(c) FROM Club c";

                TypedQuery<AdminClubListResponse> query = em.createQuery(jpql, AdminClubListResponse.class);
                query.setFirstResult((int) pageable.getOffset());
                query.setMaxResults(pageable.getPageSize());

                Long totalCount = em.createQuery(countJpql, Long.class).getSingleResult();

                List<AdminClubListResponse> results = query.getResultList();

                return new PageImpl<>(results, pageable, totalCount);
        }

        @Override
        public void deleteClubAndDependencies(Long clubId) {

                em.createQuery("DELETE FROM ClubHashtag ch WHERE ch.club.clubId = :clubId")
                                .setParameter("clubId", clubId)
                                .executeUpdate();

                em.createQuery("DELETE FROM ClubCategoryMapping cm WHERE cm.club.clubId = :clubId")
                                .setParameter("clubId", clubId)
                                .executeUpdate();

                em.createQuery("DELETE FROM ClubMembers cm WHERE cm.club.clubId = :clubId")
                                .setParameter("clubId", clubId)
                                .executeUpdate();

                em.createQuery("DELETE FROM Aplict a WHERE a.club.clubId = :clubId")
                                .setParameter("clubId", clubId)
                                .executeUpdate();

                em.createQuery("DELETE FROM ClubInfoPhoto cip WHERE cip.clubInfo.club.clubId = :clubId")
                                .setParameter("clubId", clubId)
                                .executeUpdate();

                em.createQuery("DELETE FROM ClubMainPhoto cmp WHERE cmp.club.clubId = :clubId")
                                .setParameter("clubId", clubId)
                                .executeUpdate();

                em.createQuery("DELETE FROM ClubInfo ci WHERE ci.club.clubId = :clubId")
                                .setParameter("clubId", clubId)
                                .executeUpdate();

                em.createQuery("DELETE FROM Leader l WHERE l.club.clubId = :clubId")
                                .setParameter("clubId", clubId)
                                .executeUpdate();

                List<String> clubInfoPhotoKeys = em.createQuery(
                                "SELECT cip.clubInfoPhotoS3Key FROM ClubInfoPhoto cip WHERE cip.clubInfo.club.clubId = :clubId",
                                String.class)
                                .setParameter("clubId", clubId)
                                .getResultList();

                List<String> clubMainPhotoKeys = em.createQuery(
                                "SELECT cmp.clubMainPhotoS3Key FROM ClubMainPhoto cmp WHERE cmp.club.clubId = :clubId",
                                String.class)
                                .setParameter("clubId", clubId)
                                .getResultList();

                List<String> s3Keys = new ArrayList<>();
                s3Keys.addAll(clubInfoPhotoKeys);
                s3Keys.addAll(clubMainPhotoKeys);

                if (!s3Keys.isEmpty()) {
                        s3FileUploadService.deleteFiles(s3Keys);
                }

                em.createQuery("DELETE FROM Club c WHERE c.clubId = :clubId")
                                .setParameter("clubId", clubId)
                                .executeUpdate();
        }

        @Override
        public List<Long> searchClubIds(Boolean open, List<String> filter) {
                StringBuilder jpql = new StringBuilder("SELECT DISTINCT c.clubId FROM Club c ");
                jpql.append("LEFT JOIN ClubInfo ci ON ci.club.clubId = c.clubId ");
                jpql.append("LEFT JOIN ClubCategoryMapping ccm ON ccm.club.clubId = c.clubId ");
                jpql.append("LEFT JOIN ClubCategory cc ON cc.clubCategoryId = ccm.clubCategory.clubCategoryId ");

                List<String> whereClauses = new ArrayList<>();

                // 1. 모집 여부 필터
                if (open != null) {
                        whereClauses.add("ci.recruitmentStatus = :openStatus");
                }

                // 2. 카테고리 이름 필터 (OR 조건)
                if (filter != null && !filter.isEmpty()) {
                        StringBuilder filterClause = new StringBuilder();
                        filterClause.append("(");
                        for (int i = 0; i < filter.size(); i++) {
                                if (i > 0) {
                                        filterClause.append(" OR ");
                                }
                                filterClause.append("cc.clubCategoryName LIKE :filter").append(i);
                        }
                        filterClause.append(")");
                        whereClauses.add(filterClause.toString());
                }

                if (!whereClauses.isEmpty()) {
                        jpql.append("WHERE ").append(String.join(" AND ", whereClauses));
                }

                TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);

                if (open != null) {
                        query.setParameter("openStatus",
                                        open ? RecruitmentStatus.OPEN : RecruitmentStatus.CLOSE);
                }

                if (filter != null && !filter.isEmpty()) {
                        for (int i = 0; i < filter.size(); i++) {
                                query.setParameter("filter" + i, "%" + filter.get(i) + "%");
                        }
                }

                return query.getResultList();
        }
}

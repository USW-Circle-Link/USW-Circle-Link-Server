package com.USWCicrcleLink.server.aplict.repository;

import com.USWCicrcleLink.server.aplict.domain.ApplicationAnswer;
import com.USWCicrcleLink.server.aplict.dto.UserApplicationResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationAnswerRepository extends JpaRepository<ApplicationAnswer, Long> {

    @Query("SELECT new com.USWCicrcleLink.server.aplict.dto.UserApplicationResponse(" +
            "q.content, " +
            "COALESCE(o.content, a.answerText)) " +
            "FROM ApplicationAnswer a " +
            "JOIN a.question q " +
            "LEFT JOIN a.option o " +
            "WHERE a.aplict.aplictId = :aplictId " +
            "ORDER BY q.sequence")
    List<UserApplicationResponse> findMyApplicationAnswers(@Param("aplictId") Long aplictId);
}
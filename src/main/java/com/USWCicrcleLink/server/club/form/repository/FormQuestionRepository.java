package com.USWCicrcleLink.server.club.form.repository;

import com.USWCicrcleLink.server.club.form.domain.FormQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormQuestionRepository extends JpaRepository<FormQuestion, Long> {
}
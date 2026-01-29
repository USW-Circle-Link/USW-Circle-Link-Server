package com.USWCicrcleLink.server.club.application.domain;

import com.USWCicrcleLink.server.club.leader.domain.FormQuestion;
import com.USWCicrcleLink.server.club.leader.domain.FormQuestionOption;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "APPLICATION_ANSWER")
public class ApplicationAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long answerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aplict_id")
    @Setter
    private Aplict aplict;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private FormQuestion question;

    // 선택형 질문일 경우 (RADIO, CHECKBOX 등)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private FormQuestionOption option;

    // 서술형 질문일 경우
    @Column(columnDefinition = "TEXT")
    private String answerText;

    @Builder
    public ApplicationAnswer(FormQuestion question, FormQuestionOption option, String answerText) {
        this.question = question;
        this.option = option;
        this.answerText = answerText;
    }
}


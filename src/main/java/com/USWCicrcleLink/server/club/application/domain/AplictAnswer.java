package com.USWCicrcleLink.server.club.application.domain;

import com.USWCicrcleLink.server.club.leader.domain.FormQuestion;
import com.USWCicrcleLink.server.club.leader.domain.FormQuestionOption;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "application_answer")
public class AplictAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long answerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aplict_id", nullable = false)
    private Aplict aplict;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private FormQuestion formQuestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private FormQuestionOption option; // Optional for multiple choice

    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    @Builder
    public AplictAnswer(Aplict aplict, FormQuestion formQuestion, FormQuestionOption option, String answerText) {
        this.aplict = aplict;
        this.formQuestion = formQuestion;
        this.option = option;
        this.answerText = answerText;
    }

    public void setAplict(Aplict aplict) {
        this.aplict = aplict;
    }
}

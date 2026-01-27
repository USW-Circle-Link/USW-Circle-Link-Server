package com.USWCicrcleLink.server.aplict.domain;

import com.USWCicrcleLink.server.clubLeader.domain.FormQuestion;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "APLICT_ANSWER_TABLE")
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

    @Column(name = "answer_content", columnDefinition = "TEXT")
    private String content;

    public static AplictAnswer createAnswer(Aplict aplict, FormQuestion formQuestion, String content) {
        return AplictAnswer.builder()
                .aplict(aplict)
                .formQuestion(formQuestion)
                .content(content)
                .build();
    }
}

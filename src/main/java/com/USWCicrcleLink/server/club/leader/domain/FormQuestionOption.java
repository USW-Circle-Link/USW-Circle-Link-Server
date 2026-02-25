package com.USWCicrcleLink.server.club.leader.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "FORM_QUESTION_OPTION")
public class FormQuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long optionId;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private FormQuestion formQuestion;

    @Column(length = 100, nullable = false)
    private String content;

    @Column(length = 50)
    private String value;

    private int sequence;

    @Builder
    public FormQuestionOption(Long optionId, String content, String value, int sequence) {
        this.optionId = optionId;
        this.content = content;
        this.value = value;
        this.sequence = sequence;
    }
}

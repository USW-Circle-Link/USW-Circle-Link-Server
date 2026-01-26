package com.USWCicrcleLink.server.club.form.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "FORM_QUESTION_OPTION")
public class FormQuestionOption {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long optionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    @Setter
    private FormQuestion question;

    private String content;
    private String value;
    private int sequence;

    @Builder
    public FormQuestionOption(String content, String value, int sequence) {
        this.content = content;
        this.value = value;
        this.sequence = sequence;
    }
}
package com.USWCicrcleLink.server.club.form.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "FORM_QUESTION")
public class FormQuestion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id")
    @Setter
    private ClubForm clubForm;

    @Enumerated(EnumType.STRING)
    private QuestionType type;

    private String content;
    private boolean required;
    private int sequence;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FormQuestionOption> options = new ArrayList<>();

    @Builder
    public FormQuestion(QuestionType type, String content, boolean required, int sequence) {
        this.type = type;
        this.content = content;
        this.required = required;
        this.sequence = sequence;
    }

    public void addOption(FormQuestionOption option) {
        this.options.add(option);
        option.setQuestion(this);
    }
}
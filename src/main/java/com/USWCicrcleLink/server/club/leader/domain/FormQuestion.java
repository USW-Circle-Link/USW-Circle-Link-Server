package com.USWCicrcleLink.server.club.leader.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "FORM_QUESTION")
public class FormQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    @Setter // 연관관계 메서드용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private ClubForm clubForm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RenderType renderType;

    @Column(nullable = false)
    private String content;

    private boolean required;
    private int sequence;

    // 질문 저장시 옵션들도 자동 저장됨
    @OneToMany(mappedBy = "formQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FormQuestionOption> options = new ArrayList<>();

    @Builder
    public FormQuestion(QuestionType type, String content, boolean required, int sequence) {
        this.type = type;
        this.renderType = type.getDefaultRenderType(); // Type에 따라 RenderType 자동 결정
        this.content = content;
        this.required = required;
        this.sequence = sequence;
    }

    public void addOption(FormQuestionOption option) {
        this.options.add(option);
        option.setFormQuestion(this);
    }
}

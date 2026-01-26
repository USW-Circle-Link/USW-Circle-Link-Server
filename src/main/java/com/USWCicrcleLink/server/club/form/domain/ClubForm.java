package com.USWCicrcleLink.server.club.form.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "CLUB_FORM")
public class ClubForm {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long formId;

    private Long clubId;

    @Column(length = 100)
    private String title;

    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    private FormStatus status;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    @OneToMany(mappedBy = "clubForm", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FormQuestion> questions = new ArrayList<>();

    @Builder
    public ClubForm(Long clubId, String title, String description, LocalDateTime startAt, LocalDateTime endAt) {
        this.clubId = clubId;
        this.title = title;
        this.description = description;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = FormStatus.DRAFT;
    }

    public void addQuestion(FormQuestion question) {
        this.questions.add(question);
        question.setClubForm(this);
    }

    public void updateStatus(FormStatus status) {
        this.status = status;
    }
}
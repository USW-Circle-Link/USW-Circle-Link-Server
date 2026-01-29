package com.USWCicrcleLink.server.club.leader.domain;

import com.USWCicrcleLink.server.club.domain.Club;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "CLUB_FORM")
public class ClubForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long formId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormStatus status;


    private LocalDateTime startAt;
    private LocalDateTime endAt;

    @Column(nullable = false)
    private Long createdBy;

    // 핵심: CascadeType.ALL -> 폼 저장시 질문들도 자동 저장됨
    @OneToMany(mappedBy = "clubForm", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FormQuestion> questions = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    public ClubForm(Club club, String title, String description, LocalDateTime startAt, LocalDateTime endAt, Long createdBy) {
        this.club = club;
        this.title = title;
        this.description = description;
        this.status = FormStatus.DRAFT; // 기본 상태 DRAFT
        this.startAt = startAt;
        this.endAt = endAt;
        this.createdBy = createdBy;
    }

    // 연관관계 편의 메서드
    public void addQuestion(FormQuestion question) {
        this.questions.add(question);
        question.setClubForm(this);
    }

    // 상태 변경 메서드 DRAFT PUBLISHED CLOSED
    public void updateStatus(FormStatus status) {
        this.status = status;
    }

}


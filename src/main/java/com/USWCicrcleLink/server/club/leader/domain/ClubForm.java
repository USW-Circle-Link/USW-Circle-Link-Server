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
@Table(name = "club_form")
public class ClubForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long formId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @Column(columnDefinition = "TEXT")
    private String description;

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
    public ClubForm(Club club, String description, Long createdBy) {
        this.club = club;
        this.description = description;
        this.createdBy = createdBy;
    }

    // 연관관계 편의 메서드
    public void addQuestion(FormQuestion question) {
        this.questions.add(question);
        question.setClubForm(this);
    }

}

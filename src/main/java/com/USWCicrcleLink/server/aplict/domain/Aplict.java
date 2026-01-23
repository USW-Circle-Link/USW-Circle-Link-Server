package com.USWCicrcleLink.server.aplict.domain;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.form.domain.ClubForm;
import com.USWCicrcleLink.server.profile.domain.Profile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "APLICT_TABLE",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_aplict_active", columnNames = {"club_id", "profile_id", "aplict_checked"})
        }
)
public class Aplict {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "aplict_id")
    private Long aplictId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    // ✨ [추가] 어떤 모집 공고(폼)에 대한 지원인지 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id")
    private ClubForm clubForm;

    // ✨ [추가] 지원서에 딸린 답변들
    @OneToMany(mappedBy = "aplict", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ApplicationAnswer> answers = new ArrayList<>();

    @Builder.Default
    @Column(name = "aplict_uuid", nullable = false, unique = true, updatable = false)
    private UUID aplictUUID = UUID.randomUUID();

    @Column(name = "aplict_submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "aplict_status", nullable = false, length = 10)
    private AplictStatus aplictStatus = AplictStatus.WAIT;

    @Column(name = "aplict_checked")
    private boolean checked = false;

    @Column(name = "aplict_delete_date")
    private LocalDateTime deleteDate;

    @PrePersist
    public void generateUUID() {
        if (this.aplictUUID == null) {
            this.aplictUUID = UUID.randomUUID();
        }
    }

    public void updateAplictStatus(AplictStatus newStatus, boolean checked, LocalDateTime deleteDate) {
        this.aplictStatus = newStatus;
        this.checked = checked;
        this.deleteDate = deleteDate;
    }

    // ClubLeaderService에서 사용하는 메서드 복구
    public void updateFailedAplictStatus(AplictStatus newStatus) {
        this.aplictStatus = newStatus;
    }

    // 답변 추가 편의 메서드
    public void addAnswer(ApplicationAnswer answer) {
        this.answers.add(answer);
        answer.setAplict(this);
    }
}
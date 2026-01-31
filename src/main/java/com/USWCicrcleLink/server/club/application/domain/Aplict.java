package com.USWCicrcleLink.server.club.application.domain;

import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.user.profile.domain.Profile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "APLICT_TABLE", uniqueConstraints = {
        @UniqueConstraint(name = "uk_aplict_active", columnNames = { "club_id", "profile_id" })
})
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

    @Builder.Default
    @Column(name = "aplict_uuid", nullable = false, unique = true, updatable = false)
    private UUID aplictUUID = UUID.randomUUID();

    @Column(name = "aplict_submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "aplict_status", nullable = false, length = 10)
    private AplictStatus aplictStatus = AplictStatus.WAIT;

    // checked field removed

    @Column(name = "aplict_delete_date")
    private LocalDateTime deleteDate;

    @PrePersist
    public void generateUUID() {
        if (this.aplictUUID == null) {
            this.aplictUUID = UUID.randomUUID();
        }
    }

    public void updateAplictStatus(AplictStatus newStatus, LocalDateTime deleteDate) {
        this.aplictStatus = newStatus;
        this.deleteDate = deleteDate;
    }

    public void updateFailedAplictStatus(AplictStatus newStatus) {
        this.aplictStatus = newStatus;
    }

    @Builder.Default
    @OneToMany(mappedBy = "aplict", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<AplictAnswer> answers = new java.util.ArrayList<>();

    public void addAnswer(AplictAnswer answer) {
        this.answers.add(answer);
        answer.setAplict(this);
    }
}

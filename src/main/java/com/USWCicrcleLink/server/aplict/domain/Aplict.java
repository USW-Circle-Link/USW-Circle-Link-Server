package com.USWCicrcleLink.server.aplict.domain;

import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.profile.domain.Profile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "APLICT_TABLE")
public class Aplict {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "aplict_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @Column(name = "aplict_google_form_url", nullable = false)
    private String aplictGoogleFormUrl;

    @Column(name = "aplict_submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "aplict_status", nullable = false)
    private AplictStatus aplictStatus;
}
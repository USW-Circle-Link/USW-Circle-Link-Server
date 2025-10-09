package com.USWCicrcleLink.server.user.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "event_verification_table",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_event_user_club", columnNames = {"user_uuid", "club_uuid"})
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_verification_id", nullable = false)
    private Long id;

    @Column(name = "user_uuid", nullable = false, columnDefinition = "BINARY(16)")
    private UUID userUUID;

    @Column(name = "club_uuid", nullable = false, columnDefinition = "BINARY(16)")
    private UUID clubUUID;

    @Column(name = "user_account", nullable = false, length = 20)
    private String userAccount;

    @Column(name = "email", nullable = false, length = 30)
    private String email;

    @Column(name = "verified", nullable = false, columnDefinition = "bit(1)")
    private boolean verified;

    @Column(name = "verified_at", nullable = false)
    private LocalDateTime verifiedAt;

    public static EventVerification create(UUID userUUID, UUID clubUUID, String userAccount, String email) {
        return EventVerification.builder()
                .userUUID(userUUID)
                .clubUUID(clubUUID)
                .userAccount(userAccount)
                .email(email)
                .verified(true)
                .verifiedAt(LocalDateTime.now())
                .build();
    }
}

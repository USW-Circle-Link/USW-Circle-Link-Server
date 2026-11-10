package com.USWCicrcleLink.server.notification.outbox;

import com.USWCicrcleLink.server.club.application.domain.AplictStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Durable publication snapshot. Sending and retries are implemented in a later step. */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification_outbox", uniqueConstraints = {
        @UniqueConstraint(name = "uk_outbox_application_event", columnNames = {"application_uuid", "event_type"})
})
public class NotificationOutbox {
    public enum EventType { APPLICATION_RESULT }
    public enum Status { PENDING, SKIPPED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_uuid", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID applicationUuid;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, updatable = false, length = 32)
    private EventType eventType;

    @Column(name = "recipient_profile_id", nullable = false, updatable = false)
    private Long recipientProfileId;

    @Column(name = "recipient_token", updatable = false, columnDefinition = "TEXT")
    private String recipientToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false, updatable = false, length = 10)
    private AplictStatus result;

    @Column(name = "title", nullable = false, updatable = false, length = 255)
    private String title;

    @Column(name = "body", nullable = false, updatable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private Status status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    public static NotificationOutbox applicationResult(UUID applicationUuid, Long profileId,
            String token, AplictStatus result, String title, String body) {
        if (result != AplictStatus.PASS && result != AplictStatus.FAIL) {
            throw new IllegalArgumentException("Only PASS or FAIL can create an announcement task");
        }
        Objects.requireNonNull(applicationUuid, "applicationUuid");
        Objects.requireNonNull(profileId, "profileId");
        if (profileId <= 0 || title == null || title.isBlank() || title.length() > 255
                || body == null || body.isBlank()) {
            throw new IllegalArgumentException("A positive profile ID and notification title/body are required");
        }
        var task = new NotificationOutbox();
        task.applicationUuid = applicationUuid;
        task.eventType = EventType.APPLICATION_RESULT;
        task.recipientProfileId = profileId;
        task.recipientToken = token == null || token.isBlank() ? null : token.trim();
        task.result = result;
        task.title = title;
        task.body = body;
        task.createdAt = Instant.now();
        task.status = task.recipientToken == null ? Status.SKIPPED : Status.PENDING;
        task.nextAttemptAt = task.status == Status.PENDING ? task.createdAt : null;
        task.lastError = task.status == Status.SKIPPED ? "NO_FCM_TOKEN" : null;
        return task;
    }
}

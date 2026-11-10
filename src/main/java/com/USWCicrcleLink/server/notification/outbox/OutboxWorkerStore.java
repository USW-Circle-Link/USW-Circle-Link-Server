package com.USWCicrcleLink.server.notification.outbox;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.*;
import java.util.*;

/** Only short DB transactions live here. Network calls never run inside these callbacks. */
public class OutboxWorkerStore {
    private final NotificationOutboxRepository repository;
    private final TransactionTemplate tx;
    private final Clock clock;

    public OutboxWorkerStore(NotificationOutboxRepository repository, PlatformTransactionManager manager, Clock clock) {
        this.repository = repository;
        this.clock = clock;
        tx = new TransactionTemplate(manager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public record Delivery(Long id, UUID claimToken, String token, String title, String body, Instant createdAt) { }

    public Optional<Delivery> claim() {
        return tx.execute(s -> repository.lockNext(clock.instant()).flatMap(row -> {
            if (row.getAttemptCount() >= 5) {
                row.finish(NotificationOutbox.Status.FAILED, clock.instant(), "ATTEMPTS_EXHAUSTED_AFTER_LEASE", null);
                return Optional.empty();
            }
            UUID token = UUID.randomUUID();
            row.claim(clock.instant(), token);
            return Optional.of(new Delivery(row.getId(), token, row.getRecipientToken(), row.getTitle(), row.getBody(), row.getCreatedAt()));
        }));
    }

    public boolean complete(Delivery delivery, OutboxSender.Outcome outcome) {
        return Boolean.TRUE.equals(tx.execute(s -> {
            var row = repository.lockById(delivery.id()).orElse(null);
            if (row == null || row.getStatus() != NotificationOutbox.Status.PROCESSING
                    || !delivery.claimToken().equals(row.getClaimToken())) return false;
            Instant now = clock.instant();
            if (outcome.accepted()) {
                row.finish(NotificationOutbox.Status.SENT, now, null, null);
            } else if (outcome.retryable() && row.getAttemptCount() < 5) {
                Duration delay = Duration.ofSeconds(1L << row.getAttemptCount());
                if (outcome.retryAfter() != null && outcome.retryAfter().compareTo(delay) > 0) delay = outcome.retryAfter();
                row.finish(NotificationOutbox.Status.RETRY, now, outcome.error(), now.plus(delay));
            } else {
                row.finish(NotificationOutbox.Status.FAILED, now, outcome.error(), null);
            }
            return true;
        }));
    }

    public long oldestUnfinishedAgeMillis() {
        return tx.execute(s -> repository.oldestUnfinished(List.of(NotificationOutbox.Status.PENDING,
                NotificationOutbox.Status.RETRY, NotificationOutbox.Status.PROCESSING))
                .map(created -> Math.max(0, Duration.between(created, clock.instant()).toMillis())).orElse(0L));
    }
}

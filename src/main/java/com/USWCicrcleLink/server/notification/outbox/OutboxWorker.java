package com.USWCicrcleLink.server.notification.outbox;

import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.time.Clock;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OutboxWorker {
    private final OutboxWorkerStore store;
    private final OutboxSender sender;
    private final Clock clock;

    public OutboxWorker(OutboxWorkerStore store, OutboxSender sender, Clock clock) {
        this.store = store;
        this.sender = sender;
        this.clock = clock;
    }

    public boolean runOnce() {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("Run the worker outside an application transaction");
        }
        var delivery = store.claim();
        if (delivery.isEmpty()) return false;
        OutboxSender.Outcome outcome;
        try {
            outcome = sender.send(delivery.get());
            if (outcome == null) outcome = OutboxSender.Outcome.retry("EMPTY_TRANSPORT_RESULT", null);
        } catch (RuntimeException error) {
            outcome = OutboxSender.Outcome.retry("TRANSPORT_EXCEPTION", null);
        }
        if (store.complete(delivery.get(), outcome) && outcome.accepted()) {
            log.debug("Outbox FCM accepted id={}, latencyMs={}", delivery.get().id(),
                    Duration.between(delivery.get().createdAt(), clock.instant()).toMillis());
        }
        return true;
    }
}

package com.USWCicrcleLink.server.notification.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@RequiredArgsConstructor
public class OutboxPollingScheduler {
    private final OutboxWorker worker;
    private final OutboxWorkerStore store;

    @Scheduled(fixedDelayString = "${notification.outbox.worker.poll-interval-ms:1000}",
            initialDelayString = "${notification.outbox.worker.initial-delay-ms:1000}")
    public void poll() {
        try {
            for (int i = 0; i < 10 && worker.runOnce(); i++) { /* Claim one task at a time. */ }
            log.debug("Outbox oldestUnfinishedAgeMs={}", store.oldestUnfinishedAgeMillis());
        } catch (RuntimeException error) {
            // Do not log tokens, payloads, credentials, or remote response bodies.
            log.warn("Outbox polling failed: {}", error.getClass().getSimpleName());
        }
    }
}

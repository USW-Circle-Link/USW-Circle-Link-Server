package com.USWCicrcleLink.server.notification.outbox;

import java.time.Duration;

@FunctionalInterface
public interface OutboxSender {
    Outcome send(OutboxWorkerStore.Delivery delivery);

    record Outcome(boolean accepted, boolean retryable, String error, Duration retryAfter) {
        public static Outcome sent() { return new Outcome(true, false, null, null); }
        public static Outcome retry(String error, Duration after) { return new Outcome(false, true, error, after); }
        public static Outcome failed(String error) { return new Outcome(false, false, error, null); }
    }
}

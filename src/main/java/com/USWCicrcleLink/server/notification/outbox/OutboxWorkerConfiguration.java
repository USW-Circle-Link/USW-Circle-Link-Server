package com.USWCicrcleLink.server.notification.outbox;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import java.nio.file.*;
import java.time.Clock;

@Configuration(proxyBeanMethods = false)
@EnableScheduling
@ConditionalOnProperty(name = "notification.outbox.worker.enabled", havingValue = "true")
public class OutboxWorkerConfiguration {
    private final Environment environment;
    private final Clock clock = Clock.systemUTC();

    public OutboxWorkerConfiguration(Environment environment) {
        if (!environment.matchesProfiles("outbox-test") || environment.matchesProfiles("prod")
                || !environment.getProperty("notification.publication.outbox-enabled", Boolean.class, false)) {
            throw new IllegalStateException("Worker currently requires outbox-test, no prod profile, and Outbox publication enabled");
        }
        this.environment = environment;
    }

    @Bean OutboxWorkerStore outboxWorkerStore(NotificationOutboxRepository repository, PlatformTransactionManager manager) {
        return new OutboxWorkerStore(repository, manager, clock);
    }

    @Bean OutboxSender outboxSender() {
        String url = environment.getProperty("fcm.api-url", "https://fcm.googleapis.com/v1/projects/usw-circle-link/messages:send");
        return new OutboxFcmClient(url, new OutboxGoogleTokenProvider(
                Path.of(environment.getProperty("firebase.config-path", "")), 2000, 5000), 2000, 5000);
    }

    @Bean OutboxWorker outboxWorker(OutboxWorkerStore store, OutboxSender sender) {
        return new OutboxWorker(store, sender, clock);
    }

    @Bean OutboxPollingScheduler outboxPollingScheduler(OutboxWorker worker, OutboxWorkerStore store) {
        return new OutboxPollingScheduler(worker, store);
    }
}

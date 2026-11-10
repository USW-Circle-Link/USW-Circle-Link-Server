package com.USWCicrcleLink.server.notification.outbox;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import java.util.Optional;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OutboxWorkerConfigurationTest {
    @Test void disabledByDefault(){
        new ApplicationContextRunner().withUserConfiguration(OutboxWorkerConfiguration.class)
            .run(c->assertThat(c).doesNotHaveBean(OutboxWorker.class));
    }
    @Test void refusesProductionProfile(){
        var env=new MockEnvironment().withProperty("notification.publication.outbox-enabled","true");env.setActiveProfiles("prod");
        assertThatIllegalStateException().isThrownBy(()->new OutboxWorkerConfiguration(env));
    }
    @Test void requiresOutboxPublicationMode(){
        var env=new MockEnvironment();env.setActiveProfiles("outbox-test");
        assertThatIllegalStateException().isThrownBy(()->new OutboxWorkerConfiguration(env));
    }
    @Test void testAndProductionProfilesTogetherAreRefused(){
        var env=new MockEnvironment().withProperty("notification.publication.outbox-enabled","true");env.setActiveProfiles("outbox-test","prod");
        assertThatIllegalStateException().isThrownBy(()->new OutboxWorkerConfiguration(env));
    }
    @Test void enabledSchedulerActuallyPolls() {
        var called=new CountDownLatch(1);var repo=mock(NotificationOutboxRepository.class);var tx=mock(PlatformTransactionManager.class);
        when(tx.getTransaction(any())).thenAnswer(i->new SimpleTransactionStatus());
        when(repo.lockNext(any())).thenAnswer(i->{called.countDown();return Optional.empty();});
        new ApplicationContextRunner().withUserConfiguration(OutboxWorkerConfiguration.class)
            .withBean(NotificationOutboxRepository.class,()->repo).withBean(PlatformTransactionManager.class,()->tx)
            .withPropertyValues("spring.profiles.active=outbox-test","notification.publication.outbox-enabled=true",
                "notification.outbox.worker.enabled=true","notification.outbox.worker.poll-interval-ms=20","notification.outbox.worker.initial-delay-ms=20")
            .run(c->{assertThat(c).hasSingleBean(OutboxWorker.class);assertThat(called.await(3,TimeUnit.SECONDS)).isTrue();});
    }
}

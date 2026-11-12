package com.USWCicrcleLink.server.notification.outbox;

import com.USWCicrcleLink.server.club.application.domain.AplictStatus;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.io.IOException;
import java.nio.file.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@SpringJUnitConfig(NotificationOutboxRepositoryTest.Config.class)
@TestPropertySource(properties={"spring.jpa.hibernate.ddl-auto=validate","spring.jpa.open-in-view=false"})
@EnabledIfEnvironmentVariable(named="OUTBOX_MYSQL_URL",matches="jdbc:mysql://127\\.0\\.0\\.1:[0-9]+/outbox_week11.*")
class OutboxWorkerTest {
    @Autowired NotificationOutboxRepository repository;
    @Autowired PlatformTransactionManager manager;
    @Autowired HikariDataSource pool;
    WireMockServer wire;MutableClock clock;OutboxWorkerStore store;OutboxWorker worker;
    @BeforeEach void setup(){
        repository.deleteAllInBatch();wire=new WireMockServer(options().dynamicPort());wire.start();
        wire.stubFor(post(urlEqualTo("/send")).willReturn(okJson("{\"name\":\"fake\"}")));
        clock=new MutableClock(Instant.now().plusSeconds(2));store=new OutboxWorkerStore(repository,manager,clock);
        worker=new OutboxWorker(store,new OutboxFcmClient(wire.baseUrl()+"/send",()->"fake-token",2000,5000),clock);
    }
    @AfterEach void stop(TestInfo info) throws Exception {
        if(wire==null)return;
        try {
            var data=new LinkedHashMap<String,Object>();
            data.put("test",info.getTestMethod().orElseThrow().getName());
            data.put("scope","Local MySQL + WireMock + fake bearer; check JUnit XML for pass/fail");
            data.put("clock",info.getDisplayName().startsWith("two500")?"real clock":"controlled clock for retry policy");
            data.put("tasks",repository.findAll().stream().map(t->{var r=new LinkedHashMap<String,Object>();
                r.put("id",t.getId());r.put("status",t.getStatus().name());r.put("attempts",t.getAttemptCount());
                r.put("lastError",t.getLastError());r.put("createdAt",t.getCreatedAt().toString());
                r.put("sentAt",Objects.toString(t.getSentAt(),null));r.put("nextAttemptAt",Objects.toString(t.getNextAttemptAt(),null));return r;}).toList());
            data.put("fcmCalls",wire.getAllServeEvents().stream()
                .sorted(Comparator.comparing(e->e.getRequest().getLoggedDate()))
                .map(e->{var r=new LinkedHashMap<String,Object>();r.put("configuredStatus",e.getResponse().getStatus());
                    r.put("fault",Objects.toString(e.getResponse().getFault(),null));r.put("path",e.getRequest().getUrl());return r;}).toList());
            var dir=Path.of(System.getenv().getOrDefault("OUTBOX_EVIDENCE_DIR","evidence/week11/outbox-worker/manual"));Files.createDirectories(dir);
            Files.writeString(dir.resolve(info.getTestMethod().orElseThrow().getName()+".json"),new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(data));
        } finally {wire.stop();}
    }
    NotificationOutbox task(){return repository.saveAndFlush(NotificationOutbox.applicationResult(UUID.randomUUID(),42L,"FAKE_TOKEN",AplictStatus.PASS,"title","body"));}
    NotificationOutbox row(NotificationOutbox t){return repository.findById(t.getId()).orElseThrow();}
    @Test void sendsAfterReleasingDatabaseConnection(){
        var t=task();var sender=new OutboxFcmClient(wire.baseUrl()+"/send",()->{
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
            assertThat(pool.getHikariPoolMXBean().getActiveConnections()).isZero();return "fake-token";
        },2000,5000);
        assertThat(new OutboxWorker(store,sender,clock).runOnce()).isTrue();
        assertThat(row(t).getStatus()).isEqualTo(NotificationOutbox.Status.SENT);
        assertThat(row(t).getSentAt()).isNotNull();assertThat(row(t).getAttemptCount()).isEqualTo(1);
        assertThat(worker.runOnce()).isFalse();wire.verify(1,postRequestedFor(urlEqualTo("/send")));
    }
    @Test void two500ResponsesThenSuccessWithin60Seconds() throws Exception {
        wire.resetAll();
        wire.stubFor(post(urlEqualTo("/send")).inScenario("recover").whenScenarioStateIs(Scenario.STARTED).willReturn(aResponse().withStatus(500)).willSetStateTo("second"));
        wire.stubFor(post(urlEqualTo("/send")).inScenario("recover").whenScenarioStateIs("second").willReturn(aResponse().withStatus(500)).willSetStateTo("ok"));
        wire.stubFor(post(urlEqualTo("/send")).inScenario("recover").whenScenarioStateIs("ok").willReturn(okJson("{}")));
        var ids=List.of(task().getId(),task().getId(),task().getId());var real=Clock.systemUTC();
        var w=new OutboxWorker(new OutboxWorkerStore(repository,manager,real),new OutboxFcmClient(wire.baseUrl()+"/send",()->"fake",2000,5000),real);
        long started=System.nanoTime();
        while(repository.findAllById(ids).stream().anyMatch(t->t.getStatus()!=NotificationOutbox.Status.SENT)&&System.nanoTime()-started<15_000_000_000L){w.runOnce();Thread.sleep(50);}
        assertThat(repository.findAllById(ids)).allSatisfy(t->{assertThat(t.getStatus()).isEqualTo(NotificationOutbox.Status.SENT);assertThat(Duration.between(t.getCreatedAt(),t.getSentAt())).isLessThan(Duration.ofSeconds(60));});
        wire.verify(5,postRequestedFor(urlEqualTo("/send")));
    }
    @Test void respectsRetryAfter(){
        wire.stubFor(post(urlEqualTo("/send")).willReturn(aResponse().withStatus(429).withHeader("Retry-After","3")));
        var t=task();worker.runOnce();assertThat(row(t).getStatus()).isEqualTo(NotificationOutbox.Status.RETRY);
        clock.advance(2);assertThat(worker.runOnce()).isFalse();clock.advance(1);assertThat(worker.runOnce()).isTrue();
        wire.verify(2,postRequestedFor(urlEqualTo("/send")));
    }
    @Test void httpDateRetryAfterAndMalformedFallback(){
        var client=new OutboxFcmClient(wire.baseUrl()+"/send",()->"fake",2000,5000);var t=task();var delivery=store.claim().orElseThrow();
        wire.stubFor(post(urlEqualTo("/send")).willReturn(aResponse().withStatus(429).withHeader("Retry-After",java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(10)))));
        var response=client.send(delivery);
        assertThat(response.error()).isEqualTo("HTTP_429");
        assertThat(response.retryAfter()).isGreaterThan(Duration.ZERO).isLessThanOrEqualTo(Duration.ofSeconds(11));
        wire.stubFor(post(urlEqualTo("/send")).willReturn(aResponse().withStatus(429).withHeader("Retry-After","invalid")));
        assertThat(client.send(delivery).retryAfter()).isNull();
    }
    @Test void permanent400And404AreNotRetried(){
        for(int code:List.of(400,404)){
            wire.stubFor(post(urlEqualTo("/send")).willReturn(aResponse().withStatus(code)));var t=task();worker.runOnce();
            assertThat(row(t).getStatus()).isEqualTo(NotificationOutbox.Status.FAILED);assertThat(row(t).getAttemptCount()).isEqualTo(1);
        }
        assertThat(worker.runOnce()).isFalse();wire.verify(2,postRequestedFor(urlEqualTo("/send")));
    }
    @Test void authIOExceptionAndNetworkFailuresRemainRetryable(){
        var t=task();var w=new OutboxWorker(store,new OutboxFcmClient(wire.baseUrl()+"/send",()->{throw new IOException("fake auth error");},2000,5000),clock);
        w.runOnce();assertThat(row(t).getStatus()).isEqualTo(NotificationOutbox.Status.RETRY);assertThat(wire.getAllServeEvents()).isEmpty();
        clock.advance(2);wire.stubFor(post(urlEqualTo("/send")).willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));worker.runOnce();assertThat(row(t).getStatus()).isEqualTo(NotificationOutbox.Status.RETRY);
        clock.advance(4);wire.stubFor(post(urlEqualTo("/send")).willReturn(okJson("{}").withFixedDelay(500)));
        new OutboxWorker(store,new OutboxFcmClient(wire.baseUrl()+"/send",()->"fake",100,200),clock).runOnce();
        assertThat(row(t).getStatus()).isEqualTo(NotificationOutbox.Status.RETRY);
    }
    @Test void stopsAfterFiveAttempts(){
        wire.stubFor(post(urlEqualTo("/send")).willReturn(aResponse().withStatus(503)));var t=task();
        for(int i=0;i<5;i++){worker.runOnce();clock.advance(32);}
        assertThat(row(t).getAttemptCount()).isEqualTo(5);assertThat(row(t).getStatus()).isEqualTo(NotificationOutbox.Status.FAILED);
        assertThat(worker.runOnce()).isFalse();wire.verify(5,postRequestedFor(urlEqualTo("/send")));
    }
    @Test void retryBackoffIsExactlyTwoFourEightSixteenSeconds(){
        wire.stubFor(post(urlEqualTo("/send")).willReturn(aResponse().withStatus(500)));var t=task();
        for(long seconds:new long[]{2,4,8,16}){
            assertThat(worker.runOnce()).isTrue();
            assertThat(Duration.between(clock.instant(),row(t).getNextAttemptAt())).isEqualTo(Duration.ofSeconds(seconds));
            clock.advance(seconds-1);assertThat(worker.runOnce()).isFalse();clock.advance(1);
        }
        worker.runOnce();assertThat(row(t).getStatus()).isEqualTo(NotificationOutbox.Status.FAILED);
    }
    @Test void unauthorizedAndForbiddenAreRetryable(){
        for(int code:new int[]{401,403}){
            wire.stubFor(post(urlEqualTo("/send")).willReturn(aResponse().withStatus(code)));var t=task();worker.runOnce();
            assertThat(row(t).getStatus()).isEqualTo(NotificationOutbox.Status.RETRY);
            assertThat(row(t).getLastError()).isEqualTo("HTTP_"+code);
        }
        wire.verify(2,postRequestedFor(urlEqualTo("/send")));
    }
    @Test void expiredClaimRecoversAndIgnoresStaleCompletion(){
        var t=task();var first=store.claim().orElseThrow();assertThat(store.claim()).isEmpty();
        clock.advance(31);var restartedStore=new OutboxWorkerStore(repository,manager,clock);var second=restartedStore.claim().orElseThrow();
        assertThat(first.claimToken()).isNotEqualTo(second.claimToken());assertThat(restartedStore.complete(first,OutboxSender.Outcome.sent())).isFalse();
        assertThat(row(t).getStatus()).isEqualTo(NotificationOutbox.Status.PROCESSING);
        assertThat(restartedStore.complete(second,OutboxSender.Outcome.sent())).isTrue();assertThat(row(t).getStatus()).isEqualTo(NotificationOutbox.Status.SENT);
    }
    @Test void simultaneousWorkersSendOnce() throws Exception {
        var t=task();var executor=Executors.newFixedThreadPool(2);var barrier=new CyclicBarrier(2);
        try {
            var a=executor.submit(()->{barrier.await();return worker.runOnce();});var b=executor.submit(()->{barrier.await();return worker.runOnce();});a.get(5,TimeUnit.SECONDS);b.get(5,TimeUnit.SECONDS);
            assertThat(row(t).getStatus()).isEqualTo(NotificationOutbox.Status.SENT);wire.verify(1,postRequestedFor(urlEqualTo("/send")));
        } finally {executor.shutdownNow();}
    }
    @Test void exhaustedExpiredLeaseFailsWithoutSending(){
        var t=task();for(int i=0;i<5;i++){assertThat(store.claim()).isPresent();clock.advance(31);}
        assertThat(worker.runOnce()).isFalse();assertThat(row(t).getStatus()).isEqualTo(NotificationOutbox.Status.FAILED);assertThat(wire.getAllServeEvents()).isEmpty();
    }
    @Test void skippedTasksAreNeverSent(){
        var t=repository.saveAndFlush(NotificationOutbox.applicationResult(UUID.randomUUID(),42L,null,AplictStatus.FAIL,"title","body"));
        assertThat(worker.runOnce()).isFalse();assertThat(row(t).getStatus()).isEqualTo(NotificationOutbox.Status.SKIPPED);assertThat(wire.getAllServeEvents()).isEmpty();
    }
    static class MutableClock extends Clock {
        Instant now;MutableClock(Instant now){this.now=now.truncatedTo(java.time.temporal.ChronoUnit.MICROS);}void advance(long seconds){now=now.plusSeconds(seconds);}
        public ZoneId getZone(){return ZoneOffset.UTC;}public Clock withZone(ZoneId zone){return this;}public Instant instant(){return now;}
    }
}

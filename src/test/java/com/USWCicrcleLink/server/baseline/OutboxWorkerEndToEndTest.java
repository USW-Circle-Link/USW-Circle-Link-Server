package com.USWCicrcleLink.server.baseline;

import com.USWCicrcleLink.server.notification.outbox.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.*;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringJUnitConfig({BaselineReproductionTest.Config.class,PublicationOutboxWriter.class})
@WebAppConfiguration
@ActiveProfiles("outbox-test")
@TestPropertySource(properties={"spring.datasource.url=${BASELINE_MYSQL_URL}","spring.datasource.username=root",
    "spring.datasource.password=baseline-local-only","spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.open-in-view=false","spring.jpa.show-sql=false","spring.datasource.hikari.maximum-pool-size=10",
    "cors.allowed-origins=http://localhost","notification.publication.outbox-enabled=true"})
@EnabledIfEnvironmentVariable(named="BASELINE_MYSQL_URL",matches="jdbc:mysql://127\\.0\\.0\\.1:[0-9]+/baseline_outbox_week11.*")
class OutboxWorkerEndToEndTest {
    @Autowired ApplicationContext context;
    @Autowired NotificationOutboxRepository repository;
    @Autowired PlatformTransactionManager manager;
    @Test void publicationCommitsBeforeWorkerSendsAndRecordsAcceptance() throws Exception {
        var f=new BaselineReproductionTest();context.getAutowireCapableBeanFactory().autowireBean(f);
        try {
            f.setup();repository.deleteAllInBatch();var response=f.publish(f.a1,f.a2);
            assertThat(response.getResponse().getStatus()).isEqualTo(200);f.state("PASS/PASS","FAIL/FAIL",1);
            assertThat(f.wire.getAllServeEvents()).isEmpty();
            assertThat(repository.findAll()).hasSize(2).allSatisfy(t->assertThat(t.getStatus()).isEqualTo(NotificationOutbox.Status.PENDING));
            var clock=Clock.systemUTC();var store=new OutboxWorkerStore(repository,manager,clock);
            var worker=new OutboxWorker(store,new OutboxFcmClient(f.wire.baseUrl()+"/send",()->"fake-bearer",2000,5000),clock);
            long pendingAge=store.oldestUnfinishedAgeMillis();
            assertThat(worker.runOnce()).isTrue();assertThat(worker.runOnce()).isTrue();assertThat(worker.runOnce()).isFalse();
            var tasks=repository.findAll();assertThat(tasks).hasSize(2).allSatisfy(t->{assertThat(t.getStatus()).isEqualTo(NotificationOutbox.Status.SENT);assertThat(t.getAttemptCount()).isEqualTo(1);assertThat(t.getSentAt()).isNotNull();});
            f.wire.verify(1,postRequestedFor(urlEqualTo("/send")).withRequestBody(matchingJsonPath("$.message.token",equalTo("T1"))));
            f.wire.verify(1,postRequestedFor(urlEqualTo("/send")).withRequestBody(matchingJsonPath("$.message.token",equalTo("T2"))));
            assertThat(store.oldestUnfinishedAgeMillis()).isZero();
            var evidence=new LinkedHashMap<String,Object>();
            evidence.put("apiHttp",response.getResponse().getStatus());
            evidence.put("request",Map.of("method","POST","path",f.base()+"/applicants/notifications","body",List.of(Map.of("aplictUUID",f.a1.getAplictUUID().toString()),Map.of("aplictUUID",f.a2.getAplictUUID().toString()))));
            evidence.put("fcmCallsAtApiReturn",0);evidence.put("stateAtApiReturn","2 PENDING tasks, 1 member, 2 published");
            evidence.put("finalDb",f.snapshot());evidence.put("fcmCallsAfterWorker",f.wire.getAllServeEvents().size());
            evidence.put("oldestUnfinishedAgeBeforeMs",pendingAge);evidence.put("oldestUnfinishedAgeAfterMs",store.oldestUnfinishedAgeMillis());
            evidence.put("tasks",tasks.stream().map(t->Map.of("id",t.getId(),"state",t.getStatus().name(),"attempts",t.getAttemptCount(),"creationToSentRecordMs",Duration.between(t.getCreatedAt(),t.getSentAt()).toMillis())).toList());
            evidence.put("externalServices","WireMock FCM and fixed fake bearer only; no Google/Firebase/S3/mail");
            var path=Path.of(System.getenv().getOrDefault("BASELINE_EVIDENCE_DIR","evidence/week11/outbox-worker/manual"));Files.createDirectories(path);
            Files.writeString(path.resolve("WORKER-E2E.json"),f.json.writerWithDefaultPrettyPrinter().writeValueAsString(evidence));
        } finally {f.cleanup();}
    }
}

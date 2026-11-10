package com.USWCicrcleLink.server.baseline;

import com.USWCicrcleLink.server.club.application.domain.*;
import com.USWCicrcleLink.server.club.leader.domain.Leader;
import com.USWCicrcleLink.server.admin.domain.Admin;
import com.USWCicrcleLink.server.global.security.details.*;
import com.USWCicrcleLink.server.notification.outbox.*;
import com.USWCicrcleLink.server.user.profile.domain.Profile;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig({BaselineReproductionTest.Config.class, PublicationOutboxWriter.class})
@WebAppConfiguration
@ActiveProfiles("outbox-test")
@TestPropertySource(properties={
    "spring.datasource.url=${BASELINE_MYSQL_URL}","spring.datasource.username=root",
    "spring.datasource.password=baseline-local-only","spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.open-in-view=false","spring.jpa.show-sql=false","spring.datasource.hikari.maximum-pool-size=10",
    "cors.allowed-origins=http://localhost","notification.publication.outbox-enabled=true"
})
@EnabledIfEnvironmentVariable(named="BASELINE_MYSQL_URL",matches="jdbc:mysql://127\\.0\\.0\\.1:[0-9]+/baseline_outbox_week11.*")
class PublicationOutboxIntegrationTest {
    @Autowired ApplicationContext context;
    @Autowired NotificationOutboxRepository repository;
    @Autowired PublicationOutboxWriter writer;
    BaselineReproductionTest f;
    final Path evidence=Path.of(System.getenv().getOrDefault("BASELINE_EVIDENCE_DIR",
            "evidence/week11/publication-outbox/manual"));
    final Queue<Map<String,Object>> requests=new ConcurrentLinkedQueue<>();
    @BeforeEach void setup() throws Exception {
        f=new BaselineReproductionTest();context.getAutowireCapableBeanFactory().autowireBean(f);f.setup();
        Files.createDirectories(evidence);
        // Each scenario gets fresh applications; old task rows do not affect counts.
        repository.deleteAllInBatch();
    }
    @AfterEach void cleanup(){f.cleanup();}
    List<NotificationOutbox> tasks(){return repository.findAll();}
    MvcResult publish(Aplict... applicants) throws Exception {
        var response=f.publish(applicants);
        requests.add(Map.of("method","POST","path",f.base()+"/applicants/notifications",
                "body",Arrays.stream(applicants).map(a->Map.of("aplictUUID",a.getAplictUUID().toString())).toList(),
                "http",response.getResponse().getStatus(),
                "response",response.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8)));
        return response;
    }
    void record(String scenario,Map<String,Object> before,MvcResult... responses) throws Exception {
        var data=new LinkedHashMap<String,Object>();
        data.put("scenario",scenario);data.put("operator","AI automated test");
        data.put("before",before);data.put("after",f.snapshot());
        data.put("requests",List.copyOf(requests));requests.clear();
        data.put("responses",Arrays.stream(responses).map(r->{try{return Map.of("http",r.getResponse().getStatus(),"body",r.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8));}catch(Exception e){throw new RuntimeException(e);}}).toList());
        data.put("tasks",tasks().stream().map(t->{var row=new LinkedHashMap<String,Object>();row.put("applicationUuid",t.getApplicationUuid().toString());row.put("result",t.getResult().name());row.put("status",t.getStatus().name());row.put("lastError",t.getLastError());return row;}).toList());
        data.put("fcmCalls",f.wire.getAllServeEvents().size());
        Files.writeString(evidence.resolve(scenario+".json"),f.json.writerWithDefaultPrettyPrinter().writeValueAsString(data));
    }
    @Test void normalPassFailSnapshotsWithoutFcm() throws Exception {
        var before=f.snapshot();var response=publish(f.a1,f.a2);record("OUTBOX-01-normal",before,response);
        assertThat(response.getResponse().getStatus()).isEqualTo(200);f.state("PASS/PASS","FAIL/FAIL",1);
        assertThat(tasks()).hasSize(2).allSatisfy(t->{assertThat(t.getStatus()).isEqualTo(NotificationOutbox.Status.PENDING);assertThat(t.getTitle()).isEqualTo("동아리 지원 결과");});
        assertThat(tasks()).anySatisfy(t->{assertThat(t.getApplicationUuid()).isEqualTo(f.a1.getAplictUUID());assertThat(t.getBody()).isEqualTo(f.club.getClubName()+"에 합격했습니다.");});
        assertThat(f.wire.getAllServeEvents()).isEmpty();assertThat(f.fcm.authCalls.get()).isZero();
    }
    @Test void waitCreatesNothing() throws Exception {
        var before=f.snapshot();var response=publish(f.a3);record("OUTBOX-02-wait",before,response);
        assertThat(response.getResponse().getStatus()).isEqualTo(200);f.state("PASS/WAIT","FAIL/WAIT",0);
        assertThat(tasks()).isEmpty();assertThat(f.wire.getAllServeEvents()).isEmpty();
    }
    @Test void writerRequiresCallerTransaction(){
        assertThatThrownBy(()->writer.save(f.a1,AplictStatus.PASS))
            .isInstanceOf(org.springframework.transaction.IllegalTransactionStateException.class);
        assertThat(tasks()).isEmpty();
    }
    @Test void missingTokenStillPublishesAndAddsMember() throws Exception {
        f.tx.executeWithoutResult(s->f.em.find(Profile.class,f.a1.getProfile().getProfileId()).updateFcmToken(null));
        var before=f.snapshot();var response=publish(f.a1);record("OUTBOX-03-no-token",before,response);
        assertThat(response.getResponse().getStatus()).isEqualTo(200);f.state("PASS/PASS","FAIL/WAIT",1);
        assertThat(tasks()).singleElement().satisfies(t->{assertThat(t.getStatus()).isEqualTo(NotificationOutbox.Status.SKIPPED);assertThat(t.getLastError()).isEqualTo("NO_FCM_TOKEN");});
        assertThat(f.wire.getAllServeEvents()).isEmpty();
    }
    @Test void sequentialAndBodyDuplicatesDoNotCreateMoreTasks() throws Exception {
        var before=f.snapshot();var first=publish(f.a1,f.a1);var second=publish(f.a1);record("OUTBOX-04-repeat",before,first,second);
        assertThat(f.message(first)).isEqualTo("지원 결과 처리 완료");assertThat(f.message(second)).isEqualTo("이미 발표했습니다");
        f.state("PASS/PASS","FAIL/WAIT",1);assertThat(tasks()).hasSize(1);assertThat(f.wire.getAllServeEvents()).isEmpty();
    }
    @RepeatedTest(10) void concurrentPublication(RepetitionInfo info) throws Exception {
        var before=f.snapshot();var pool=Executors.newFixedThreadPool(10);var barrier=new CyclicBarrier(10);
        try {
            var futures=new ArrayList<Future<MvcResult>>();
            for(int i=0;i<10;i++)futures.add(pool.submit(()->{barrier.await(10,TimeUnit.SECONDS);return publish(f.a1);}));
            var responses=new ArrayList<MvcResult>();for(var future:futures)responses.add(future.get(30,TimeUnit.SECONDS));
            record("OUTBOX-05-concurrent-"+info.getCurrentRepetition(),before,responses.toArray(MvcResult[]::new));
            var messages=new ArrayList<String>();for(var response:responses){assertThat(response.getResponse().getStatus()).isEqualTo(200);messages.add(f.message(response));}
            assertThat(Collections.frequency(messages,"지원 결과 처리 완료")).isEqualTo(1);
            assertThat(Collections.frequency(messages,"이미 발표했습니다")).isEqualTo(9);
            f.state("PASS/PASS","FAIL/WAIT",1);assertThat(tasks()).hasSize(1);assertThat(f.wire.getAllServeEvents()).isEmpty();
        } finally {pool.shutdownNow();assertThat(pool.awaitTermination(35,TimeUnit.SECONDS)).isTrue();}
    }
    @Test void secondTaskUniqueFailureRollsBackFirstTaskResultAndMember() throws Exception {
        // Pre-existing task forces a real MySQL unique failure only at the second applicant.
        repository.saveAndFlush(NotificationOutbox.applicationResult(f.a2.getAplictUUID(),f.a2.getProfile().getProfileId(),"T2",AplictStatus.FAIL,"동아리 지원 결과","기존 작업"));
        var before=f.snapshot();var response=publish(f.a1,f.a2);record("OUTBOX-06-rollback",before,response);
        assertThat(response.getResponse().getStatus()).isEqualTo(500);f.state("PASS/WAIT","FAIL/WAIT",0);
        assertThat(tasks()).singleElement().satisfies(t->assertThat(t.getApplicationUuid()).isEqualTo(f.a2.getAplictUUID()));
        assertThat(f.wire.getAllServeEvents()).isEmpty();
    }
    @Test void missingAndForeignApplicationsRejectWholeRequest() throws Exception {
        for(var invalid:List.of(Aplict.builder().aplictUUID(UUID.randomUUID()).build(),f.a4)){
            var before=f.snapshot();var response=publish(f.a1,invalid);
            record(invalid==f.a4?"OUTBOX-07-foreign":"OUTBOX-07-missing",before,response);
            assertThat(response.getResponse().getStatus()).isEqualTo(404);f.state("PASS/WAIT","FAIL/WAIT",0);
            assertThat(tasks()).isEmpty();assertThat(f.wire.getAllServeEvents()).isEmpty();
        }
    }
    @Test void unauthorizedUserAndOtherLeaderCannotChangeAnything() throws Exception {
        for(Object principal:List.of(new CustomUserDetails(f.user,List.of()),new CustomLeaderDetails(mock(Leader.class),f.other.getClubuuid()))){
            f.principal=principal;var before=f.snapshot();var response=publish(f.a1);
            record(principal instanceof CustomUserDetails?"OUTBOX-08-user":"OUTBOX-08-other-leader",before,response);
            assertThat(response.getResponse().getStatus()).isEqualTo(403);f.state("PASS/WAIT","FAIL/WAIT",0);
            assertThat(tasks()).isEmpty();assertThat(f.wire.getAllServeEvents()).isEmpty();
        }
    }
    @Test void administratorRetainsAccess() throws Exception {
        f.principal=new CustomAdminDetails(mock(Admin.class));var before=f.snapshot();var response=publish(f.a1);record("OUTBOX-09-admin",before,response);
        assertThat(response.getResponse().getStatus()).isEqualTo(200);f.state("PASS/PASS","FAIL/WAIT",1);assertThat(tasks()).hasSize(1);assertThat(f.wire.getAllServeEvents()).isEmpty();
    }
}

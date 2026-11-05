package com.USWCicrcleLink.server.baseline;

import com.USWCicrcleLink.server.admin.domain.Admin;
import com.USWCicrcleLink.server.club.api.ClubController;
import com.USWCicrcleLink.server.club.application.api.AplictController;
import com.USWCicrcleLink.server.club.application.domain.*;
import com.USWCicrcleLink.server.club.application.repository.*;
import com.USWCicrcleLink.server.club.application.service.AplictService;
import com.USWCicrcleLink.server.club.domain.*;
import com.USWCicrcleLink.server.club.leader.domain.*;
import com.USWCicrcleLink.server.club.leader.service.*;
import com.USWCicrcleLink.server.club.repository.*;
import com.USWCicrcleLink.server.global.exception.GlobalExceptionHandler;
import com.USWCicrcleLink.server.global.security.details.*;
import com.USWCicrcleLink.server.global.security.jwt.domain.Role;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.profile.domain.*;
import com.USWCicrcleLink.server.user.profile.domain.Profile;
import com.USWCicrcleLink.server.user.profile.repository.ProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Fault;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/** Opt-in: disposable MySQL only. No application component scan or external credentials. */
@SpringJUnitConfig(BaselineReproductionTest.Config.class)
@org.springframework.test.context.web.WebAppConfiguration
@TestPropertySource(properties = {
    "spring.datasource.url=${BASELINE_MYSQL_URL}",
    "spring.datasource.username=root", "spring.datasource.password=baseline-local-only", "fcm.api-url=http://127.0.0.1:1/blocked",
    "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.open-in-view=false", "cors.allowed-origins=http://localhost",
    "spring.datasource.hikari.maximum-pool-size=10", "spring.jpa.show-sql=false"
})
@EnabledIfEnvironmentVariable(named="BASELINE_MYSQL_URL", matches="jdbc:mysql://127\\.0\\.0\\.1:.*baseline.*")
class BaselineReproductionTest {
    @Configuration
    @org.springframework.web.servlet.config.annotation.EnableWebMvc
    @EnableTransactionManagement(proxyTargetClass=true)
    @ImportAutoConfiguration({DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
    @EntityScan("com.USWCicrcleLink.server")
    @EnableJpaRepositories("com.USWCicrcleLink.server")
    @Import({com.USWCicrcleLink.server.global.security.config.SecurityConfig.class,
        com.USWCicrcleLink.server.global.security.exception.CustomAuthenticationEntryPoint.class,
        com.USWCicrcleLink.server.global.security.exception.CustomAccessDeniedHandler.class})
    static class Config {
        @Bean ObjectMapper objectMapper(){return new ObjectMapper();}
        @Bean com.USWCicrcleLink.server.global.security.jwt.JwtProvider jwt(){return mock(com.USWCicrcleLink.server.global.security.jwt.JwtProvider.class);}
        @Bean com.USWCicrcleLink.server.global.security.jwt.SecurityProperties securityProperties(){
            var p=new com.USWCicrcleLink.server.global.security.jwt.SecurityProperties();p.setPermitAllPaths(List.of());p.setLoggingPaths(List.of());p.setMethods(List.of());return p;
        }
        @Bean com.USWCicrcleLink.server.global.s3File.Service.S3FileUploadService s3() {
            return mock(com.USWCicrcleLink.server.global.s3File.Service.S3FileUploadService.class);
        }
        @Bean(destroyMethod="stop") WireMockServer wire() {
            WireMockServer w = new WireMockServer(options().dynamicPort()); w.start(); return w;
        }
        @Bean TestFcm fcm(ProfileRepository profiles, WireMockServer wire) {
            TestFcm f = new TestFcm(profiles);
            ReflectionTestUtils.setField(f, "fcmApiUrl", wire.baseUrl()+"/send"); return f;
        }
        @Bean ClubLeaderService leader(ApplicationContext c, TestFcm f) throws Exception {
            return construct(ClubLeaderService.class,c,Map.of(FcmServiceImpl.class,f));
        }
        @Bean AplictService applications(ApplicationContext c) throws Exception {
            return construct(AplictService.class,c,Map.of());
        }
    }
    static <T> T construct(Class<T> type, ApplicationContext c, Map<Class<?>,Object> overrides) throws Exception {
        var ctor=type.getConstructors()[0]; Object[] args=new Object[ctor.getParameterCount()];
        for(int i=0;i<args.length;i++) {
            Class<?> p=ctor.getParameterTypes()[i];
            if(overrides.containsKey(p)) args[i]=overrides.get(p);
            else { var beans=c.getBeansOfType(p); args[i]=beans.isEmpty()?mock(p):beans.values().iterator().next(); }
        }
        return type.cast(ctor.newInstance(args));
    }
    static class TestFcm extends FcmServiceImpl {
        AtomicInteger authCalls=new AtomicInteger(); volatile int failAuthAt=-1; volatile int readTimeout=0;
        volatile CountDownLatch reached,release;
        TestFcm(ProfileRepository profiles){super(profiles);}
        @Override protected String getAccessToken() throws IOException {
            if(authCalls.incrementAndGet()==failAuthAt) throw new IOException("injected-auth-io");
            return "fake-local-token";
        }
        @Override protected RestTemplate createRestTemplate() {
            RestTemplate transport;
            if(readTimeout==0) transport=super.createRestTemplate();
            else {var factory=new SimpleClientHttpRequestFactory();factory.setReadTimeout(readTimeout);transport=new RestTemplate(factory);}
            if(reached!=null)transport.getInterceptors().add((request,body,execution)->{
                var response=execution.execute(request,body);reached.countDown();
                try {if(!release.await(15,TimeUnit.SECONDS))throw new IOException("gate timeout");}
                catch(InterruptedException e){Thread.currentThread().interrupt();throw new IOException(e);}
                return response;
            });
            return transport;
        }
    }
    @Autowired ApplicationContext context;
    @Autowired ClubLeaderService leader;
    @Autowired AplictService applications;
    @Autowired TestFcm fcm;
    @Autowired WireMockServer wire;
    @Autowired PlatformTransactionManager manager;
    @PersistenceContext EntityManager em;
    TransactionTemplate tx; MockMvc mvc; Club club,other; Aplict a1,a2,a3,a4; User user;
    Object principal; ObjectMapper json=new ObjectMapper();
    final Path evidence=Path.of("evidence/week10/publication-response");

    @BeforeEach void setup() throws Exception {
        fcm=org.springframework.test.util.AopTestUtils.getTargetObject(fcm);
        ReflectionTestUtils.setField(fcm,"fcmApiUrl",wire.baseUrl()+"/send");
        tx=new TransactionTemplate(manager); wire.resetAll(); fcm.authCalls.set(0); fcm.failAuthAt=-1; fcm.readTimeout=0;fcm.reached=null;fcm.release=null;
        wire.stubFor(post(urlEqualTo("/send")).willReturn(okJson("{\"name\":\"local-message\"}").withFixedDelay(200)));
        String suffix=UUID.randomUUID().toString().substring(0,6);
        tx.executeWithoutResult(s->{
            club=club("A"+suffix); other=club("B"+suffix);
            user=User.builder().userAccount("u"+suffix).email(suffix+"@test.invalid").userPw("fake").role(Role.USER).build(); em.persist(user);
            a1=app(club,AplictStatus.PASS,user,"T1"); a2=app(club,AplictStatus.FAIL,null,"T2");
            a3=app(club,AplictStatus.WAIT,null,"T3"); a4=app(other,AplictStatus.PASS,null,"T4");
        });
        principal=new CustomLeaderDetails(mock(Leader.class),club.getClubuuid());
        Map<Class<?>,Object> overrides=Map.of(ClubLeaderService.class,leader,AplictService.class,applications,FcmServiceImpl.class,fcm);
        mvc=MockMvcBuilders.standaloneSetup(construct(ClubController.class,context,overrides),construct(AplictController.class,context,overrides))
            .setControllerAdvice(new GlobalExceptionHandler()).build();
        Files.createDirectories(evidence);
    }
    Club club(String name){var c=Club.builder().clubName(name).leaderName("tester").leaderHp("01000000000").department(Department.ACADEMIC).clubRoomNumber("100").build();em.persist(c);return c;}
    Aplict app(Club c,AplictStatus status,User u,String token){
        var p=Profile.builder().user(u).userName("tester").studentNumber("12345678").userHp("01000000000").major("test")
            .memberType(MemberType.REGULARMEMBER).profileCreatedAt(LocalDateTime.now()).profileUpdatedAt(LocalDateTime.now()).fcmToken(token).build();
        em.persist(p);var a=Aplict.builder().club(c).profile(p).submittedAt(LocalDateTime.now()).privateStatus(status).build();em.persist(a);return a;
    }
    @AfterEach void cleanup(){ SecurityContextHolder.clearContext(); }
    MvcResult call(String method,String path,String body) throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal,null,List.of()));
        try {return mvc.perform(request(org.springframework.http.HttpMethod.valueOf(method),path).contentType("application/json").content(body)).andReturn();}
        finally{SecurityContextHolder.clearContext();}
    }
    String base(){return "/clubs/"+club.getClubuuid();}
    MvcResult publish(Aplict... as) throws Exception {
        return call("POST",base()+"/applicants/notifications",json.writeValueAsString(Arrays.stream(as).map(a->Map.of("aplictUUID",a.getAplictUUID())).toList()));
    }
    MvcResult patch(String status) throws Exception {return call("PATCH",base()+"/applications/"+a1.getAplictUUID()+"/status","{\"status\":\""+status+"\"}");}
    Map<String,Object> snapshot(){return tx.execute(s->{
        var one=em.find(Aplict.class,a1.getAplictId());var two=em.find(Aplict.class,a2.getAplictId());
        long members=em.createQuery("select count(m) from ClubMembers m where m.club.clubId=:id",Long.class).setParameter("id",club.getClubId()).getSingleResult();
        return Map.of("a1",one.getPrivateStatus()+"/"+one.getPublicStatus(),"a2",two.getPrivateStatus()+"/"+two.getPublicStatus(),"members",members,
            "token1",Objects.toString(one.getProfile().getFcmToken(),"NULL"),"token2",Objects.toString(two.getProfile().getFcmToken(),"NULL"));
    });}
    void record(String id,MvcResult... results) throws Exception {
        var data=new LinkedHashMap<String,Object>(); data.put("scenario",id);data.put("snapshot",snapshot());
        data.put("baselineCommit","794306c2b48c5d3e92a8a8018b2701e32a4c14dd");data.put("recordedAt",java.time.OffsetDateTime.now().toString());
        data.put("responses",Arrays.stream(results).map(r->Map.of("http",r.getResponse().getStatus(),"body",new String(r.getResponse().getContentAsByteArray(),java.nio.charset.StandardCharsets.UTF_8))).toList());
        data.put("fcm",wire.getAllServeEvents().stream().map(e->Map.of("body",e.getRequest().getBodyAsString(),"status",e.getResponse().getStatus(),"time",e.getRequest().getLoggedDate().toString())).toList());
        Files.writeString(evidence.resolve(id+".json"),json.writerWithDefaultPrettyPrinter().writeValueAsString(data));
    }
    void state(String one,String two,long members){assertThat(snapshot()).containsEntry("a1",one).containsEntry("a2",two).containsEntry("members",members);}
    @Test void PUB_01_normal() throws Exception {var r=publish(a1,a2);record("PUB-01",r);assertThat(r.getResponse().getStatus()).isEqualTo(200);state("PASS/PASS","FAIL/FAIL",1);wire.verify(2,postRequestedFor(urlEqualTo("/send")));}
    @Test void PUB_02_wait() throws Exception {var r=publish(a3);record("PUB-02",r);state("PASS/WAIT","FAIL/WAIT",0);assertThat(wire.getAllServeEvents()).isEmpty();}
    @Test void PUB_03_noToken() throws Exception {tx.executeWithoutResult(s->em.find(Profile.class,a1.getProfile().getProfileId()).updateFcmToken(null));var r=publish(a1);record("PUB-03",r);state("PASS/PASS","FAIL/WAIT",1);assertThat(wire.getAllServeEvents()).isEmpty();}
    @Test void PUB_04_repeat() throws Exception {var r=publish(a1);var r2=publish(a1);record("PUB-04",r,r2);state("PASS/PASS","FAIL/WAIT",1);assertThat(r.getResponse().getStatus()).isEqualTo(200);assertThat(r2.getResponse().getStatus()).isEqualTo(200);assertThat(message(r)).isEqualTo("지원 결과 처리 완료");assertThat(message(r2)).isEqualTo("이미 발표했습니다");assertThat(wire.getAllServeEvents()).hasSize(1);}
    @Test void PUB_05_duplicateBody() throws Exception {var r=publish(a1,a1);record("PUB-05",r);state("PASS/PASS","FAIL/WAIT",1);assertThat(r.getResponse().getStatus()).isEqualTo(200);assertThat(wire.getAllServeEvents()).hasSize(1);}
    @Test void PUB_MEMBER_existing() throws Exception {tx.executeWithoutResult(s->em.persist(ClubMembers.builder().club(em.find(Club.class,club.getClubId())).profile(em.find(Profile.class,a1.getProfile().getProfileId())).build()));var r=publish(a1);record("PUB-MEMBER",r);state("PASS/PASS","FAIL/WAIT",1);}
    @ParameterizedTest @ValueSource(ints={400,404,429,500}) void ERR_http(int code) throws Exception {
        String fcmCode=switch(code){case 404->"UNREGISTERED";case 429->"QUOTA_EXCEEDED";case 500->"INTERNAL";default->"INVALID_ARGUMENT";};
        String status=code==404?"NOT_FOUND":code==429?"RESOURCE_EXHAUSTED":code==500?"INTERNAL":"INVALID_ARGUMENT";
        wire.stubFor(post(urlEqualTo("/send")).withRequestBody(matchingJsonPath("$.message.token",equalTo("T2")))
            .willReturn(aResponse().withStatus(code).withHeader("Retry-After","2").withHeader("Content-Type","application/json").withBody("{\"error\":{\"status\":\""+status+"\",\"details\":[{\"@type\":\"type.googleapis.com/google.firebase.fcm.v1.FcmError\",\"errorCode\":\""+fcmCode+"\"}]}}")));
        var r=publish(a1,a2);record("ERR-"+code,r);assertThat(r.getResponse().getStatus()).isEqualTo(500);state("PASS/WAIT","FAIL/WAIT",0);assertThat(wire.getAllServeEvents()).hasSize(2);
        assertThat(snapshot()).containsEntry("token1","T1").containsEntry("token2","T2");
        if(code==429){Thread.sleep(2100);assertThat(wire.getAllServeEvents()).hasSize(2);}
    }
    @Test void ERR_disconnect() throws Exception {wire.stubFor(post(urlEqualTo("/send")).withRequestBody(containing("T2")).willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));var r=publish(a1,a2);record("ERR-DISCONNECT",r);assertThat(r.getResponse().getStatus()).isEqualTo(500);state("PASS/WAIT","FAIL/WAIT",0);}
    @Test void ERR_io() throws Exception {fcm.failAuthAt=2;var r=publish(a1,a2);record("ERR-IO",r);assertThat(r.getResponse().getStatus()).isEqualTo(200);state("PASS/PASS","FAIL/FAIL",1);assertThat(wire.getAllServeEvents()).hasSize(1);}
    @Test void ERR_delay() throws Exception {wire.stubFor(post(urlEqualTo("/send")).withRequestBody(containing("T2")).willReturn(okJson("{}").withFixedDelay(3000)));long start=System.nanoTime();var r=publish(a1,a2);record("ERR-DELAY",r);assertThat(System.nanoTime()-start).isGreaterThan(3_000_000_000L);state("PASS/PASS","FAIL/FAIL",1);}
    @Test void ERR_readTimeout() throws Exception {fcm.readTimeout=1000;wire.stubFor(post(urlEqualTo("/send")).withRequestBody(containing("T2")).willReturn(okJson("{}").withFixedDelay(3000)));var r=publish(a1,a2);assertThat(r.getResponse().getStatus()).isEqualTo(500);state("PASS/WAIT","FAIL/WAIT",0);Thread.sleep(3100);record("ERR-READ-TIMEOUT",r);}
    @Test void PUB_09_invalidAfterSend() throws Exception {var missing=Aplict.builder().aplictUUID(UUID.randomUUID()).build();var r=publish(a1,missing);record("PUB-09A",r);assertThat(r.getResponse().getStatus()).isEqualTo(404);state("PASS/WAIT","FAIL/WAIT",0);assertThat(wire.getAllServeEvents()).hasSize(1);}
    @Test void PUB_09_invalidFirst() throws Exception {var missing=Aplict.builder().aplictUUID(UUID.randomUUID()).build();var r=publish(missing,a1);record("PUB-09B",r);assertThat(r.getResponse().getStatus()).isEqualTo(404);assertThat(wire.getAllServeEvents()).isEmpty();}
    @Test void PUB_09_foreignClub() throws Exception {var r=publish(a1,a4);record("PUB-09C",r);assertThat(r.getResponse().getStatus()).isEqualTo(404);state("PASS/WAIT","FAIL/WAIT",0);assertThat(wire.getAllServeEvents()).hasSize(1);}
    @Test void RACE_01_patchFirst() throws Exception {var p=patch("FAIL");var r=publish(a1);record("RACE-01",p,r);state("FAIL/FAIL","FAIL/WAIT",0);}
    @Test void RACE_02_publishFirst() throws Exception {var r=publish(a1);var p=patch("FAIL");record("RACE-02",r,p);state("FAIL/PASS","FAIL/WAIT",1);}
    @RepeatedTest(10) void RACE_03_gated(RepetitionInfo info) throws Exception {
        fcm.reached=new CountDownLatch(1);fcm.release=new CountDownLatch(1);var patchStarted=new CountDownLatch(1);var pool=Executors.newFixedThreadPool(2);
        try {var published=pool.submit(()->publish(a1));assertThat(fcm.reached.await(10,TimeUnit.SECONDS)).isTrue();
            var patched=pool.submit(()->{patchStarted.countDown();return patch("FAIL");});assertThat(patchStarted.await(10,TimeUnit.SECONDS)).isTrue();
            fcm.release.countDown();var r=published.get(30,TimeUnit.SECONDS);var p=patched.get(30,TimeUnit.SECONDS);record("RACE-03-"+info.getCurrentRepetition(),r,p);
            assertThat(r.getResponse().getStatus()).isEqualTo(200);assertThat(p.getResponse().getStatus()).isEqualTo(200);
        }finally{fcm.release.countDown();pool.shutdown();assertThat(pool.awaitTermination(35,TimeUnit.SECONDS)).isTrue();}
    }
    @Test void ERR_clientTimeout() throws Exception {
        wire.stubFor(post(urlEqualTo("/send")).withRequestBody(containing("T2")).willReturn(okJson("{}").withFixedDelay(3000)));
        var server=com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress("127.0.0.1",0),0);var pool=Executors.newSingleThreadExecutor();server.setExecutor(pool);
        var complete=new CountDownLatch(1);var result=new java.util.concurrent.atomic.AtomicReference<MvcResult>();var error=new java.util.concurrent.atomic.AtomicReference<Throwable>();
        server.createContext(base()+"/applicants/notifications",exchange->{try{
            var r=call("POST",exchange.getRequestURI().getPath(),new String(exchange.getRequestBody().readAllBytes(),java.nio.charset.StandardCharsets.UTF_8));result.set(r);
            exchange.sendResponseHeaders(r.getResponse().getStatus(),0);exchange.getResponseBody().write(r.getResponse().getContentAsByteArray());
        }catch(Exception e){error.set(e);}finally{exchange.close();complete.countDown();}});server.start();
        try {var client=java.net.http.HttpClient.newHttpClient();var req=java.net.http.HttpRequest.newBuilder(java.net.URI.create("http://127.0.0.1:"+server.getAddress().getPort()+base()+"/applicants/notifications"))
            .timeout(java.time.Duration.ofMillis(1000)).POST(java.net.http.HttpRequest.BodyPublishers.ofString("[{\"aplictUUID\":\""+a1.getAplictUUID()+"\"},{\"aplictUUID\":\""+a2.getAplictUUID()+"\"}]")).build();
            assertThatThrownBy(()->client.send(req,java.net.http.HttpResponse.BodyHandlers.ofString())).isInstanceOf(java.net.http.HttpTimeoutException.class);
            assertThat(complete.await(15,TimeUnit.SECONDS)).isTrue();assertThat(result.get()).isNotNull();record("ERR-CLIENT-TIMEOUT",result.get());state("PASS/PASS","FAIL/FAIL",1);
        }finally{server.stop(0);pool.shutdown();assertThat(pool.awaitTermination(15,TimeUnit.SECONDS)).isTrue();}
    }
    @RepeatedTest(10) void RACE_04_simultaneous(RepetitionInfo info) throws Exception {
        var pool=Executors.newFixedThreadPool(2);var barrier=new CyclicBarrier(2);
        try {var p=pool.submit(()->{barrier.await(10,TimeUnit.SECONDS);return patch("FAIL");});var r=pool.submit(()->{barrier.await(10,TimeUnit.SECONDS);return publish(a1);});
            record("RACE-04-"+info.getCurrentRepetition(),p.get(30,TimeUnit.SECONDS),r.get(30,TimeUnit.SECONDS));
        } finally {pool.shutdown();assertThat(pool.awaitTermination(35,TimeUnit.SECONDS)).isTrue();}
    }
    @RepeatedTest(10) void PUB_06_concurrent(RepetitionInfo info) throws Exception {
        var pool=Executors.newFixedThreadPool(10);var barrier=new CyclicBarrier(10);var futures=new ArrayList<Future<MvcResult>>();
        try {for(int i=0;i<10;i++)futures.add(pool.submit(()->{barrier.await(10,TimeUnit.SECONDS);return publish(a1);}));
            var results=new ArrayList<MvcResult>();for(var f:futures)results.add(f.get(30,TimeUnit.SECONDS));record("PUB-06-"+info.getCurrentRepetition(),results.toArray(MvcResult[]::new));
            state("PASS/PASS","FAIL/WAIT",1);
            assertThat(results).allSatisfy(r->assertThat(r.getResponse().getStatus()).isEqualTo(200));
            var messages=new ArrayList<String>();for(var result:results)messages.add(message(result));
            assertThat(Collections.frequency(messages,"지원 결과 처리 완료")).isEqualTo(1);
            assertThat(Collections.frequency(messages,"이미 발표했습니다")).isEqualTo(9);
            assertThat(wire.getAllServeEvents()).hasSize(1);
        } finally {pool.shutdown();assertThat(pool.awaitTermination(35,TimeUnit.SECONDS)).isTrue();}
    }
    String message(MvcResult result) throws Exception {
        return json.readTree(new String(result.getResponse().getContentAsByteArray(),java.nio.charset.StandardCharsets.UTF_8)).get("message").asText();
    }
    @Test void publicationResponseHandlesMixedAndWait() throws Exception {
        assertThat(message(publish(a1))).isEqualTo("지원 결과 처리 완료");
        assertThat(message(publish(a1,a2))).isEqualTo("지원 결과 처리 완료");
        assertThat(message(publish(a1,a2))).isEqualTo("이미 발표했습니다");
        assertThat(message(publish(a3))).isEqualTo("지원 결과 처리 완료");
        assertThat(message(publish(a1,a3))).isEqualTo("지원 결과 처리 완료");
        assertThat(message(publish())).isEqualTo("지원 결과 처리 완료");
        assertThat(wire.getAllServeEvents()).hasSize(2);
    }
    @Test void memberUniqueConstraintRejectsDuplicate() throws Exception {
        tx.executeWithoutResult(s->em.persist(ClubMembers.builder().club(em.find(Club.class,club.getClubId())).profile(em.find(Profile.class,a1.getProfile().getProfileId())).build()));
        assertThatThrownBy(()->tx.executeWithoutResult(s->{
            em.persist(ClubMembers.builder().club(em.find(Club.class,club.getClubId())).profile(em.find(Profile.class,a1.getProfile().getProfileId())).build());em.flush();
        })).isInstanceOf(RuntimeException.class).hasStackTraceContaining("uk_club_member");
        assertThat(snapshot().get("members")).isEqualTo(1L);
    }
    @Test void publicationBatchesInOppositeOrderDoNotDuplicate() throws Exception {
        var pool=Executors.newFixedThreadPool(2);var barrier=new CyclicBarrier(2);
        try {
            var first=pool.submit(()->{barrier.await(10,TimeUnit.SECONDS);return publish(a1,a2);});
            var second=pool.submit(()->{barrier.await(10,TimeUnit.SECONDS);return publish(a2,a1);});
            var r1=first.get(30,TimeUnit.SECONDS);var r2=second.get(30,TimeUnit.SECONDS);
            record("PUB-BATCH-REVERSED",r1,r2);
            assertThat(r1.getResponse().getStatus()).isEqualTo(200);assertThat(r2.getResponse().getStatus()).isEqualTo(200);
            state("PASS/PASS","FAIL/FAIL",1);assertThat(wire.getAllServeEvents()).hasSize(2);
        } finally {pool.shutdown();assertThat(pool.awaitTermination(35,TimeUnit.SECONDS)).isTrue();}
    }
    @Test void concurrentFailPublicationSendsOnce() throws Exception {
        var pool=Executors.newFixedThreadPool(10);var barrier=new CyclicBarrier(10);var futures=new ArrayList<Future<MvcResult>>();
        try {
            for(int i=0;i<10;i++)futures.add(pool.submit(()->{barrier.await(10,TimeUnit.SECONDS);return publish(a2);}));
            var results=new ArrayList<MvcResult>();for(var future:futures)results.add(future.get(30,TimeUnit.SECONDS));
            record("PUB-FAIL-CONCURRENT",results.toArray(MvcResult[]::new));
            assertThat(results).allSatisfy(r->assertThat(r.getResponse().getStatus()).isEqualTo(200));
            state("PASS/WAIT","FAIL/FAIL",0);assertThat(wire.getAllServeEvents()).hasSize(1);
        } finally {pool.shutdown();assertThat(pool.awaitTermination(35,TimeUnit.SECONDS)).isTrue();}
    }
    @Test void membershipUniqueAllowsDifferentClubs() {
        tx.executeWithoutResult(s->{
            var profile=em.find(Profile.class,a1.getProfile().getProfileId());
            em.persist(ClubMembers.builder().club(em.find(Club.class,club.getClubId())).profile(profile).build());
            em.persist(ClubMembers.builder().club(em.find(Club.class,other.getClubId())).profile(profile).build());
            em.flush();
        });
        assertThat(snapshot().get("members")).isEqualTo(1L);
    }
    @Test void LIST_filters() throws Exception {
        tx.executeWithoutResult(s->{em.createQuery("delete from Aplict a where a.club.clubId=:id").setParameter("id",club.getClubId()).executeUpdate();
            for(var priv:AplictStatus.values())for(var pub:AplictStatus.values()){var a=app(em.find(Club.class,club.getClubId()),pub,null,"L");a.publishResults();a.updatePrivateStatus(priv);}});
        Map<String,Integer> cases=Map.of("",9,"?status=PASS",3,"?isResultPublished=true",6,"?isResultPublished=false",3,"?status=PASS&isResultPublished=true",2,"?status=PASS&isResultPublished=false",1);
        for(var e:cases.entrySet()){var r=call("GET",base()+"/applicants"+e.getKey(),"");assertThat(json.readTree(r.getResponse().getContentAsString()).get("data").size()).isEqualTo(e.getValue());}
    }
    @Test void AUTH_serviceScope() throws Exception {principal=new CustomLeaderDetails(mock(Leader.class),other.getClubuuid());var r=publish(a1);record("AUTH-03",r);assertThat(r.getResponse().getStatus()).isEqualTo(403);assertThat(wire.getAllServeEvents()).isEmpty();principal=new CustomAdminDetails(mock(Admin.class));assertThat(publish(a1).getResponse().getStatus()).isEqualTo(200);}

    String prepareSubmission() throws Exception {
        principal=new CustomUserDetails(user,List.of());
        return tx.execute(s->{
            em.remove(em.find(Aplict.class,a1.getAplictId()));em.flush();
            var form=ClubForm.builder().club(em.find(Club.class,club.getClubId())).createdBy(1L).description("test").build();
            var text=FormQuestion.builder().type(QuestionType.LONG_TEXT).content("why").build();
            var single=FormQuestion.builder().type(QuestionType.RADIO).content("one").build();
            var multi=FormQuestion.builder().type(QuestionType.CHECKBOX).content("many").build();
            var opt1=FormQuestionOption.builder().content("one").build();single.addOption(opt1);
            var opt2=FormQuestionOption.builder().content("two").build();var opt3=FormQuestionOption.builder().content("three").build();multi.addOption(opt2);multi.addOption(opt3);
            form.addQuestion(text);form.addQuestion(single);form.addQuestion(multi);em.persist(form);em.flush();
            return "{\"qnaList\":[{\"questionId\":"+text.getQuestionId()+",\"answerText\":\"test\"},{\"questionId\":"+single.getQuestionId()+",\"optionId\":"+opt1.getOptionId()+"},{\"questionId\":"+multi.getQuestionId()+",\"optionIds\":["+opt2.getOptionId()+","+opt3.getOptionId()+"]}]}";
        });
    }
    MvcResult submit(String body) throws Exception {return call("POST",base()+"/applications",body);}
    long applicationCount(){return tx.execute(s->em.createQuery("select count(a) from Aplict a where a.profile.profileId=:id and a.club.clubId=:club",Long.class).setParameter("id",a1.getProfile().getProfileId()).setParameter("club",club.getClubId()).getSingleResult());}
    long answerCount(){return tx.execute(s->em.createQuery("select count(x) from AplictAnswer x where x.aplict.profile.profileId=:id and x.aplict.club.clubId=:club",Long.class).setParameter("id",a1.getProfile().getProfileId()).setParameter("club",club.getClubId()).getSingleResult());}
    void applyRecord(String id,MvcResult... results) throws Exception {
        var data=Map.of("scenario",id,"applications",applicationCount(),"answers",answerCount(),"responses",Arrays.stream(results).map(r->Map.of("http",r.getResponse().getStatus(),"body",new String(r.getResponse().getContentAsByteArray(),java.nio.charset.StandardCharsets.UTF_8))).toList());
        Files.writeString(evidence.resolve(id+".json"),json.writerWithDefaultPrettyPrinter().writeValueAsString(data));
    }
    @Test void APPLY_01_02_submitRepeat() throws Exception {String body=prepareSubmission();var first=submit(body);assertThat(first.getResponse().getStatus()).isEqualTo(200);assertThat(applicationCount()).isEqualTo(1);assertThat(answerCount()).isEqualTo(4);var second=submit(body);applyRecord("APPLY-01-02",first,second);assertThat(second.getResponse().getStatus()).isEqualTo(400);assertThat(answerCount()).isEqualTo(4);}
    @Test void APPLY_03_concurrent() throws Exception {
        String body=prepareSubmission();var pool=Executors.newFixedThreadPool(10);var barrier=new CyclicBarrier(10);var futures=new ArrayList<Future<MvcResult>>();
        try {for(int i=0;i<10;i++)futures.add(pool.submit(()->{barrier.await(10,TimeUnit.SECONDS);return submit(body);}));var results=new ArrayList<MvcResult>();for(var f:futures)results.add(f.get(30,TimeUnit.SECONDS));applyRecord("APPLY-03",results.toArray(MvcResult[]::new));assertThat(applicationCount()).isEqualTo(1);assertThat(answerCount()).isEqualTo(4);
            String ddl=tx.execute(s->Objects.toString(((Object[])em.createNativeQuery("SHOW CREATE TABLE aplict_table").getSingleResult())[1]));Files.writeString(evidence.resolve("mysql-aplict-ddl.txt"),ddl);assertThat(ddl).contains("uk_aplict_active");
        }finally{pool.shutdown();assertThat(pool.awaitTermination(35,TimeUnit.SECONDS)).isTrue();}
    }
    @Test void APPLY_05_validation() throws Exception {prepareSubmission();for(String body:List.of("{}","{\"qnaList\":null}","{\"qnaList\":[{}]}")){var r=submit(body);assertThat(r.getResponse().getStatus()).isEqualTo(400);}assertThat(applicationCount()).isZero();}
    @Test void APPLY_06_empty() throws Exception {prepareSubmission();var r=submit("{\"qnaList\":[]}");applyRecord("APPLY-06-empty",r);assertThat(r.getResponse().getStatus()).isEqualTo(200);assertThat(applicationCount()).isEqualTo(1);assertThat(answerCount()).isZero();}
    @Test void APPLY_06_missingQuestion() throws Exception {prepareSubmission();var r=submit("{\"qnaList\":[{\"questionId\":9223372036854775806}]}");applyRecord("APPLY-06-missing",r);assertThat(r.getResponse().getStatus()).isEqualTo(500);assertThat(applicationCount()).isZero();}
    @Test void APPLY_06_missingOption() throws Exception {String valid=prepareSubmission();long question=json.readTree(valid).get("qnaList").get(1).get("questionId").asLong();var r=submit("{\"qnaList\":[{\"questionId\":"+question+",\"optionId\":9223372036854775806}]}");applyRecord("APPLY-06-missing-option",r);assertThat(r.getResponse().getStatus()).isEqualTo(500);assertThat(applicationCount()).isZero();}
    @Test void APPLY_08_deletedDate() throws Exception {principal=new CustomUserDetails(user,List.of());tx.executeWithoutResult(s->ReflectionTestUtils.setField(em.find(Aplict.class,a1.getAplictId()),"deleteDate",LocalDateTime.now().minusDays(1)));var r=submit("{\"qnaList\":[]}");applyRecord("APPLY-08",r);assertThat(r.getResponse().getStatus()).isEqualTo(400);assertThat(applicationCount()).isEqualTo(1);}
    @ParameterizedTest @ValueSource(strings={"member","phone","student"}) void APPLY_04_memberChecks(String kind) throws Exception {
        prepareSubmission();tx.executeWithoutResult(s->{var p=em.find(Profile.class,kind.equals("member")?a1.getProfile().getProfileId():a2.getProfile().getProfileId());
            if(kind.equals("phone"))ReflectionTestUtils.setField(p,"studentNumber","87654321");
            if(kind.equals("student"))ReflectionTestUtils.setField(p,"userHp","01099999999");
            em.persist(ClubMembers.builder().club(em.find(Club.class,club.getClubId())).profile(p).build());});
        var r=submit("{\"qnaList\":[]}");applyRecord("APPLY-04-"+kind,r);assertThat(r.getResponse().getStatus()).isEqualTo(400);
        assertThat(r.getResponse().getContentAsString()).contains(switch(kind){case "member"->"APT-206";case "phone"->"APT-207";default->"APT-208";});assertThat(applicationCount()).isZero();
    }
    @Test void APPLY_07_foreignQuestionAndOption() throws Exception {
        String body=prepareSubmission();var source=json.readTree(body).get("qnaList");long ownText=source.get(0).get("questionId").asLong(),ownOption=source.get(1).get("optionId").asLong();
        long[] foreign=tx.execute(s->{var form=ClubForm.builder().club(em.find(Club.class,other.getClubId())).createdBy(1L).build();var q=FormQuestion.builder().type(QuestionType.RADIO).content("foreign").build();var o=FormQuestionOption.builder().content("foreign-option").build();q.addOption(o);form.addQuestion(q);em.persist(form);em.flush();return new long[]{q.getQuestionId(),o.getOptionId()};});
        var r=submit("{\"qnaList\":[{\"questionId\":"+ownText+",\"optionId\":"+foreign[1]+"},{\"questionId\":"+foreign[0]+",\"optionId\":"+foreign[1]+",\"optionIds\":["+ownOption+"]}]}");
        applyRecord("APPLY-07",r);assertThat(r.getResponse().getStatus()).isEqualTo(200);assertThat(answerCount()).isEqualTo(2);
        tx.executeWithoutResult(s->{var answers=em.createQuery("select x from AplictAnswer x where x.aplict.profile.profileId=:id",AplictAnswer.class).setParameter("id",a1.getProfile().getProfileId()).getResultList();
            assertThat(answers).anySatisfy(x->{assertThat(x.getFormQuestion().getQuestionId()).isEqualTo(foreign[0]);assertThat(x.getOption().getOptionId()).isEqualTo(ownOption);});});
    }
    @Test void APPLY_09_missingClubAndProfile() throws Exception {
        prepareSubmission();var missingClub=call("POST","/clubs/"+UUID.randomUUID()+"/applications","{\"qnaList\":[]}");assertThat(missingClub.getResponse().getStatus()).isEqualTo(404);
        principal=new CustomUserDetails(User.builder().userUUID(UUID.randomUUID()).role(Role.USER).build(),List.of());var missingProfile=submit("{\"qnaList\":[]}");applyRecord("APPLY-09",missingClub,missingProfile);assertThat(missingProfile.getResponse().getStatus()).isEqualTo(400);assertThat(applicationCount()).isZero();
    }
    @Test void STATUS_01_privateOnly() throws Exception {for(String status:List.of("PASS","FAIL","WAIT")){var r=call("PATCH",base()+"/applications/"+a3.getAplictUUID()+"/status","{\"status\":\""+status+"\"}");assertThat(r.getResponse().getStatus()).isEqualTo(200);tx.executeWithoutResult(s->{var current=em.find(Aplict.class,a3.getAplictId());assertThat(current.getPrivateStatus().name()).isEqualTo(status);assertThat(current.getPublicStatus()).isEqualTo(AplictStatus.WAIT);});}assertThat(wire.getAllServeEvents()).isEmpty();}
    @Test void STATUS_03_missingAndForeign() throws Exception {var missing=call("PATCH",base()+"/applications/"+UUID.randomUUID()+"/status","{\"status\":\"PASS\"}");var foreign=call("PATCH",base()+"/applications/"+a4.getAplictUUID()+"/status","{\"status\":\"PASS\"}");record("STATUS-03",missing,foreign);assertThat(missing.getResponse().getContentAsString()).contains("APT-202");assertThat(foreign.getResponse().getContentAsString()).contains("APT-201");assertThat(missing.getResponse().getStatus()).isEqualTo(404);assertThat(foreign.getResponse().getStatus()).isEqualTo(404);}
    @ParameterizedTest @ValueSource(strings={"[]","[{}]","[null]","[{\"aplictUUID\":null}]",""}) void PUB_10_validation(String body) throws Exception {var r=call("POST",base()+"/applicants/notifications",body);record("PUB-10-"+Integer.toUnsignedString(body.hashCode()),r);state("PASS/WAIT","FAIL/WAIT",0);assertThat(wire.getAllServeEvents()).isEmpty();}
    @ParameterizedTest @ValueSource(strings={"{}","{\"status\":null}","{\"status\":\"OTHER\"}"}) void STATUS_04_validation(String body) throws Exception {var r=call("PATCH",base()+"/applications/"+a1.getAplictUUID()+"/status",body);assertThat(r.getResponse().getStatus()).isEqualTo(400);state("PASS/WAIT","FAIL/WAIT",0);}
    @Test void LIST_emptyAndInvalid() throws Exception {tx.executeWithoutResult(s->em.createQuery("delete from Aplict a where a.club.clubId=:id").setParameter("id",club.getClubId()).executeUpdate());var r=call("GET",base()+"/applicants","");assertThat(json.readTree(r.getResponse().getContentAsString()).get("data").size()).isZero();var bad=call("GET",base()+"/applicants?status=OTHER","");Files.writeString(evidence.resolve("LIST-03.json"),bad.getResponse().getContentAsString());assertThat(bad.getResponse().getStatus()).isEqualTo(500);}
    @ParameterizedTest @ValueSource(strings={"?isResultPublished=OTHER","/invalid-uuid"}) void LIST_invalidTypes(String input) throws Exception {String path=input.startsWith("?")?base()+"/applicants"+input:"/clubs/not-a-uuid/applicants";var r=call("GET",path,"");record("LIST-03-"+Integer.toUnsignedString(input.hashCode()),r);assertThat(r.getResponse().getStatus()).isEqualTo(500);}
    @Test void AUTH_filterMatrix() throws Exception {
        var jwt=context.getBean(com.USWCicrcleLink.server.global.security.jwt.JwtProvider.class);
        org.mockito.Mockito.when(jwt.resolveAccessToken(org.mockito.ArgumentMatchers.any())).thenAnswer(i->((jakarta.servlet.http.HttpServletRequest)i.getArgument(0)).getHeader("Authorization"));
        org.mockito.Mockito.when(jwt.validateAccessToken(org.mockito.ArgumentMatchers.anyString())).thenAnswer(i->switch((String)i.getArgument(0)){
            case "expired" -> com.USWCicrcleLink.server.global.security.jwt.domain.TokenValidationResult.EXPIRED;
            case "invalid" -> com.USWCicrcleLink.server.global.security.jwt.domain.TokenValidationResult.INVALID;
            default -> com.USWCicrcleLink.server.global.security.jwt.domain.TokenValidationResult.VALID;
        });
        org.mockito.Mockito.when(jwt.getAuthentication(org.mockito.ArgumentMatchers.anyString())).thenAnswer(i->{
            var fixtureLeader=mock(Leader.class);org.mockito.Mockito.when(fixtureLeader.getLeaderUUID()).thenReturn(UUID.fromString("10000000-0000-4000-8000-000000000001"));
            var fixtureAdmin=mock(Admin.class);org.mockito.Mockito.when(fixtureAdmin.getAdminUUID()).thenReturn(UUID.fromString("20000000-0000-4000-8000-000000000001"));
            org.mockito.Mockito.when(fixtureLeader.getRole()).thenReturn(Role.LEADER);
            org.mockito.Mockito.when(fixtureAdmin.getRole()).thenReturn(Role.ADMIN);
            String token=i.getArgument(0);org.springframework.security.core.userdetails.UserDetails details=switch(token){
                case "user" -> new CustomUserDetails(user,List.of());
                case "otherLeader" -> new CustomLeaderDetails(fixtureLeader,other.getClubuuid());
                case "admin" -> new CustomAdminDetails(fixtureAdmin);
                default -> new CustomLeaderDetails(fixtureLeader,club.getClubuuid());
            };return new UsernamePasswordAuthenticationToken(details,null,details.getAuthorities());
        });
        var secure=MockMvcBuilders.standaloneSetup(construct(ClubController.class,context,Map.of(ClubLeaderService.class,leader,FcmServiceImpl.class,fcm)),construct(AplictController.class,context,Map.of(AplictService.class,applications,ClubLeaderService.class,leader)))
            .setControllerAdvice(new GlobalExceptionHandler()).addFilters(context.getBean("springSecurityFilterChain",jakarta.servlet.Filter.class)).build();
        var observations=new ArrayList<Map<String,Object>>();
        for(String token:List.of("none","invalid","expired","user","leader","otherLeader","admin"))for(String endpoint:List.of("PUB","STATUS","LIST","APPLY")){
            String path=switch(endpoint){case "PUB"->base()+"/applicants/notifications";case "STATUS"->base()+"/applications/"+a1.getAplictUUID()+"/status";case "LIST"->base()+"/applicants";default->base()+"/applications";};
            var builder=switch(endpoint){case "LIST"->org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(path);case "STATUS"->org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(path).content("{\"status\":\"PASS\"}");default->org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(path).content(endpoint.equals("PUB")?"[]":"{\"qnaList\":[]}");};
            if(!token.equals("none"))builder.header("Authorization",token);
            var r=secure.perform(builder.contentType("application/json")).andReturn();
            int expected=token.equals("none")||token.equals("invalid")||token.equals("expired")?401:
                endpoint.equals("APPLY")?(token.equals("user")?400:403):token.equals("user")||token.equals("otherLeader")?403:200;
            observations.add(Map.of("tokenKind",token,"endpoint",endpoint,"http",r.getResponse().getStatus(),"expected",expected,"body",new String(r.getResponse().getContentAsByteArray(),java.nio.charset.StandardCharsets.UTF_8)));
        }
        Files.writeString(evidence.resolve("AUTH-matrix.json"),json.writerWithDefaultPrettyPrinter().writeValueAsString(observations));
        for(var o:observations)assertThat(o.get("http")).as(o.toString()).isEqualTo(o.get("expected"));
    }
}

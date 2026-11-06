package com.USWCicrcleLink.server.baseline;

import com.USWCicrcleLink.server.admin.domain.Admin;
import com.USWCicrcleLink.server.club.api.ClubController;
import com.USWCicrcleLink.server.club.application.domain.*;
import com.USWCicrcleLink.server.club.domain.*;
import com.USWCicrcleLink.server.club.leader.service.*;
import com.USWCicrcleLink.server.global.exception.GlobalExceptionHandler;
import com.USWCicrcleLink.server.global.security.details.CustomAdminDetails;
import com.USWCicrcleLink.server.user.profile.domain.*;
import com.USWCicrcleLink.server.user.profile.domain.Profile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.zaxxer.hikari.*;
import com.zaxxer.hikari.metrics.*;
import jakarta.persistence.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.core.env.MapPropertySource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.Mockito.mock;

/** Real loopback HTTP + production publication code + MySQL; authentication is a test fixture. */
public class PublicationBenchmarkServer {
    @Configuration
    @Import(BaselineReproductionTest.Config.class)
    static class Config {
        @Bean ClubController controller(ApplicationContext c, ClubLeaderService leader, BaselineReproductionTest.TestFcm fcm) throws Exception {
            return BaselineReproductionTest.construct(ClubController.class,c,Map.of(ClubLeaderService.class,leader,FcmServiceImpl.class,fcm));
        }
        @Bean GlobalExceptionHandler errors(){return new GlobalExceptionHandler();}
        @Bean Control control(PlatformTransactionManager m, DataSource ds, WireMockServer wire){return new Control(m,(HikariDataSource)ds,wire);}
    }

    public static void main(String[] args) throws Exception {
        String url=System.getenv("BENCHMARK_MYSQL_URL");
        if(url==null || !url.matches("jdbc:mysql://127\\.0\\.0\\.1:[0-9]+/baseline_load\\?.*"))
            throw new IllegalArgumentException("Dedicated loopback baseline_load database required");
        Path output=Path.of(Objects.requireNonNull(System.getenv("BENCHMARK_OUTPUT"))).toAbsolutePath();
        Files.createDirectories(output);
        Tomcat tomcat=new Tomcat();tomcat.setBaseDir(Files.createTempDirectory("publication-tomcat-").toString());
        tomcat.setPort(0);tomcat.getConnector().setProperty("address","127.0.0.1");
        var web=tomcat.addContext("",Files.createTempDirectory("publication-web-").toString());
        web.setParentClassLoader(PublicationBenchmarkServer.class.getClassLoader());
        var app=new AnnotationConfigWebApplicationContext();
        var props=new HashMap<String,Object>();
        props.put("spring.datasource.url",url);props.put("spring.datasource.username","root");
        props.put("spring.datasource.password","benchmark-local-only");
        props.put("spring.jpa.hibernate.ddl-auto","create-drop");props.put("spring.jpa.open-in-view","false");
        props.put("spring.datasource.hikari.maximum-pool-size","10");props.put("spring.datasource.hikari.minimum-idle","10");
        props.put("spring.jpa.show-sql","false");props.put("spring.jpa.properties.hibernate.format_sql","false");
        props.put("fcm.api-url","http://127.0.0.1:1/blocked");props.put("firebase.config-path","unused-benchmark");
        props.put("cors.allowed-origins","http://localhost");
        app.getEnvironment().getPropertySources().addFirst(new MapPropertySource("benchmark",props));
        app.setServletContext(web.getServletContext());app.register(Config.class);app.refresh();
        var fcm=AopTestUtils.<BaselineReproductionTest.TestFcm>getTargetObject(app.getBean(BaselineReproductionTest.TestFcm.class));
        var wire=app.getBean(WireMockServer.class);ReflectionTestUtils.setField(fcm,"fcmApiUrl",wire.baseUrl()+"/send");
        var control=app.getBean(Control.class);control.output=output;
        var def=new FilterDef();def.setFilterName("local-fixture-auth");def.setFilter(new Filter(){
            public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain) throws java.io.IOException,ServletException {
                if(!"Bearer benchmark-local-only".equals(((HttpServletRequest)request).getHeader("Authorization"))){
                    ((HttpServletResponse)response).sendError(401);return;
                }
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(new CustomAdminDetails(mock(Admin.class)),null,List.of()));
                try{chain.doFilter(request,response);}finally{SecurityContextHolder.clearContext();}
            }
        });web.addFilterDef(def);var mapping=new FilterMap();mapping.setFilterName(def.getFilterName());mapping.addURLPattern("/*");web.addFilterMap(mapping);
        Tomcat.addServlet(web,"dispatcher",new DispatcherServlet(app)).setLoadOnStartup(1);web.addServletMappingDecoded("/","dispatcher");
        tomcat.start();
        Runtime.getRuntime().addShutdownHook(new Thread(()->{control.scheduler.shutdownNow();app.close();try{tomcat.stop();tomcat.destroy();}catch(Exception ignored){}}));
        Map<String,Object> ready=Map.of("port",tomcat.getConnector().getLocalPort(),"pid",ProcessHandle.current().pid(),
            "heapMaxBytes",Runtime.getRuntime().maxMemory(),"processors",Runtime.getRuntime().availableProcessors(),
            "javaVersion",System.getProperty("java.version"),"poolMaximum",control.pool.getMaximumPoolSize(),"wireMock",wire.baseUrl(),"scope","HTTP controller/service/JPA/MySQL; test authentication; fake Google token; local WireMock");
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(output.resolve("ready.json").toFile(),ready);
        new CountDownLatch(1).await();
    }

    @RestController
    @RequestMapping("/benchmark")
    static class Control {
        @PersistenceContext EntityManager em;
        final TransactionTemplate tx;final HikariDataSource pool;final WireMockServer wire;
        final ScheduledExecutorService scheduler=Executors.newSingleThreadScheduledExecutor();
        final Acquisition acquisition=new Acquisition();final ObjectMapper json=new ObjectMapper();
        final List<Map<String,Object>> samples=new CopyOnWriteArrayList<>();
        final List<Long> clubs=new ArrayList<>();Path output;ScheduledFuture<?> sampling;String run;
        Control(PlatformTransactionManager m,HikariDataSource pool,WireMockServer wire){
            tx=new TransactionTemplate(m);this.pool=pool;this.wire=wire;
            pool.setMetricsTrackerFactory((name,stats)->acquisition);
        }
        @PostMapping("/prepare") synchronized Map<String,Object> prepare(@RequestParam int count,@RequestParam int batches,@RequestParam String runId){
            if(sampling!=null)throw new IllegalStateException("Stop active run first");
            if(!List.of(2,50,100).contains(count)||batches<1||batches>200||!runId.matches("[a-zA-Z0-9_-]+"))throw new IllegalArgumentException("Invalid dataset request");
            if(pool.getHikariPoolMXBean().getActiveConnections()!=0)throw new IllegalStateException("Requests still active");
            clubs.clear();run=runId;wire.resetAll();
            wire.stubFor(post(urlEqualTo("/send")).willReturn(okJson("{\"name\":\"benchmark-accepted\"}").withFixedDelay(200)));
            var datasets=new ArrayList<Map<String,Object>>();
            for(int b=0;b<batches;b++){
                final int batch=b;
                datasets.add(tx.execute(s->{
                    var c=Club.builder().clubName("B"+UUID.randomUUID().toString().substring(0,9)).leaderName("tester").leaderHp("01000000000")
                        .department(Department.ACADEMIC).clubRoomNumber("100").build();em.persist(c);clubs.add(c.getClubId());
                    var body=new ArrayList<Map<String,Object>>();
                    for(int i=0;i<count;i++){
                        var p=Profile.builder().userName("tester").studentNumber("12345678").userHp("01000000000").major("test")
                            .memberType(MemberType.REGULARMEMBER).profileCreatedAt(LocalDateTime.of(2026,1,1,0,0)).profileUpdatedAt(LocalDateTime.of(2026,1,1,0,0))
                            .fcmToken(runId+"-"+batch+"-"+i).build();em.persist(p);
                        var a=Aplict.builder().club(c).profile(p).submittedAt(LocalDateTime.of(2026,1,1,0,0)).privateStatus(i<count/2?AplictStatus.PASS:AplictStatus.FAIL).build();em.persist(a);
                        body.add(Map.of("aplictUUID",a.getAplictUUID().toString()));
                    }
                    return Map.<String,Object>of("clubUUID",c.getClubuuid().toString(),"body",body);
                }));
            }
            return Map.of("count",count,"batches",datasets,"runId",runId,"pattern","first half PASS, second half FAIL; all public WAIT; unique UUIDs");
        }
        @PostMapping("/start") synchronized Map<String,Object> start(){
            if(sampling!=null)throw new IllegalStateException("Already active");
            samples.clear();acquisition.clear();
            sampling=scheduler.scheduleAtFixedRate(()->{
                var p=pool.getHikariPoolMXBean();var os=(com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
                samples.add(Map.of("time",Instant.now().toString(),"active",p.getActiveConnections(),"idle",p.getIdleConnections(),"waiting",p.getThreadsAwaitingConnection(),
                    "heapUsedBytes",ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed(),"processCpuLoad",os.getProcessCpuLoad(),"processCpuTimeNs",os.getProcessCpuTime()));
            },0,100,TimeUnit.MILLISECONDS);
            return Map.of("started",true);
        }
        @PostMapping("/stop") synchronized Map<String,Object> stop() throws Exception {
            if(sampling==null)throw new IllegalStateException("Not active");
            if(pool.getHikariPoolMXBean().getActiveConnections()!=0)throw new IllegalStateException("Requests still active");
            sampling.cancel(false);sampling=null;
            var timing=acquisition.summary();
            var state=tx.execute(s->Map.of("published",em.createQuery("select count(a) from Aplict a where a.club.clubId in :ids and a.publicStatus <> :wait",Long.class).setParameter("ids",clubs).setParameter("wait",AplictStatus.WAIT).getSingleResult(),
                "members",em.createQuery("select count(m) from ClubMembers m where m.club.clubId in :ids",Long.class).setParameter("ids",clubs).getSingleResult()));
            var journal=wire.getAllServeEvents().stream().map(e->Map.of("status",e.getResponse().getStatus(),"body",e.getRequest().getBodyAsString(),"time",e.getRequest().getLoggedDate().toInstant().toString())).toList();
            Path folder=output.resolve(run);Files.createDirectories(folder);
            try(var writer=Files.newBufferedWriter(folder.resolve("resources.jsonl"))){for(var sample:List.copyOf(samples)){writer.write(json.writeValueAsString(sample));writer.newLine();}}
            json.writerWithDefaultPrettyPrinter().writeValue(folder.resolve("fcm.json").toFile(),journal);
            var result=Map.of("runId",run,"db",state,"fcmCalls",journal.size(),"acquisition",timing,"resourceSamples",samples.size());
            json.writerWithDefaultPrettyPrinter().writeValue(folder.resolve("verification.json").toFile(),result);return result;
        }
        @PostMapping("/shutdown") Map<String,Object> shutdown(){scheduler.schedule(()->System.exit(0),500,TimeUnit.MILLISECONDS);return Map.of("stopping",true);}
    }
    static class Acquisition implements IMetricsTracker {
        final List<Long> nanos=new ArrayList<>();
        @Override public synchronized void recordConnectionAcquiredNanos(long n){nanos.add(n);}
        synchronized void clear(){nanos.clear();}
        synchronized Map<String,Object> summary(){var values=nanos.stream().sorted().toList();return Map.of("count",values.size(),"meanMs",values.stream().mapToLong(Long::longValue).average().orElse(0)/1e6,
            "maxMs",values.isEmpty()?0:values.get(values.size()-1)/1e6,"p95Ms",values.isEmpty()?0:values.get((int)Math.ceil(values.size()*.95)-1)/1e6);}
    }
}

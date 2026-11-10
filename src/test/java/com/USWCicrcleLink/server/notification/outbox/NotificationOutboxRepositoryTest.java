package com.USWCicrcleLink.server.notification.outbox;

import com.USWCicrcleLink.server.club.application.domain.AplictStatus;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.*;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

@SpringJUnitConfig(NotificationOutboxRepositoryTest.Config.class)
@TestPropertySource(properties={"spring.jpa.hibernate.ddl-auto=validate", "spring.jpa.open-in-view=false"})
@EnabledIfEnvironmentVariable(named="OUTBOX_MYSQL_URL",matches="jdbc:mysql://127\\.0\\.0\\.1:[0-9]+/outbox_week11.*")
class NotificationOutboxRepositoryTest {
    @Configuration
    @EnableTransactionManagement
    @ImportAutoConfiguration(HibernateJpaAutoConfiguration.class)
    @EntityScan(basePackageClasses=NotificationOutbox.class)
    @EnableJpaRepositories(basePackageClasses=NotificationOutboxRepository.class)
    static class Config {
        @Bean(destroyMethod="close") HikariDataSource dataSource() {
            var ds=new HikariDataSource();
            ds.setJdbcUrl(System.getenv("OUTBOX_MYSQL_URL"));
            ds.setUsername("root"); ds.setPassword("outbox-local-only"); ds.setMaximumPoolSize(2);
            new ResourceDatabasePopulator(new FileSystemResource("scripts/sql/20261110_create_notification_outbox.sql")).execute(ds);
            return ds;
        }
    }
    @Autowired NotificationOutboxRepository repository;
    @Autowired PlatformTransactionManager manager;
    @BeforeEach void clean(){repository.deleteAllInBatch();}
    NotificationOutbox task(UUID id,String token,AplictStatus result){
        return NotificationOutbox.applicationResult(id,42L,token,result,"동아리 지원 결과","테스트 동아리 결과");
    }
    @Test void ddlMatchesEntityAndPersistsSnapshot(){
        var id=UUID.randomUUID();var saved=repository.saveAndFlush(task(id," T1 ",AplictStatus.PASS));
        var loaded=repository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getApplicationUuid()).isEqualTo(id);
        assertThat(loaded.getRecipientProfileId()).isEqualTo(42L);
        assertThat(loaded.getRecipientToken()).isEqualTo("T1");
        assertThat(loaded.getResult()).isEqualTo(AplictStatus.PASS);
        assertThat(loaded.getTitle()).isEqualTo("동아리 지원 결과");
        assertThat(loaded.getBody()).isEqualTo("테스트 동아리 결과");
        assertThat(loaded.getStatus()).isEqualTo(NotificationOutbox.Status.PENDING);
        assertThat(loaded.getAttemptCount()).isZero();
        assertThat(loaded.getCreatedAt()).isNotNull();
        assertThat(loaded.getNextAttemptAt()).isNotNull();
        assertThat(loaded.getLastError()).isNull();
    }
    @Test void databaseRejectsDuplicateApplicationEvent(){
        var id=UUID.randomUUID();repository.saveAndFlush(task(id,"T1",AplictStatus.PASS));
        assertThatThrownBy(()->repository.saveAndFlush(task(id,"T2",AplictStatus.FAIL)))
            .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(repository.count()).isEqualTo(1);
    }
    @Test void allowsDifferentApplicationsForSameRecipient(){
        repository.saveAndFlush(task(UUID.randomUUID(),"T1",AplictStatus.PASS));
        repository.saveAndFlush(task(UUID.randomUUID(),"T1",AplictStatus.FAIL));
        assertThat(repository.count()).isEqualTo(2);
    }
    @Test void missingTokensAreRecordedAsSkipped(){
        for(String token:new String[]{null,"","  "}){
            var saved=repository.saveAndFlush(task(UUID.randomUUID(),token,AplictStatus.FAIL));
            assertThat(saved.getStatus()).isEqualTo(NotificationOutbox.Status.SKIPPED);
            assertThat(saved.getLastError()).isEqualTo("NO_FCM_TOKEN");
            assertThat(saved.getNextAttemptAt()).isNull();
            assertThat(saved.getRecipientToken()).isNull();
        }
    }
    @Test void waitDoesNotCreateAnAnnouncementTask(){
        assertThatIllegalArgumentException().isThrownBy(()->task(UUID.randomUUID(),"T1",AplictStatus.WAIT));
        assertThat(repository.count()).isZero();
    }
    @Test void participatesInCallerTransactionAndRollsBack(){
        var tx=new TransactionTemplate(manager);
        assertThatThrownBy(()->tx.executeWithoutResult(s->{
            repository.saveAndFlush(task(UUID.randomUUID(),"T1",AplictStatus.PASS));
            throw new IllegalStateException("intentional failure after flush");
        })).isInstanceOf(IllegalStateException.class);
        assertThat(repository.count()).isZero();
    }
}

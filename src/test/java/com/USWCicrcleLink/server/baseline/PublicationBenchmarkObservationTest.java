package com.USWCicrcleLink.server.baseline;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.transaction.PlatformTransactionManager;
import java.nio.file.*;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PublicationBenchmarkObservationTest {
    @TempDir Path output;

    @Test void savesPartialEvidenceEvenWhileDatabaseRequestIsActive() throws Exception {
        var manager=mock(PlatformTransactionManager.class);
        var pool=mock(HikariDataSource.class);
        var counters=mock(HikariPoolMXBean.class);
        when(pool.getHikariPoolMXBean()).thenReturn(counters);
        when(counters.getActiveConnections()).thenReturn(1);
        var wire=mock(WireMockServer.class);
        var control=new PublicationBenchmarkServer.Control(manager,pool,wire);
        try {
            control.output=output;control.run="interrupted";
            control.samples.add(Map.of("active",1,"time","2026-11-06T00:00:00Z"));
            var result=control.observations();
            assertThat(result).containsEntry("partial",true).containsEntry("activeConnections",1);
            assertThat(Files.readAllLines(output.resolve("interrupted/partial-resources.jsonl"))).hasSize(1);
            assertThat(new ObjectMapper().readTree(output.resolve("interrupted/partial-observation.json").toFile()).get("partial").asBoolean()).isTrue();
            assertThat(Files.exists(output.resolve("interrupted/verification.json"))).isFalse();
            verifyNoInteractions(manager);
        } finally {control.scheduler.shutdownNow();}
    }
}

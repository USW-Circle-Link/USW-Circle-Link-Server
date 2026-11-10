package com.USWCicrcleLink.server.notification.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.io.IOException;
import java.security.KeyPairGenerator;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

class OutboxGoogleTokenProviderTest {
    @TempDir Path directory;
    WireMockServer wire;
    static String pem;
    @BeforeAll static void key() throws Exception {
        var generator=KeyPairGenerator.getInstance("RSA");generator.initialize(2048);
        pem="-----BEGIN PRIVATE KEY-----\n"+Base64.getMimeEncoder(64,new byte[]{10}).encodeToString(generator.generateKeyPair().getPrivate().getEncoded())+"\n-----END PRIVATE KEY-----\n";
    }
    @BeforeEach void server(){wire=new WireMockServer(options().dynamicPort());wire.start();}
    @AfterEach void close(){wire.stop();}
    Path credentials() throws Exception {
        var path=directory.resolve("fake-service-account.json");
        Files.writeString(path,new ObjectMapper().writeValueAsString(Map.of("type","service_account","project_id","fake-project",
            "private_key_id","fake-key-id","private_key",pem,"client_email","fake@fake-project.iam.gserviceaccount.com",
            "client_id","1234","token_uri",wire.baseUrl()+"/token")));
        return path;
    }
    @Test void getsFakeTokenFromLocalAuthenticationServer() throws Exception {
        wire.stubFor(post(urlEqualTo("/token")).willReturn(okJson("{\"access_token\":\"fake-access-token\",\"expires_in\":3600,\"token_type\":\"Bearer\"}")));
        assertThat(new OutboxGoogleTokenProvider(credentials(),1000,1000).get()).isEqualTo("fake-access-token");
        wire.verify(1,postRequestedFor(urlEqualTo("/token")));
    }
    @Test void authenticationTimeoutHasNoHiddenRetries() throws Exception {
        wire.stubFor(post(urlEqualTo("/token")).willReturn(okJson("{\"access_token\":\"fake-access-token\",\"expires_in\":3600,\"token_type\":\"Bearer\"}").withFixedDelay(1000)));
        var provider=new OutboxGoogleTokenProvider(credentials(),100,100);
        assertThatThrownBy(provider::get).isInstanceOf(IOException.class);
        wire.verify(1,postRequestedFor(urlEqualTo("/token")));
    }
    @Test void authentication500HasNoHiddenRetries() throws Exception {
        wire.stubFor(post(urlEqualTo("/token")).willReturn(aResponse().withStatus(500)));
        var provider=new OutboxGoogleTokenProvider(credentials(),1000,1000);
        assertThatThrownBy(provider::get).isInstanceOf(IOException.class);
        wire.verify(1,postRequestedFor(urlEqualTo("/token")));
    }
    @Test void missingKeyFileIsAnIOException(){
        var provider=new OutboxGoogleTokenProvider(directory.resolve("missing.json"),100,100);
        assertThatThrownBy(provider::get).isInstanceOf(IOException.class);
        assertThat(wire.getAllServeEvents()).isEmpty();
    }
}

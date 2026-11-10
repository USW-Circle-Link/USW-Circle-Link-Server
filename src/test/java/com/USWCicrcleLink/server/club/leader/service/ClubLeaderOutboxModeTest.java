package com.USWCicrcleLink.server.club.leader.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClubLeaderOutboxModeTest {
    private final ClubLeaderService service=mock(ClubLeaderService.class,CALLS_REAL_METHODS);
    @Test void normalProfileKeepsDefaultSynchronousPath(){
        var env=new MockEnvironment();env.setActiveProfiles("prod");
        assertThatCode(()->service.configurePublicationMode(false,env)).doesNotThrowAnyException();
    }
    @Test void refusesOutboxModeOutsideDedicatedTestProfile(){
        var env=new MockEnvironment();env.setActiveProfiles("prod");
        assertThatIllegalStateException().isThrownBy(()->service.configurePublicationMode(true,env));
    }
    @Test void allowsExplicitTestMode(){
        var env=new MockEnvironment();env.setActiveProfiles("outbox-test");
        assertThatCode(()->service.configurePublicationMode(true,env)).doesNotThrowAnyException();
    }
}

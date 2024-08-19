package com.USWCicrcleLink.server.clubLeader.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@ConfigurationProperties(prefix = "spring.firebase")
public class FirebaseConfig {
    private String configPath;
}

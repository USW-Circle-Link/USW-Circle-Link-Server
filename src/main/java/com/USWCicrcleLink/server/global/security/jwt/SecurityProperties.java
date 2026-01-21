package com.USWCicrcleLink.server.global.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.security")
public class SecurityProperties {
    private List<String> permitAllPaths;
    private List<String> loggingPaths;
    private List<String> methods;
}

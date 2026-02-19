package com.USWCicrcleLink.server.global.security.config;

import com.USWCicrcleLink.server.global.security.exception.CustomAccessDeniedHandler;
import com.USWCicrcleLink.server.global.security.exception.CustomAuthenticationEntryPoint;
import com.USWCicrcleLink.server.global.security.jwt.filter.JwtFilter;
import com.USWCicrcleLink.server.global.security.jwt.filter.LoggingFilter;
import com.USWCicrcleLink.server.global.security.jwt.JwtProvider;
import com.USWCicrcleLink.server.global.security.jwt.SecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtProvider jwtProvider;
        private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
        private final CustomAccessDeniedHandler customAccessDeniedHandler;
        private final SecurityProperties securityProperties;

        @Value("#{'${cors.allowed-origins}'.split(',')}")
        private List<String> allowedOrigins;

        @Bean
        public JwtFilter jwtAuthFilter() {
                return new JwtFilter(jwtProvider, securityProperties.getPermitAllPaths(),
                                customAuthenticationEntryPoint);
        }

        @Bean
        public LoggingFilter loggingFilter() {
                return new LoggingFilter(securityProperties.getLoggingPaths(), securityProperties.getMethods());
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .exceptionHandling(
                                                exceptionHandling -> exceptionHandling
                                                                .authenticationEntryPoint(
                                                                                customAuthenticationEntryPoint)
                                                                .accessDeniedHandler(customAccessDeniedHandler))
                                .authorizeHttpRequests(auth -> {
                                        auth.requestMatchers(
                                                        securityProperties.getPermitAllPaths().toArray(new String[0]))
                                                        .permitAll();

                                        // Public Club Endpoints
                                        auth.requestMatchers(HttpMethod.GET, "/clubs", "/clubs/filter", "/clubs/open",
                                                        "/clubs/{clubUUID}",
                                                        "/clubs/open/filter").permitAll();

                                        // Admin Club Management
                                        auth.requestMatchers(HttpMethod.POST, "/clubs").hasRole("ADMIN");
                                        // 동아리 자체 삭제는 ADMIN만 가능 (정확한 경로만 매칭)
                                        auth.requestMatchers(HttpMethod.DELETE, "/clubs/{clubUUID}").hasRole("ADMIN");

                                        // 동아리 하위 리소스 (회원, 지원자, 모집폼 등) - Leader와 Admin 모두 접근 가능
                                        auth.requestMatchers(HttpMethod.GET, "/clubs/{clubUUID}/members",
                                                        "/clubs/{clubUUID}/applicants",
                                                        "/clubs/{clubUUID}/forms",
                                                        "/clubs/{clubUUID}/recruit-status")
                                                        .hasAnyRole("LEADER", "ADMIN");
                                        auth.requestMatchers(HttpMethod.POST,
                                                        "/clubs/{clubUUID}/applicants/notifications",
                                                        "/clubs/{clubUUID}/forms")
                                                        .hasAnyRole("LEADER", "ADMIN");
                                        auth.requestMatchers(HttpMethod.PUT, "/clubs/{clubUUID}")
                                                        .hasAnyRole("LEADER", "ADMIN");
                                        auth.requestMatchers(HttpMethod.PATCH, "/clubs/{clubUUID}/recruit-status",
                                                        "/clubs/{clubUUID}/applications/{applicationUUID}/status",
                                                        "/clubs/terms/agreement")
                                                        .hasAnyRole("LEADER", "ADMIN");
                                        auth.requestMatchers(HttpMethod.DELETE, "/clubs/{clubUUID}/members")
                                                        .hasAnyRole("LEADER", "ADMIN");

                                        // Category Management
                                        auth.requestMatchers(HttpMethod.GET, "/categories").hasAnyRole("LEADER",
                                                        "ADMIN");
                                        auth.requestMatchers(HttpMethod.POST, "/categories").hasRole("ADMIN");
                                        auth.requestMatchers(HttpMethod.DELETE, "/categories/**").hasRole("ADMIN");

                                        auth.requestMatchers(HttpMethod.GET, "/admin/clubs", "/admin/clubs/{clubUUID}")
                                                        .hasAnyRole("ADMIN",
                                                                        "LEADER");
                                        auth.requestMatchers(HttpMethod.GET, "/notices/**").permitAll();
                                        auth.requestMatchers(HttpMethod.POST, "/auth/withdrawal/code")
                                                        .hasAnyRole("USER", "LEADER");

                                        auth.requestMatchers(HttpMethod.GET, "/admin/**").hasRole("ADMIN");
                                        auth.requestMatchers(HttpMethod.POST, "/admin/**").hasRole("ADMIN");
                                        auth.requestMatchers(HttpMethod.DELETE, "/admin/**").hasRole("ADMIN");
                                        auth.requestMatchers(HttpMethod.PUT, "/admin/**").hasRole("ADMIN");

                                        auth.requestMatchers(HttpMethod.POST, "/notices/**").hasRole("ADMIN");
                                        auth.requestMatchers(HttpMethod.PUT, "/notices/**").hasRole("ADMIN");
                                        auth.requestMatchers(HttpMethod.DELETE, "/notices/**").hasRole("ADMIN");

                                        auth.requestMatchers(HttpMethod.PATCH, "/profiles/change", "/users/userpw",
                                                        "/club-leader/fcmtoken")
                                                        .hasRole("USER");
                                        auth.requestMatchers(HttpMethod.GET, "/mypages/my-clubs",
                                                        "/mypages/aplict-clubs",
                                                        "/profiles/me")
                                                        .hasRole("USER");
                                        auth.requestMatchers(HttpMethod.DELETE, "/users/exit").hasRole("USER");
                                        auth.requestMatchers(HttpMethod.POST, "/users/exit/send-code").hasRole("USER");
                                        auth.requestMatchers(HttpMethod.POST, "/apply/**").hasRole("USER");
                                        auth.requestMatchers(HttpMethod.GET, "/apply/**").hasRole("USER");
                                        auth.requestMatchers(HttpMethod.GET, "/users/event/**").hasRole("USER");
                                        auth.requestMatchers(HttpMethod.POST, "/users/event/**").hasRole("USER");
                                        auth.requestMatchers(HttpMethod.DELETE, "/users/event/**").hasRole("USER");
                                        auth.requestMatchers(HttpMethod.DELETE, "/users/event/**").hasRole("USER");

                                        auth.anyRequest().authenticated();
                                })
                                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                                .addFilterAfter(loggingFilter(), JwtFilter.class);
                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                allowedOrigins.stream()
                                .map(String::trim)
                                .filter(origin -> !origin.isEmpty())
                                .forEach(configuration::addAllowedOriginPattern);

                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(Arrays.asList("*"));

                configuration.addExposedHeader("Authorization");
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }
}
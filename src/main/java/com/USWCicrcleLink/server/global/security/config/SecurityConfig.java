package com.USWCicrcleLink.server.global.security.config;

import com.USWCicrcleLink.server.global.security.filter.JwtFilter;
import com.USWCicrcleLink.server.global.security.util.JwtProvider;
import lombok.RequiredArgsConstructor;
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

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public JwtFilter jwtAuthFilter() {
        return new JwtFilter(jwtProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // CSRF 보호 비활성화
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(customAuthenticationEntryPoint))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/users/login", // 모바일 로그인
                            "/users/temporary",
                            "/users/email/verify-token",
                            "/users/finish-signup",
                            "/users/verify-duplicate/{account}",
                            "/users/validate-passwords-match",
                            "/users/find-account/{email}",
                            "/users/auth/send-code",
                            "/users/auth/verify-token",
                            "/users/reset-password",
                            "/users/email/resend-confirmation",
                            "/mypages/notices",
                            "/auth/refresh-token", // 토큰 재발급
                            "/integration/login", // 동아리 회장, 동연회-개발자 통합 로그인
                            "/mainPhoto/**",
                            "/introPhoto/**"
                    ).permitAll();

                    // ADMIN
                    auth.requestMatchers(HttpMethod.POST, "/admin/clubs", "/admin/notices").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/admin/clubs", "/admin/notices", "/admin/notices/paged").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PATCH, "/admin/notices").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/admin/clubs", "/admin/notices").hasRole("ADMIN");

                    // USER
                    auth.requestMatchers(HttpMethod.POST, "/apply/").hasRole("USER");
                    auth.requestMatchers(HttpMethod.GET, "/apply/", "/clubs/", "/clubs/intro/", "/mypages/notices", "/mypages/my-clubs", "/mypages/aplict-clubs").hasRole("USER");
                    auth.requestMatchers(HttpMethod.GET, "/apply/", "/clubs/", "/clubs/intro/", "mypages/notices").hasRole("USER");

                    // LEADER
                    auth.requestMatchers(HttpMethod.POST, "/club-leader/info").hasRole("LEADER");
                    auth.requestMatchers(HttpMethod.GET, "/club-leader/members", "/club-leader/members/export").hasRole("LEADER");
                    auth.requestMatchers(HttpMethod.PATCH, "/club-leader/info", "/club-leader/intro", "/club-leader/toggle-recruitment").hasRole("LEADER");
                    auth.requestMatchers(HttpMethod.DELETE, "/club-leader/members").hasRole("LEADER");
                    auth.requestMatchers(HttpMethod.POST, "/club-leader/{clubId}/**", "/club-leader/fcm-token").hasRole("LEADER");
                    auth.requestMatchers(HttpMethod.GET, "/club-leader/{clubId}/**").hasRole("LEADER");
                    auth.requestMatchers(HttpMethod.PATCH, "/club-leader/{clubId}/**").hasRole("LEADER");
                    auth.requestMatchers(HttpMethod.DELETE, "/club-leader/{clubId}/members").hasRole("LEADER");


                    // INTEGRATION
                    auth.requestMatchers(HttpMethod.POST, "/integration/logout").authenticated(); // 통합 로그아웃 api
                    // 기타 모든 요청
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*"); // 프론트엔드 도메인 명시
        configuration.addAllowedMethod("*"); // 메소드 형식
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}

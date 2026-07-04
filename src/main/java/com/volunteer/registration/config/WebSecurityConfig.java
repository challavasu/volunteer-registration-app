package com.volunteer.registration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Enable CSRF protection with custom repository
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository())
                .ignoringRequestMatchers("/api/**", "/login", "/logout")
            )
            // Authorization rules
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/volunteer-signup", "/my-registrations", "/signup", "/checkin", "/login-test", "/css/**", "/js/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/login").permitAll()
                .requestMatchers("/logout").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/debug/**").permitAll()
                .requestMatchers("/api/campaigns/**").permitAll()
                .requestMatchers("/api/volunteers/**").permitAll()
                .requestMatchers("/api/registrations/**").permitAll()
                .requestMatchers("/api/jobs/**").permitAll()
                .requestMatchers("/api/shifts/**").permitAll()
                .anyRequest().authenticated()
            )
            // Security headers
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
                .xssProtection()
            )
            // Session security
            .sessionManagement(session -> session
                .sessionConcurrency(sessionConcurrency -> sessionConcurrency
                    .maximumSessions(1)
                )
            )
            // Exception handling - redirect to login on authentication failure
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.sendRedirect("/login");
                })
            )
            // Disable Spring Security's default login form (we use custom auth)
            .formLogin(formLogin -> formLogin.disable())
            // Logout configuration
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            );

        // H2 Console CSRF exception (development only)
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable().frameOptions(frameOptions2 -> frameOptions2.sameOrigin())));

        return http.build();
    }

    @Bean
    public HttpSessionCsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName("X-CSRF-TOKEN");
        repository.setParameterName("_csrf");
        return repository;
    }
}


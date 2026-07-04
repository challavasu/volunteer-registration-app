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
                .ignoringRequestMatchers("/login", "/logout")
            )
            // Authorization rules
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/volunteer-signup", "/my-registrations", "/signup", "/checkin", "/login-test", "/css/**", "/js/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/login").permitAll()
                .requestMatchers("/logout").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                // Public APIs for volunteer signup (read-only)
                .requestMatchers("GET", "/api/campaigns/active").permitAll()
                .requestMatchers("GET", "/api/campaigns/*").permitAll()
                .requestMatchers("GET", "/api/jobs/campaign/*/available").permitAll()
                .requestMatchers("GET", "/api/shifts/job/*/available").permitAll()
                .requestMatchers("POST", "/api/volunteers/register").permitAll()
                .requestMatchers("POST", "/api/registrations").permitAll()
                .requestMatchers("POST", "/api/registrations/send-confirmation/**").permitAll()
                .requestMatchers("GET", "/api/registrations/volunteer/email/**").permitAll()
                .requestMatchers("GET", "/api/checkin/**").permitAll()
                // Protected APIs - require authentication
                .requestMatchers("/api/**").authenticated()
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


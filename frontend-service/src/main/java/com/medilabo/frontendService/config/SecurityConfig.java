package com.medilabo.frontendService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the frontend service.
 * This configuration allows all requests and sets the session management to stateless.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures the security filter chain for the frontend service.
     *
     * @param http the HttpSecurity object to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.requestMatchers("/**").permitAll().anyRequest().authenticated()) // Allow all requests to the frontend service
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // Set session management to stateless
        return http.build();
    }
}

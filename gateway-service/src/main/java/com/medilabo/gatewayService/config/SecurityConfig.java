package com.medilabo.gatewayService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Security configuration for the gateway service.
 * This configuration disables CSRF protection and allows all exchanges.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * Configures the security filter chain for the gateway service.
     *
     * @param http the ServerHttpSecurity instance to configure
     * @return the configured SecurityWebFilterChain
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http
    ) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Disable CSRF protection
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll()) // Allow all exchanges
                .build(); // Build the security filter chain
    }
}

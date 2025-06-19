package com.medilabo.gatewayService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Gateway Service.
 * This service acts as a gateway for routing requests to various microservices.
 */
@SpringBootApplication
public class GatewayServiceApplication {

    /**
     * Main method to run the Gateway Service application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}

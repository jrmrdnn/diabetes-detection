package com.medilabo.frontendService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Main application class for the Frontend Service.
 */
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class FrontendServiceApplication {
    /**
     * Main method to run the Frontend Service application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(FrontendServiceApplication.class, args);
    }
}

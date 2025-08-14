package com.medilabo.eurekaServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * The main application class for the Eureka Server.
 * This class is responsible for starting the Eureka server which acts as a service registry.
 */
@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {

    /**
     * The main method to run the Eureka Server application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}

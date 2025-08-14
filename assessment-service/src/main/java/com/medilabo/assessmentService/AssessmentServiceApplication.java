package com.medilabo.assessmentService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Main application class for the Assessment Service.
 */
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class AssessmentServiceApplication {

  /**
   * Main method to run the Assessment Service application.
   *
   * @param args command line arguments
   * @see SpringApplication
   * @see SpringBootApplication
   */
  public static void main(String[] args) {
    SpringApplication.run(AssessmentServiceApplication.class, args);
  }
}

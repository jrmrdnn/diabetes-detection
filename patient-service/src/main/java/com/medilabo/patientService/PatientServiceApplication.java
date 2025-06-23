package com.medilabo.patientService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Patient Service.
 * This class is responsible for starting the Spring Boot application.
 */
@SpringBootApplication
public class PatientServiceApplication {

  /**
   * The main method to run the Patient Service application.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(PatientServiceApplication.class, args);
  }
}

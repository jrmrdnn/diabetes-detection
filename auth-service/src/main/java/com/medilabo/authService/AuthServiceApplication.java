package com.medilabo.authService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Auth Service.
 */
@SpringBootApplication
public class AuthServiceApplication {

  /**
   * Main method to run the Auth Service application.
   *
   * @param args command line arguments
   * @see SpringApplication
   * @see SpringBootApplication
   */
  public static void main(String[] args) {
    SpringApplication.run(AuthServiceApplication.class, args);
  }
}

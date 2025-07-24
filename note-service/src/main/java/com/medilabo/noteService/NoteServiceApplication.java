package com.medilabo.noteService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Note Service.
 * This service is responsible for managing notes in the system.
 */
@SpringBootApplication
public class NoteServiceApplication {

  /**
   * Main method to run the Note Service application.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(NoteServiceApplication.class, args);
  }
}
